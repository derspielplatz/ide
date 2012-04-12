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
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.ide.extension.cloudfoundry.server.Cloudfoundry;
import org.exoplatform.ide.extension.cloudfoundry.server.CloudfoundryAuthenticator;
import org.exoplatform.ide.extension.cloudfoundry.server.CloudfoundryCredentials;
import org.exoplatform.ide.extension.cloudfoundry.server.CloudfoundryException;
import org.exoplatform.ide.extension.cloudfoundry.server.DebugMode;
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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ApplicationRunner for deploy Java applications at Cloud Foundry PaaS.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class CloudfoundryApplicationRunner implements ApplicationRunner, Startable
{
   /** Default application lifetime (in minutes). After this time application may be stopped automatically. */
   public static final int DEFAULT_APPLICATION_LIFETIME = 10;

   private static final Log LOG = ExoLogger.getLogger(CloudfoundryApplicationRunner.class);

   //
   private static final Random RANDOM = new Random();
   private static final char[] CHARS = new char[36];

   static
   {
      int i = 0;
      for (int c = 48; c <= 57; c++)
      {
         CHARS[i++] = (char)c;
      }
      for (int c = 97; c <= 122; c++)
      {
         CHARS[i++] = (char)c;
      }
   }

   private static String generateAppName(int length)
   {
      StringBuilder b = new StringBuilder(length + 4);
      b.append("app-");
      for (int i = 0; i < length; i++)
      {
         b.append(CHARS[RANDOM.nextInt(CHARS.length)]);
      }
      return b.toString();
   }

   private final int applicationLifetime;
   private final long applicationLifetimeMillis;

   private final Cloudfoundry cloudfoundry;
   private final List<Application> applications;
   private final ScheduledExecutorService applicationTerminator;

   public CloudfoundryApplicationRunner(InitParams initParams)
   {
      this(
         getValueParam(initParams, "cloudfoundry-target"),
         getValueParam(initParams, "cloudfoundry-token"),
         parseNumber(getValueParam(initParams, "cloudfoundry-application-lifetime"), DEFAULT_APPLICATION_LIFETIME)
            .intValue()
      );
   }

   protected CloudfoundryApplicationRunner(String cfTarget, String cfToken, int applicationLifetime)
   {
      if (cfTarget == null || cfTarget.isEmpty())
      {
         throw new IllegalArgumentException("Cloud Foundry target URL may not be null or empty.");
      }
      if (cfToken == null || cfToken.isEmpty())
      {
         throw new IllegalArgumentException("Cloud Foundry secret token may not be null or empty.");
      }
      if (applicationLifetime < 1)
      {
         throw new IllegalArgumentException("Invalid application lifetime: " + 1);
      }

      this.applicationLifetime = applicationLifetime;
      this.applicationLifetimeMillis = applicationLifetime * 60 * 1000;

      this.cloudfoundry = new Cloudfoundry(new Auth(cfTarget, cfToken));
      this.applications = new CopyOnWriteArrayList<Application>();
      this.applicationTerminator = Executors.newSingleThreadScheduledExecutor();
      this.applicationTerminator.scheduleAtFixedRate(new TerminateApplicationTask(), 1, 1, TimeUnit.MINUTES);
   }

   private static String getValueParam(InitParams initParams, String name)
   {
      if (initParams != null)
      {
         ValueParam vp = initParams.getValueParam(name);
         return vp != null ? vp.getValue() : null;
      }
      return null;
   }

   private static Double parseNumber(String str, double defaultValue)
   {
      if (str != null)
      {
         try
         {
            return Double.parseDouble(str);
         }
         catch (NumberFormatException ignored)
         {
         }
      }
      return defaultValue;
   }

   @Override
   public synchronized ApplicationInstance runApplication(URL war) throws ApplicationRunnerException
   {
      // Method should be 'synchronized'. At the moment we use just one user as deployer.
      try
      {
         final String target = cloudfoundry.getTarget();
         CloudFoundryApplication cfApp = cloudfoundry.createApplication(target, generateAppName(16), "spring", null,
            1, -1, false, null, null, null, war);
         final String name = cfApp.getName();
         final long expired = System.currentTimeMillis() + applicationLifetimeMillis;
         applications.add(new Application(name, expired));
         LOG.debug("Start application {}.", name);
         return new ApplicationInstanceImpl(name, cfApp.getUris().get(0), null, applicationLifetime);
      }
      catch (CloudfoundryException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (ParsingResponseException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (VirtualFileSystemException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (IOException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
   }

   @Override
   public synchronized DebugApplicationInstance debugApplication(URL war, boolean suspend) throws ApplicationRunnerException
   {
      // Method should be 'synchronized'. At the moment we use just one user as deployer.
      try
      {
         final String target = cloudfoundry.getTarget();
         CloudFoundryApplication cfApp = cloudfoundry.createApplication(target, generateAppName(16), "spring",
            null, 1, -1, false, suspend ? new DebugMode("suspend") : new DebugMode(), null, null, war);
         final String name = cfApp.getName();
         Instance[] instances = cloudfoundry.applicationInstances(target, name, null, null);
         if (instances.length != 1)
         {
            throw new ApplicationRunnerException("Unable run application in debug mode. ");
         }
         final long expired = System.currentTimeMillis() + applicationLifetimeMillis;
         applications.add(new Application(name, expired));
         LOG.debug("Start application {} under debug.", name);
         return new DebugApplicationInstanceImpl(
            name,
            cfApp.getUris().get(0),
            null,
            applicationLifetime,
            instances[0].getDebugHost(),
            instances[0].getDebugPort()
         );
      }
      catch (CloudfoundryException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (ParsingResponseException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (VirtualFileSystemException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (IOException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
   }

   @Override
   public synchronized void stopApplication(String name) throws ApplicationRunnerException
   {
      // Method should be 'synchronized'. At the moment we use just one user as deployer.
      try
      {
         String target = cloudfoundry.getTarget();
         cloudfoundry.stopApplication(target, name, null, null);
         cloudfoundry.deleteApplication(target, name, null, null, true);
         LOG.debug("Stop application {}.", name);
      }
      catch (CloudfoundryException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (ParsingResponseException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (VirtualFileSystemException e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
      catch (IOException e)
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
      for (Application app : applications)
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

   private class TerminateApplicationTask implements Runnable
   {
      @Override
      public void run()
      {
         List<Application> stopped = new ArrayList<Application>();
         for (Application app : applications)
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
               stopped.add(app);
            }
         }
         applications.removeAll(stopped);
         LOG.debug("{} APPLICATION REMOVED", stopped.size());
      }
   }

   private static class Auth extends CloudfoundryAuthenticator
   {
      private final CloudfoundryCredentials credentials;
      private final String cfTarget;

      public Auth(String cfTarget, String cfToken)
      {
         // We do not use stored cloud foundry credentials.
         // Not need VFS, configuration, etc.
         super(null, null);
         this.cfTarget = cfTarget;
         credentials = new CloudfoundryCredentials();
         credentials.addToken(cfTarget, cfToken);
      }

      @Override
      public String readTarget() throws VirtualFileSystemException, IOException
      {
         return cfTarget;
      }

      @Override
      public CloudfoundryCredentials readCredentials() throws VirtualFileSystemException, IOException
      {
         return credentials;
      }

      @Override
      public void writeTarget(String target) throws VirtualFileSystemException, IOException
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void writeCredentials(CloudfoundryCredentials credentials) throws VirtualFileSystemException, IOException
      {
         throw new UnsupportedOperationException();
      }
   }

   private static class Application
   {
      final String name;
      final long expirationTime;

      Application(String name, long expirationTime)
      {
         this.name = name;
         this.expirationTime = expirationTime;
      }

      boolean isExpired()
      {
         return expirationTime < System.currentTimeMillis();
      }
   }
}