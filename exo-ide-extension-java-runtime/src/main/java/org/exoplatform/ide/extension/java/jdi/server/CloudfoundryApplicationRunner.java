/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.ide.extension.java.jdi.server;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.ide.commons.RoundRobin;
import org.exoplatform.ide.extension.cloudfoundry.server.Cloudfoundry;
import org.exoplatform.ide.extension.cloudfoundry.server.CloudfoundryException;
import org.exoplatform.ide.extension.cloudfoundry.server.DebugMode;
import org.exoplatform.ide.extension.cloudfoundry.server.SimpleAuthenticator;
import org.exoplatform.ide.extension.cloudfoundry.shared.CloudFoundryApplication;
import org.exoplatform.ide.extension.cloudfoundry.shared.Instance;
import org.exoplatform.ide.extension.java.jdi.server.model.ApplicationInstanceImpl;
import org.exoplatform.ide.extension.java.jdi.server.model.DebugApplicationInstanceImpl;
import org.exoplatform.ide.extension.java.jdi.shared.ApplicationInstance;
import org.exoplatform.ide.extension.java.jdi.shared.DebugApplicationInstance;
import org.exoplatform.ide.helper.ParsingResponseException;
import org.exoplatform.ide.vfs.server.exceptions.VirtualFileSystemException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.exoplatform.ide.commons.ContainerUtils.readValueParam;
import static org.exoplatform.ide.commons.FileUtils.*;
import static org.exoplatform.ide.commons.NameGenerator.generate;
import static org.exoplatform.ide.commons.ZipUtils.listEntries;
import static org.exoplatform.ide.commons.ZipUtils.unzip;

/**
 * ApplicationRunner for deploy Java applications at Cloud Foundry PaaS.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class CloudfoundryApplicationRunner implements ApplicationRunner, Startable
{
   /** Default application lifetime (in minutes). After this time application may be stopped automatically. */
   private static final int DEFAULT_APPLICATION_LIFETIME = 10;

   private static final Log LOG = ExoLogger.getLogger(CloudfoundryApplicationRunner.class);

   private final int applicationLifetime;
   private final long applicationLifetimeMillis;

   private final Map<String, Cloudfoundry> servers;
   private final Map<String, CloudfoundryServerConfiguration> configs;
   private final RoundRobin<Cloudfoundry> balancer;

   private final Map<String, Application> applications;
   private final ScheduledExecutorService applicationTerminator;
   private final java.io.File appEngineSdk;

   public CloudfoundryApplicationRunner(InitParams initParams)
   {
      this(getConfigurations(initParams), parseApplicationLifeTime(
         readValueParam(initParams, "cloudfoundry-application-lifetime")));
   }

   private static List<CloudfoundryServerConfiguration> getConfigurations(InitParams params)
   {
      List<CloudfoundryServerConfiguration> configs = new ArrayList<CloudfoundryServerConfiguration>();
      if (params != null)
      {
         List<CloudfoundryServerConfiguration> objectParams =
            params.getObjectParamValues(CloudfoundryServerConfiguration.class);
         if (!(objectParams == null || objectParams.isEmpty()))
         {
            configs.addAll(objectParams);
         }
      }
      return configs;
   }

   private static int parseApplicationLifeTime(String str)
   {
      if (str != null)
      {
         try
         {
            return Integer.parseInt(str);
         }
         catch (NumberFormatException ignored)
         {
         }
      }
      return DEFAULT_APPLICATION_LIFETIME;
   }

   protected CloudfoundryApplicationRunner(List<CloudfoundryServerConfiguration> configs, int applicationLifetime)
   {
      if (configs == null || configs.isEmpty())
      {
         throw new IllegalArgumentException("At least one server must be configured. ");
      }
      this.servers = new HashMap<String, Cloudfoundry>();
      this.configs = new HashMap<String, CloudfoundryServerConfiguration>();
      for (CloudfoundryServerConfiguration config : configs)
      {
         this.configs.put(config.getTarget(), config);
         this.servers.put(config.getTarget(), new Cloudfoundry(new SimpleAuthenticator(config.getTarget())));
      }
      this.balancer = new RoundRobin<Cloudfoundry>(servers.values());
      this.applicationLifetime = applicationLifetime;
      this.applicationLifetimeMillis = applicationLifetime * 60 * 1000;

      this.applications = new ConcurrentHashMap<String, Application>();
      this.applicationTerminator = Executors.newSingleThreadScheduledExecutor();
      this.applicationTerminator.scheduleAtFixedRate(new TerminateApplicationTask(), 1, 1, TimeUnit.MINUTES);

      java.io.File lib = null;
      try
      {
         Class cl = Thread.currentThread().getContextClassLoader()
            .loadClass("com.google.appengine.tools.development.DevAppServerMain");
         URL cs = cl.getProtectionDomain().getCodeSource().getLocation();
         lib = new java.io.File(URI.create(cs.toString()));
         while (!(lib == null || "lib".equals(lib.getName())))
         {
            lib = lib.getParentFile();
         }
      }
      catch (ClassNotFoundException ignored)
      {
      }

      appEngineSdk = lib == null ? null : lib.getParentFile();
      if (appEngineSdk == null)
      {
         LOG.error("**********************************\n"
            + "* Google appengine Java SDK not found *\n"
            + "**********************************");
      }
   }

   @Override
   public ApplicationInstance runApplication(URL war) throws ApplicationRunnerException
   {
      return startApplication(balancer.next(), generate("app-", 16), war, null);
   }

   @Override
   public DebugApplicationInstance debugApplication(URL war, boolean suspend) throws ApplicationRunnerException
   {
      return (DebugApplicationInstance)startApplication(balancer.next(), generate("app-", 16), war,
         suspend ? new DebugMode("suspend") : new DebugMode());
   }

   private ApplicationInstance startApplication(Cloudfoundry cloudfoundry,
                                                String name,
                                                URL war,
                                                DebugMode debugMode) throws ApplicationRunnerException
   {
      final java.io.File path;
      try
      {
         path = downloadFile(null, "app-", ".war", war);
      }
      catch (IOException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }

      try
      {
         if (debugMode != null)
         {
            return doDebugApplication(cloudfoundry, name, path, debugMode);
         }
         return doRunApplication(cloudfoundry, name, path);
      }
      catch (ApplicationRunnerException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof CloudfoundryException)
         {
            if (200 == ((CloudfoundryException)cause).getExitCode())
            {
               // login and try one more time.
               login(cloudfoundry);
               if (debugMode != null)
               {
                  return doDebugApplication(cloudfoundry, name, path, debugMode);
               }
               return doRunApplication(cloudfoundry, name, path);
            }

            // try to remove application.
            try
            {
               LOG.error("Application {} failed to start. Try delete it. ", name);
               cloudfoundry.deleteApplication(cloudfoundry.getTarget(), name, null, null, true);
            }
            catch (Exception e1)
            {
               LOG.error("Unable delete failed application {}", name);
            }
         }
         throw e;
      }
      finally
      {
         if (path.exists())
         {
            path.delete();
         }
      }
   }

   private ApplicationInstance doRunApplication(Cloudfoundry cloudfoundry, String name, File path) throws ApplicationRunnerException
   {
      try
      {
         final String target = cloudfoundry.getTarget();
         final CloudFoundryApplication cfApp = createApplication(cloudfoundry, name, path, null);
         final long expired = System.currentTimeMillis() + applicationLifetimeMillis;

         applications.put(cfApp.getName(), new Application(cfApp.getName(), target, expired));
         LOG.debug("Start application {} at CF server {}", cfApp.getName(), target);
         return new ApplicationInstanceImpl(cfApp.getName(), cfApp.getUris().get(0), null, applicationLifetime);
      }
      catch (Exception e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
   }

   private DebugApplicationInstance doDebugApplication(Cloudfoundry cloudfoundry,
                                                       String name,
                                                       java.io.File path,
                                                       DebugMode debugMode) throws ApplicationRunnerException
   {
      try
      {
         final String target = cloudfoundry.getTarget();
         final CloudFoundryApplication cfApp = createApplication(cloudfoundry, name, path, debugMode);
         final long expired = System.currentTimeMillis() + applicationLifetimeMillis;

         Instance[] instances = cloudfoundry.applicationInstances(cloudfoundry.getTarget(), name, null, null);
         if (instances.length != 1)
         {
            throw new ApplicationRunnerException("Unable run application in debug mode. ");
         }

         applications.put(name, new Application(name, target, expired));
         LOG.debug("Start application {} under debug at CF server {}", name, target);
         return new DebugApplicationInstanceImpl(name, cfApp.getUris().get(0), null,
            applicationLifetime, instances[0].getDebugHost(), instances[0].getDebugPort());
      }
      catch (Exception e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
   }

   @Override
   public void stopApplication(String name) throws ApplicationRunnerException
   {
      Application application = applications.get(name);
      if (application != null)
      {
         Cloudfoundry cloudfoundry = servers.get(application.server);
         if (cloudfoundry != null)
         {
            try
            {
               doStopApplication(cloudfoundry, name);
            }
            catch (ApplicationRunnerException e)
            {
               Throwable cause = e.getCause();
               if (cause instanceof CloudfoundryException)
               {
                  if (200 == ((CloudfoundryException)cause).getExitCode())
                  {
                     login(cloudfoundry);
                     doStopApplication(cloudfoundry, name);
                  }
               }
               throw e;
            }
         }
         else
         {
            throw new ApplicationRunnerException("Server '" + application.server + "' not available. ");
         }
      }
      else
      {
         throw new ApplicationRunnerException("Application '" + name + "' not found. ");
      }
   }

   private void doStopApplication(Cloudfoundry cloudfoundry, String name) throws ApplicationRunnerException
   {
      try
      {
         cloudfoundry.stopApplication(cloudfoundry.getTarget(), name, null, null);
         cloudfoundry.deleteApplication(cloudfoundry.getTarget(), name, null, null, true);
         applications.remove(name);
         LOG.debug("Stop application {}.", name);
      }
      catch (Exception e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
   }

   @Override
   public void start()
   {
   }

   @Override
   public void stop()
   {
      applicationTerminator.shutdownNow();
      for (Application app : applications.values())
      {
         try
         {
            stopApplication(app.name);
         }
         catch (ApplicationRunnerException e)
         {
            LOG.error("Failed to stop application {}.", app.name, e);
         }
      }
      applications.clear();
   }

   private CloudFoundryApplication createApplication(Cloudfoundry cloudfoundry, String name, File path, DebugMode debug)
      throws CloudfoundryException, IOException, ParsingResponseException, VirtualFileSystemException
   {
      if (APPLICATION_TYPE.JAVA_WEB_APP_ENGINE == determineApplicationType(path))
      {
         if (appEngineSdk == null)
         {
            throw new RuntimeException("Unable run or debug appengine project. Google appengine Java SDK not found. ");
         }

         final java.io.File appengineApplication = createTempDirectory(null, "gae-app-");
         try
         {
            // copy sdk
            final java.io.File sdk = new java.io.File(appengineApplication, "appengine-java-sdk");
            if (!sdk.mkdir())
            {
               throw new IOException("Unable create directory " + sdk.getAbsolutePath());
            }
            copy(appEngineSdk, sdk, null);

            // unzip content of war file
            final java.io.File app = new java.io.File(appengineApplication, "application");
            if (!app.mkdir())
            {
               throw new IOException("Unable create directory " + app.getAbsolutePath());
            }
            unzip(path, app);

            final String command = "java -ea -cp appengine-java-sdk/lib/appengine-tools-api.jar "
               + "-javaagent:appengine-java-sdk/lib/agent/appengine-agent.jar $JAVA_OPTS "
               + "com.google.appengine.tools.development.DevAppServerMain --port=$VCAP_APP_PORT --address=0.0.0.0 --disable_update_check "
               + "application";

            return cloudfoundry.createApplication(cloudfoundry.getTarget(), name, "standalone", null, 1, 256, false, "java",
               command, debug, null, null, appengineApplication.toURI().toURL());
         }
         finally
         {
            deleteRecursive(appengineApplication);
         }
      }
      else
      {
         return cloudfoundry.createApplication(cloudfoundry.getTarget(), name, "spring", null, 1, 256, false, "java",
            null, debug, null, null, path.toURI().toURL());
      }
   }

   private enum APPLICATION_TYPE
   {
      JAVA_WEB,
      JAVA_WEB_APP_ENGINE
   }

   private APPLICATION_TYPE determineApplicationType(java.io.File war) throws IOException
   {
      for (String f : listEntries(war))
      {
         if (f.endsWith("WEB-INF/appengine-web.xml"))
         {
            return APPLICATION_TYPE.JAVA_WEB_APP_ENGINE;
         }
      }
      return APPLICATION_TYPE.JAVA_WEB;
   }

   private void login(Cloudfoundry cloudfoundry) throws ApplicationRunnerException
   {
      try
      {
         CloudfoundryServerConfiguration config = configs.get(cloudfoundry.getTarget());
         cloudfoundry.login(cloudfoundry.getTarget(), config.getUser(), config.getPassword());
      }
      catch (Exception e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
   }

   private class TerminateApplicationTask implements Runnable
   {
      @Override
      public void run()
      {
         List<String> stopped = new ArrayList<String>();
         for (Application app : applications.values())
         {
            if (app.isExpired())
            {
               try
               {
                  stopApplication(app.name);
               }
               catch (ApplicationRunnerException e)
               {
                  LOG.error("Failed to stop application {}.", app.name, e);
               }
               // Do not try to stop application twice.
               stopped.add(app.name);
            }
         }
         applications.keySet().removeAll(stopped);
         LOG.debug("{} applications removed. ", stopped.size());
      }
   }

   private static class Application
   {
      final String name;
      final String server;
      final long expirationTime;

      Application(String name, String server, long expirationTime)
      {
         this.name = name;
         this.server = server;
         this.expirationTime = expirationTime;
      }

      boolean isExpired()
      {
         return expirationTime < System.currentTimeMillis();
      }
   }
}