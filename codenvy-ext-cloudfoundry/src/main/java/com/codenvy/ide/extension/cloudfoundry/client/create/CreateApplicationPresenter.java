/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package com.codenvy.ide.extension.cloudfoundry.client.create;

import com.codenvy.ide.api.resources.ResourceProvider;
import com.codenvy.ide.api.ui.console.Console;
import com.codenvy.ide.commons.exception.ExceptionThrownEvent;
import com.codenvy.ide.core.event.RefreshBrowserEvent;
import com.codenvy.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback;
import com.codenvy.ide.extension.cloudfoundry.client.CloudFoundryClientService;
import com.codenvy.ide.extension.cloudfoundry.client.CloudFoundryExtension;
import com.codenvy.ide.extension.cloudfoundry.client.CloudFoundryLocalizationConstant;
import com.codenvy.ide.extension.cloudfoundry.client.login.LoggedInHandler;
import com.codenvy.ide.extension.cloudfoundry.client.marshaller.FrameworksUnmarshaller;
import com.codenvy.ide.extension.cloudfoundry.client.marshaller.TargetsUnmarshaller;
import com.codenvy.ide.extension.cloudfoundry.shared.CloudFoundryApplication;
import com.codenvy.ide.extension.cloudfoundry.shared.Framework;
import com.codenvy.ide.extension.maven.client.event.BuildProjectEvent;
import com.codenvy.ide.extension.maven.client.event.ProjectBuiltEvent;
import com.codenvy.ide.extension.maven.client.event.ProjectBuiltHandler;
import com.codenvy.ide.json.JsonArray;
import com.codenvy.ide.resources.model.Project;
import com.codenvy.ide.resources.model.Resource;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.AutoBeanUnmarshaller;
import com.codenvy.ide.websocket.rest.AutoBeanUnmarshallerWS;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 
 * @author <a href="mailto:aplotnikov@exoplatform.com">Andrey Plotnikov</a>
 */
@Singleton
public class CreateApplicationPresenter implements CreateApplicationView.ActionDelegate, CreateApplicationHandler,
   ProjectBuiltHandler
{
   private class AppData
   {
      String server;

      String name;

      String type;

      String url;

      int instances;

      int memory;

      boolean nostart;

      public AppData(String server, String name, String type, String url, int instances, int memory, boolean nostart)
      {
         this.server = server;
         this.name = name;
         this.type = type;
         this.url = url;
         this.instances = instances;
         this.memory = memory;
         this.nostart = nostart;
      }
   }

   private CreateApplicationView view;

   private List<Framework> frameworks;

   /**
    * Public url to war file of application.
    */
   private String warUrl;

   /**
    * Store application data in format, that convenient to send to server.
    */
   private AppData appData;

   /**
    * 
    */
   private boolean isMavenProject;

   private ResourceProvider resourceProvider;

   private HandlerRegistration projectBuildHandler;

   private EventBus eventBus;

   private Console console;

   private CloudFoundryLocalizationConstant constant;

   /**
    * Create presenter.
    * 
    * @param resourceProvider
    * @param view
    * @param eventBus
    * @param console
    * @param constant
    */
   @Inject
   protected CreateApplicationPresenter(ResourceProvider resourceProvider, CreateApplicationView view,
      EventBus eventBus, Console console, CloudFoundryLocalizationConstant constant)
   {
      this.frameworks = new ArrayList<Framework>();

      this.resourceProvider = resourceProvider;
      this.view = view;
      this.view.setDelegate(this);
      this.console = console;
      this.eventBus = eventBus;
      this.constant = constant;

      this.eventBus.addHandler(CreateApplicationEvent.TYPE, this);
   }

   @Override
   public void doCreate()
   {
      appData = getAppDataFromForm();

      validateData(appData);
   }

   /**
    * Process values from application create form, and store data in bean in format, that is convenient to send to server
    * 
    * @return {@link AppData}
    */
   private AppData getAppDataFromForm()
   {
      String server = view.getServer();
      if (server == null || server.isEmpty())
      {
         // is server is empty, set value to null
         // it is need for client service
         // if null, than service will not send this parameter
         server = null;
      }
      else if (server.endsWith("/"))
      {
         server = server.substring(0, server.length() - 1);
      }
      String name = view.getName();
      String type;
      int memory = 0;
      if (view.isAutodetectType())
      {
         type = null;
         memory = 0;
      }
      else
      {
         Framework framework = findFrameworkByName(view.getType());
         type = framework.getName();
         try
         {
            memory = Integer.parseInt(view.getMemory());
         }
         catch (NumberFormatException e)
         {
            eventBus.fireEvent(new ExceptionThrownEvent(constant.errorMemoryFormat()));
            console.print(constant.errorMemoryFormat());
         }
      }

      String url;

      if (view.isCustomUrl())
      {
         url = view.getUrl();
         if (url == null || url.isEmpty())
         {
            url = null;
         }
      }
      else
      {
         url = null;
      }

      int instances = 0;
      try
      {
         instances = Integer.parseInt(view.getInstances());
      }
      catch (NumberFormatException e)
      {
         eventBus.fireEvent(new ExceptionThrownEvent(constant.errorInstancesFormat()));
         console.print(constant.errorInstancesFormat());
      }
      boolean nostart = !view.isStartAfterCreation();

      return new AppData(server, name, type, url, instances, memory, nostart);
   }

   /**
    * Check is selected item project and can be built.
    */
   private void checkIsProject(final Project project)
   {
      JsonArray<Resource> children = project.getChildren();

      isMavenProject = false;
      for (int i = 0; i < children.size(); i++)
      {
         Resource child = children.get(i);
         if (child.isFile() && "pom.xml".equals(child.getName()))
         {
            isMavenProject = true;
         }
      }
   }

   /**
    * Find framework from list by name.
    * 
    * @param frameworkName
    * @return
    */
   private Framework findFrameworkByName(String frameworkName)
   {
      for (int i = 0; i < frameworks.size(); i++)
      {
         Framework framework = frameworks.get(i);
         String name = framework.getDisplayName() != null ? framework.getDisplayName() : framework.getName();
         if (frameworkName.equals(name))
         {
            return framework;
         }
      }
      return null;
   }

   private void validateData(final AppData app)
   {
      LoggedInHandler validateHandler = new LoggedInHandler()
      {
         @Override
         public void onLoggedIn()
         {
            validateData(app);
         }
      };

      Project project = resourceProvider.getActiveProject();

      try
      {
         CloudFoundryClientService.getInstance().validateAction("create", app.server, app.name, app.type, app.url,
            resourceProvider.getVfsId(), project.getId(), app.instances, app.memory, app.nostart,
            new CloudFoundryAsyncRequestCallback<String>(null, validateHandler, null, app.server, eventBus, console,
               constant)
            {
               @Override
               protected void onSuccess(String result)
               {
                  if (isMavenProject)
                  {
                     buildApplication();
                  }
                  else
                  {
                     createApplication(appData);
                  }

                  view.close();
               }
            });
      }
      catch (RequestException e)
      {
         eventBus.fireEvent(new ExceptionThrownEvent(e));
         console.print(e.getMessage());
      }
   }

   private void buildApplication()
   {
      projectBuildHandler = eventBus.addHandler(ProjectBuiltEvent.TYPE, this);
      eventBus.fireEvent(new BuildProjectEvent());
   }

   /**
    * Create application on CloudFoundry by sending request over WebSocket or HTTP.
    * 
    * @param appData data to create new application
    */
   private void createApplication(final AppData appData)
   {
      LoggedInHandler loggedInHandler = new LoggedInHandler()
      {
         @Override
         public void onLoggedIn()
         {
            createApplication(appData);
         }
      };

      final Project project = resourceProvider.getActiveProject();

      AutoBean<CloudFoundryApplication> cloudFoundryApplication =
         CloudFoundryExtension.AUTO_BEAN_FACTORY.cloudFoundryApplication();
      AutoBeanUnmarshallerWS<CloudFoundryApplication> unmarshaller =
         new AutoBeanUnmarshallerWS<CloudFoundryApplication>(cloudFoundryApplication);

      // TODO This code uses WebSocket but we have not ported it yet.
      //      try
      //      {
      //         CloudFoundryClientService.getInstance().createWS(
      //            appData.server,
      //            appData.name,
      //            appData.type,
      //            appData.url,
      //            appData.instances,
      //            appData.memory,
      //            appData.nostart,
      //            resourceProvider.getVfsId(),
      //            project.getId(),
      //            warUrl,
      //            new CloudFoundryRESTfulRequestCallback<CloudFoundryApplication>(unmarshaller, loggedInHandler, null,
      //               appData.server, eventBus, console, constant)
      //            {
      //               @Override
      //               protected void onSuccess(CloudFoundryApplication result)
      //               {
      //                  onAppCreatedSuccess(result);
      //                  eventBus.fireEvent(new RefreshBrowserEvent(project));
      //               }
      //
      //               @Override
      //               protected void onFailure(Throwable exception)
      //               {
      //                  console.print(constant.applicationCreationFailed());
      //                  super.onFailure(exception);
      //               }
      //            });
      //      }
      //      catch (WebSocketException e)
      //      {
      //         createApplicationREST(appData, project, loggedInHandler);
      //      }
      createApplicationREST(appData, project, loggedInHandler);
   }

   /**
    * Create application on CloudFoundry by sending request over HTTP.
    * 
    * @param appData data to create new application
    * @param project {@link Project}
    * @param loggedInHandler handler that should be called after success login
    */
   private void createApplicationREST(final AppData appData, final Project project, LoggedInHandler loggedInHandler)
   {
      AutoBean<CloudFoundryApplication> cloudFoundryApplication =
         CloudFoundryExtension.AUTO_BEAN_FACTORY.cloudFoundryApplication();
      AutoBeanUnmarshaller<CloudFoundryApplication> unmarshaller =
         new AutoBeanUnmarshaller<CloudFoundryApplication>(cloudFoundryApplication);

      try
      {
         CloudFoundryClientService.getInstance().create(
            appData.server,
            appData.name,
            appData.type,
            appData.url,
            appData.instances,
            appData.memory,
            appData.nostart,
            resourceProvider.getVfsId(),
            project.getId(),
            warUrl,
            new CloudFoundryAsyncRequestCallback<CloudFoundryApplication>(unmarshaller, loggedInHandler, null,
               appData.server, eventBus, console, constant)
            {
               @Override
               protected void onSuccess(final CloudFoundryApplication cloudFoundryApp)
               {
                  project.refreshProperties(new AsyncCallback<Project>()
                  {
                     @Override
                     public void onSuccess(Project result)
                     {
                        onAppCreatedSuccess(cloudFoundryApp);
                        eventBus.fireEvent(new RefreshBrowserEvent(project));
                     }
                     
                     @Override
                     public void onFailure(Throwable caught)
                     {
                     }
                  });
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  console.print(constant.applicationCreationFailed());
                  super.onFailure(exception);
               }
            });
      }
      catch (RequestException e)
      {
         console.print(constant.applicationCreationFailed());
      }
   }

   /**
    * Performs action when application successfully created.
    * 
    * @param app {@link CloudFoundryApplication} which is created
    */
   private void onAppCreatedSuccess(CloudFoundryApplication app)
   {
      warUrl = null;

      if ("STARTED".equals(app.getState()) && app.getInstances() == app.getRunningInstances())
      {
         String msg = constant.applicationCreatedSuccessfully(app.getName());
         if (app.getUris().isEmpty())
         {
            msg += "<br>" + constant.applicationStartedWithNoUrls();
         }
         else
         {
            msg += "<br>" + constant.applicationStartedOnUrls(app.getName(), getAppUrlsAsString(app));
         }
         console.print(msg);
      }
      else if ("STARTED".equals(app.getState()) && app.getInstances() != app.getRunningInstances())
      {
         String msg = constant.applicationWasNotStarted(app.getName());
         console.print(msg);
      }
      else
      {
         String msg = constant.applicationCreatedSuccessfully(app.getName());
         console.print(msg);
      }
   }

   private String getAppUrlsAsString(CloudFoundryApplication application)
   {
      String appUris = "";
      for (String uri : application.getUris())
      {
         if (!uri.startsWith("http"))
         {
            uri = "http://" + uri;
         }
         appUris += ", " + "<a href=\"" + uri + "\" target=\"_blank\">" + uri + "</a>";
      }
      if (!appUris.isEmpty())
      {
         // crop unnecessary symbols
         appUris = appUris.substring(2);
      }
      return appUris;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void doCancel()
   {
      view.close();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onAutoDetectTypeChanged()
   {
      boolean value = view.isAutodetectType();

      view.enableTypeField(!value);
      view.enableMemoryField(!value);

      if (value)
      {
         view.setTypeValues(new ArrayList<String>());
         view.setMemory("");
      }
      else
      {
         final List<String> frameworkArray = getApplicationTypes(frameworks);
         view.setTypeValues(frameworkArray);
         view.setSelectedIndexForTypeSelectItem(0);
         getFrameworks(view.getServer());
      }
   }

   /**
    * Get the array of application types from list of frameworks.
    * 
    * @param frameworks - list of available frameworks
    * @return an array of types
    */
   private List<String> getApplicationTypes(List<Framework> frameworks)
   {
      List<String> frameworkNames = new ArrayList<String>();
      for (Framework framework : frameworks)
      {
         frameworkNames.add(framework.getDisplayName() != null ? framework.getDisplayName() : framework.getName());
      }

      return frameworkNames;
   }

   private void getFrameworks(final String server)
   {
      LoggedInHandler getFrameworksLoggedInHandler = new LoggedInHandler()
      {
         @Override
         public void onLoggedIn()
         {
            getFrameworks(server);
         }
      };

      try
      {
         CloudFoundryClientService.getInstance().getFrameworks(
            new CloudFoundryAsyncRequestCallback<List<Framework>>(
               new FrameworksUnmarshaller(new ArrayList<Framework>()), getFrameworksLoggedInHandler, null, eventBus,
               console, constant)
            {
               @Override
               protected void onSuccess(List<Framework> result)
               {
                  if (!result.isEmpty())
                  {
                     frameworks = result;
                     List<String> fw = getApplicationTypes(result);
                     view.setTypeValues(fw);
                     Framework framework = findFrameworkByName(fw.get(0));
                     view.setMemory(String.valueOf(framework.getMemory()));
                  }
               }
            }, server);
      }
      catch (RequestException e)
      {
         eventBus.fireEvent(new ExceptionThrownEvent(e));
         console.print(e.getMessage());
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onCustomUrlChanged()
   {
      boolean value = view.isCustomUrl();

      view.enableUrlField(value);

      if (value)
      {
         view.focusInUrlField();
      }
      else
      {
         updateUrlField();
      }
   }

   /**
    * Update the URL field, using values from server and name field.
    */
   private void updateUrlField()
   {
      final String url = getUrlByServerAndName(view.getServer(), view.getName());
      view.setUrl(url);
   }

   private String getUrlByServerAndName(String serverUrl, String name)
   {
      int index = serverUrl.indexOf(".");
      if (index < 0)
      {
         return name.toLowerCase();
      }
      final String domain = serverUrl.substring(index, serverUrl.length());
      return "http://" + name.toLowerCase() + domain;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onApplicationNameChanged()
   {
      // if url set automatically, than try to create url using server and name
      if (!view.isCustomUrl())
      {
         updateUrlField();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void serverChanged()
   {
      // if url set automatically, than try to create url using server and name
      if (!view.isCustomUrl())
      {
         updateUrlField();
         view.enableAutodetectTypeCheckItem(true);
      }
   }

   /**
    * Shows dialog.
    */
   public void showDialog()
   {
      // set the state of fields
      this.view.enableTypeField(false);
      this.view.enableUrlField(false);
      this.view.enableMemoryField(false);
      this.view.focusInNameField();

      // set default values to fields
      this.view.setTypeValues(new ArrayList<String>());
      this.view.setInstances("1");
      this.view.setAutodetectType(true);

      view.focusInNameField();
      getServers();
      view.setStartAfterCreation(true);

      view.showDialog();
   }

   /**
    * Get the list of server and put them to select field.
    */
   private void getServers()
   {
      try
      {
         CloudFoundryClientService.getInstance().getTargets(
            new AsyncRequestCallback<List<String>>(new TargetsUnmarshaller(new ArrayList<String>()))
            {
               @Override
               protected void onSuccess(List<String> result)
               {
                  if (result.isEmpty())
                  {
                     List<String> list = new ArrayList<String>();
                     list.add(CloudFoundryExtension.DEFAULT_SERVER);
                     view.setServerValues(list);
                     view.setServer(CloudFoundryExtension.DEFAULT_SERVER);
                  }
                  else
                  {
                     view.setServerValues(result);
                     view.setServer(result.get(0));
                     getFrameworks(result.get(0));
                  }
                  view.setName(resourceProvider.getActiveProject().getName());
                  updateUrlField();
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  eventBus.fireEvent(new ExceptionThrownEvent(exception));
                  console.print(exception.getMessage());
               }
            });
      }
      catch (RequestException e)
      {
         eventBus.fireEvent(new ExceptionThrownEvent(e));
         console.print(e.getMessage());
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onCreateApplication(CreateApplicationEvent event)
   {
      Project selectedProject = resourceProvider.getActiveProject();
      if (selectedProject != null)
      {
         checkIsProject(selectedProject);
         showDialog();
      }
      else
      {
         String msg = constant.createApplicationNotFolder(view.getName());
         eventBus.fireEvent(new ExceptionThrownEvent(msg));
         console.print(msg);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onTypeChanged()
   {
      Framework framework = findFrameworkByName(view.getType());
      if (framework != null)
      {
         view.setMemory(String.valueOf(framework.getMemory()));
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onProjectBuilt(ProjectBuiltEvent event)
   {
      projectBuildHandler.removeHandler();
      if (event.getBuildStatus().getDownloadUrl() != null)
      {
         warUrl = event.getBuildStatus().getDownloadUrl();
         createApplication(appData);
      }
   }
}