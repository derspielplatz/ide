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

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.ide.commons.ParsingResponseException;
import org.exoplatform.ide.extension.cloudfoundry.server.Cloudfoundry;
import org.exoplatform.ide.extension.cloudfoundry.server.CloudfoundryException;
import org.exoplatform.ide.extension.cloudfoundry.server.DebugMode;
import org.exoplatform.ide.extension.cloudfoundry.server.ext.CloudfoundryPool;
import org.exoplatform.ide.extension.cloudfoundry.shared.CloudFoundryApplication;
import org.exoplatform.ide.extension.cloudfoundry.shared.Instance;
import org.exoplatform.ide.extension.java.jdi.server.model.ApplicationInstanceImpl;
import org.exoplatform.ide.extension.java.jdi.shared.ApplicationInstance;
import org.exoplatform.ide.vfs.server.exceptions.VirtualFileSystemException;
import org.exoplatform.ide.websocket.MessageBroker;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.exoplatform.ide.commons.ContainerUtils.readValueParam;
import static org.exoplatform.ide.commons.FileUtils.*;
import static org.exoplatform.ide.commons.JsonHelper.toJson;
import static org.exoplatform.ide.commons.NameGenerator.generate;
import static org.exoplatform.ide.commons.ZipUtils.*;

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

   /** Delay (in milliseconds) before applications which will be expire soon to be checked. */
   public static final long EXPIRE_SOON_CHECKING_DELAY = 2 * 60 * 1000;

   private static final Log LOG = ExoLogger.getLogger(CloudfoundryApplicationRunner.class);

   private final int applicationLifetime;
   private final long applicationLifetimeMillis;

   private final CloudfoundryPool cfServers;

   private final Map<String, Application> applications;
   private final ScheduledExecutorService applicationTerminator;
   private final java.io.File appEngineSdk;

   /** Component for sending messages to client over WebSocket connection. */
   private static final MessageBroker messageBroker = (MessageBroker)ExoContainerContext.getCurrentContainer()
      .getComponentInstanceOfType(MessageBroker.class);

   public CloudfoundryApplicationRunner(CloudfoundryPool cfServers, InitParams initParams)
   {
      this(cfServers, parseApplicationLifeTime(readValueParam(initParams, "cloudfoundry-application-lifetime")));
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

   protected CloudfoundryApplicationRunner(CloudfoundryPool cfServers, int applicationLifetime)
   {
      if (applicationLifetime < 1)
      {
         throw new IllegalArgumentException("Invalid application lifetime: " + 1);
      }
      this.applicationLifetime = applicationLifetime;
      this.applicationLifetimeMillis = applicationLifetime * 60 * 1000;
      this.cfServers = cfServers;

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
         LOG.error("***** Google appengine Java SDK not found *****");
      }
   }

   @Override
   public ApplicationInstance runApplication(URL war, Map<String, String> params) throws ApplicationRunnerException
   {
      return startApplication(cfServers.next(), generate("app-", 16), war, null, params);
   }

   @Override
   public ApplicationInstance debugApplication(URL war, boolean suspend, Map<String, String> params)
      throws ApplicationRunnerException
   {
      return startApplication(cfServers.next(), generate("app-", 16), war,
         suspend ? new DebugMode("suspend") : new DebugMode(), params);
   }

   private ApplicationInstance startApplication(Cloudfoundry cloudfoundry,
                                                String name,
                                                URL war,
                                                DebugMode debugMode,
                                                Map<String, String> params) throws ApplicationRunnerException
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
            return doDebugApplication(cloudfoundry, name, path, debugMode, params);
         }
         return doRunApplication(cloudfoundry, name, path, params);
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
                  return doDebugApplication(cloudfoundry, name, path, debugMode, params);
               }
               return doRunApplication(cloudfoundry, name, path, params);
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

   private ApplicationInstance doRunApplication(Cloudfoundry cloudfoundry,
                                                String name,
                                                java.io.File path,
                                                Map<String, String> params) throws ApplicationRunnerException
   {
      try
      {
         final String target = cloudfoundry.getTarget();
         final CloudFoundryApplication cfApp = createApplication(cloudfoundry, target, name, path, null, params);
         final long expired = System.currentTimeMillis() + applicationLifetimeMillis;

         applications.put(name, new Application(name, target, expired));
         LOG.debug("Start application {} at CF server {}", name, target);
         return new ApplicationInstanceImpl(name, cfApp.getUris().get(0), null, applicationLifetime);
      }
      catch (Exception e)
      {
         String logs = safeGetLogs(cloudfoundry, name);

         // try to remove application.
         try
         {
            LOG.warn("Application {} failed to start, cause: {}", name, e.getMessage());
            cloudfoundry.deleteApplication(cloudfoundry.getTarget(), name, null, null, true);
         }
         catch (Exception e1)
         {
            LOG.warn("Unable delete failed application {}, cause: {}", name, e.getMessage());
         }

         throw new ApplicationRunnerException(e.getMessage(), e, logs);
      }
   }

   private ApplicationInstance doDebugApplication(Cloudfoundry cloudfoundry,
                                                       String name,
                                                       java.io.File path,
                                                       DebugMode debugMode,
                                                       Map<String, String> params) throws ApplicationRunnerException
   {
      try
      {
         final String target = cloudfoundry.getTarget();
         final CloudFoundryApplication cfApp = createApplication(cloudfoundry, target, name, path, debugMode, params);
         final long expired = System.currentTimeMillis() + applicationLifetimeMillis;

         Instance[] instances = cloudfoundry.applicationInstances(target, name, null, null);
         if (instances.length != 1)
         {
            throw new ApplicationRunnerException("Unable run application in debug mode. ");
         }

         applications.put(name, new Application(name, target, expired));
         LOG.debug("Start application {} under debug at CF server {}", name, target);
         return new ApplicationInstanceImpl(name, cfApp.getUris().get(0), null, applicationLifetime,
            instances[0].getDebugHost(), instances[0].getDebugPort());
      }
      catch (Exception e)
      {
         String logs = safeGetLogs(cloudfoundry, name);

         // try to remove application.
         try
         {
            LOG.warn("Application {} failed to start, cause: {}", name, e.getMessage());
            cloudfoundry.deleteApplication(cloudfoundry.getTarget(), name, null, null, true);
         }
         catch (Exception e1)
         {
            LOG.warn("Unable delete failed application {}, cause: {}", name, e.getMessage());
         }

         throw new ApplicationRunnerException(e.getMessage(), e, logs);
      }
   }

   @Override
   public String getLogs(String name) throws ApplicationRunnerException
   {
      Application application = applications.get(name);
      if (application != null)
      {
         Cloudfoundry cloudfoundry = cfServers.byTargetName(application.server);
         if (cloudfoundry != null)
         {
            try
            {
               return doGetLogs(cloudfoundry, name);
            }
            catch (ApplicationRunnerException e)
            {
               Throwable cause = e.getCause();
               if (cause instanceof CloudfoundryException)
               {
                  if (200 == ((CloudfoundryException)cause).getExitCode())
                  {
                     login(cloudfoundry);
                     return doGetLogs(cloudfoundry, name);
                  }
               }
               throw e;
            }
         }
         else
         {
            throw new ApplicationRunnerException("Unable get logs. Server not available. ");
         }
      }
      else
      {
         throw new ApplicationRunnerException("Unable get logs. Application '" + name + "' not found. ");
      }
   }

   private String doGetLogs(Cloudfoundry cloudfoundry, String name) throws ApplicationRunnerException
   {
      try
      {
         return cloudfoundry.getLogs(cloudfoundry.getTarget(), name, "0", null, null);
      }
      catch (Exception e)
      {
         throw new ApplicationRunnerException(e.getMessage(), e);
      }
   }

   /**
    * Get applications logs and hide any errors. This method is used for getting logs of failed application to help
    * user
    * understand what is going wrong.
    */
   private String safeGetLogs(Cloudfoundry cloudfoundry, String name)
   {
      try
      {
         return cloudfoundry.getLogs(cloudfoundry.getTarget(), name, "0", null, null);
      }
      catch (Exception e)
      {
         // Not able show log if any errors occurs.
         return null;
      }
   }

   @Override
   public void stopApplication(String name) throws ApplicationRunnerException
   {
      Application application = applications.get(name);
      if (application != null)
      {
         Cloudfoundry cloudfoundry = cfServers.byTargetName(application.server);
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
            throw new ApplicationRunnerException("Unable stop application. Server not available. ");
         }
      }
      else
      {
         throw new ApplicationRunnerException("Unable stop application. Application '" + name + "' not found. ");
      }
   }

   private void doStopApplication(Cloudfoundry cloudfoundry, String name) throws ApplicationRunnerException
   {
      try
      {
         String target = cloudfoundry.getTarget();
         cloudfoundry.stopApplication(target, name, null, null);
         cloudfoundry.deleteApplication(target, name, null, null, true);
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

   private CloudFoundryApplication createApplication(Cloudfoundry cloudfoundry,
                                                     String target,
                                                     String name,
                                                     java.io.File path,
                                                     DebugMode debug,
                                                     Map<String,String> params)
      throws CloudfoundryException, IOException, ParsingResponseException, VirtualFileSystemException
   {
      if (APPLICATION_TYPE.JAVA_WEB_APP_ENGINE == determineApplicationType(path))
      {
         if (appEngineSdk == null)
         {
            throw new RuntimeException("Unable run or debug appengine project. Google appengine Java SDK not found. ");
         }

         final java.io.File appengineApplication = createTempDirectory("gae-app-");
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

            return cloudfoundry.createApplication(target, name, "standalone", null, 1, 256, false, "java", command, debug,
               null, null, appengineApplication.toURI().toURL(), params);
         }
         finally
         {
            deleteRecursive(appengineApplication);
         }
      }
      else
      {
         return cloudfoundry.createApplication(target, name, "spring", null, 1, 256, false, "java", null, debug, null, null,
            path.toURI().toURL(), params);
      }
   }

   @Override
   public void prolongExpirationTime(String name, long time) throws ApplicationRunnerException
   {
      Application application = applications.get(name);
      if (application != null)
      {
         application.expirationTime += time;
      }
      throw new ApplicationRunnerException("Unable stop application. Application '" + name + "' not found. ");
   }

   /**
    * Pattern to get CF server name. Normally target URL of CF server is http://api.server.com and we need to get
    * server.com.
    */
   private static final Pattern serverNameGetter = Pattern.compile("(http(s)?://)?([^\\.]+)(.*)");

   @Override
   public void updateApplication(String name, URL war) throws ApplicationRunnerException
   {
      Application application = applications.get(name);
      if (application != null)
      {
         Cloudfoundry cloudfoundry = cfServers.byTargetName(application.server);
         if (cloudfoundry != null)
         {
            java.io.File sourceWar = null;
            java.io.File uploadZip = null;
            java.io.File appDir = null;
            try
            {
               sourceWar = downloadFile(null, "app-", ".war", war);

               Matcher m = serverNameGetter.matcher(application.server);
               m.matches();
               final URL url = new URL(application.server.substring(
                  0, m.start(3)) + name + application.server.substring(m.end(3)) + "/update_jrebel");

               // Get md5 hashes for remote files
               Map<String, String> remoteClassesHashes = new HashMap<String, String>();
               Map<String, String> remoteLibHashes = new HashMap<String, String>();
               Map<String, String> remoteWebHashes = new HashMap<String, String>();
               getRemoteFileHashes(url, remoteClassesHashes, remoteLibHashes, remoteWebHashes);

               appDir = createTempDirectory(name + "-update");
               unzip(sourceWar, appDir);

               // Separate application files:
               // 1. Files from WEB-INF/classes
               // 2. Files from WEB-INF/lib
               // 3. Other files. NOTE: Always skip maven files from META-INF/maven
               java.io.File classesDir = new java.io.File(appDir, "WEB-INF/classes");
               List<java.io.File> classes =
                  classesDir.exists() ? list(classesDir, null) : Collections.<java.io.File>emptyList();
               java.io.File libDir = new java.io.File(appDir, "WEB-INF/lib");
               List<java.io.File> libs = libDir.exists() ? list(libDir, null) : Collections.<java.io.File>emptyList();
               List<java.io.File> web = list(appDir, new FilenameFilter()
               {
                  @Override
                  public boolean accept(File dir, String name)
                  {
                     return !(dir.getAbsolutePath().endsWith("WEB-INF/classes")
                        || dir.getAbsolutePath().endsWith("WEB-INF/lib")
                        || dir.getAbsolutePath().endsWith("META-INF/maven"));
                  }
               });

               // Prepare digest for counting md5 hashes for local files.
               MessageDigest digest;
               try
               {
                  digest = MessageDigest.getInstance("MD5");
               }
               catch (NoSuchAlgorithmException e)
               {
                  throw new RuntimeException(e.getMessage(), e);
               }

               // Check file hashes and remove all files that are the same to remote files.
               checkFiles(classesDir, classes, remoteClassesHashes, digest);
               checkFiles(libDir, libs, remoteLibHashes, digest);
               checkFiles(appDir, web, remoteWebHashes, digest);

               // Pack to zip files that must be upload to remote server.
               uploadZip = new java.io.File(System.getProperty("java.io.tmpdir"), appDir.getName() + ".zip");
               zipDir(appDir.getAbsolutePath(), appDir, uploadZip, null);
               doUpdateApplication(url, uploadZip, remoteClassesHashes, remoteLibHashes, remoteWebHashes);
            }
            catch (IOException e)
            {
               throw new ApplicationRunnerException(e.getMessage(), e);
            }
            finally
            {
               // Cleanup create files and directories.
               if (sourceWar != null && sourceWar.exists())
               {
                  sourceWar.delete();
               }
               if (appDir != null && appDir.exists())
               {
                  deleteRecursive(appDir);
               }
               if (uploadZip != null && uploadZip.exists())
               {
                  uploadZip.delete();
               }
            }
         }
         else
         {
            throw new ApplicationRunnerException("Unable update application. Server not available. ");
         }
      }
      else
      {
         throw new ApplicationRunnerException("Unable update application. Application '" + name + "' not found. ");
      }
   }

   private void getRemoteFileHashes(URL url,
                                    Map<String, String> remoteClassesHashes,
                                    Map<String, String> remoteLibHashes,
                                    Map<String, String> remoteWebHashes) throws IOException
   {
      HttpURLConnection conn = null;
      try
      {
         conn = (HttpURLConnection)url.openConnection();
         InputStream input = conn.getInputStream();
         BufferedReader reader = new BufferedReader(new InputStreamReader(input));
         String line;
         Map<String, String> hashes = remoteClassesHashes;
         int i = 0;

         // Read response line by line. Expected response format is: md5_hash_sum relative_file_path
         // Empty line separate list for three groups:
         // 1. Files from WEB-INF/classes
         // 2. Files from WEB-INF/lib
         // 3. Other files
         //
         // Here is example of remote server response:
         // 83d230901f5f18eb8804aa029e1094df helloworld/GreetingController.class
         // ...
         // [blank line]
         // c49fbf1401117f2a7de32a0e29309600 spring-web-3.0.5.RELEASE.jar
         // ...
         // [blank line]
         // 23d647f59023c61b67df7d086e75bd39 index.jsp
         // ...

         while ((line = reader.readLine()) != null && i < 3)
         {
            if (line.isEmpty())
            {
               i++;
               switch (i)
               {
                  case 1:
                     hashes = remoteLibHashes;
                     break;
                  case 2:
                     hashes = remoteWebHashes;
                     break;
               }
               continue;
            }
            String hash = line.substring(0, 32); // Length of MD-5 hash sum
            String relPath = line.substring(33);
            hashes.put(relPath, hash);
         }
         input.close();
      }
      finally
      {
         if (conn != null)
         {
            conn.disconnect();
         }
      }
   }

   private void checkFiles(java.io.File baseDir,
                           List<java.io.File> files,
                           Map<String, String> remoteFilesHashes,
                           MessageDigest digest) throws IOException
   {
      int relPathOffset = baseDir.getAbsolutePath().length() + 1;
      for (java.io.File f : files)
      {
         String relPath = f.getAbsolutePath().substring(relPathOffset);
         if (remoteFilesHashes.containsKey(relPath))
         {
            digest.reset();
            if (remoteFilesHashes.get(relPath).equals(countFileHash(f, digest)))
            {
               // Delete file in hashes are the same.
               f.delete();
            }
            remoteFilesHashes.remove(relPath);
         }
      }
   }

   private void doUpdateApplication(URL url, java.io.File zip,
                                    Map<String, String> remoteClassesHashes,
                                    Map<String, String> remoteLibHashes,
                                    Map<String, String> remoteWebHashes) throws IOException
   {
      HttpURLConnection conn = null;
      try
      {
         conn = (HttpURLConnection)url.openConnection();
         conn.setRequestMethod("POST");
         conn.setRequestProperty("content-type", "application/zip");
         conn.setRequestProperty("content-length", Long.toString(zip.length()));
         // Send lists of files that should be removed in request headers.
         conn.setRequestProperty("x-exo-ide-classes-delete", remoteClassesHashes.keySet().toString());
         conn.setRequestProperty("x-exo-ide-lib-delete", remoteLibHashes.keySet().toString());
         conn.setRequestProperty("x-exo-ide-web-delete", remoteWebHashes.keySet().toString());
         //
         conn.setDoOutput(true);
         byte[] buf = new byte[8192];
         int r;
         InputStream zipIn = new FileInputStream(zip);
         OutputStream out = conn.getOutputStream();
         try
         {
            while ((r = zipIn.read(buf)) != -1)
            {
               out.write(buf, 0, r);
            }
         }
         finally
         {
            zipIn.close();
            out.close();
         }
         conn.getResponseCode();
      }
      finally
      {
         if (conn != null)
         {
            conn.disconnect();
         }
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
         cloudfoundry.login();
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
         List<String> expireSoon = new ArrayList<String>();
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
            else if (app.expiresAfter(EXPIRE_SOON_CHECKING_DELAY))
            {
               expireSoon.add(app.name);
            }
         }
         applications.keySet().removeAll(stopped);
         LOG.debug("{} applications removed. ", stopped.size());

         if (!expireSoon.isEmpty())
         {
            publishWebSocketMessage(toJson(expireSoon), null);
         }
      }
   }

   private static class Application
   {
      final String name;
      final String server;
      long expirationTime;

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

      boolean expiresAfter(long delay)
      {
         return expirationTime - System.currentTimeMillis() <= delay;
      }
   }

   /**
    * Publishes the message over WebSocket connection.
    *
    * @param data
    *    the data to be sent to the client
    * @param e
    *    an exception to be sent to the client
    */
   private void publishWebSocketMessage(String data, Exception e)
   {
      messageBroker.publish(MessageBroker.Channels.DEBUGGER_EXPIRE_SOON_APPS.toString(), data, e, null);
   }
}