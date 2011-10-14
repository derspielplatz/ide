/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.ide.extension.samples.client.wizard.finish;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.ui.HasValue;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.rest.copy.AsyncRequestCallback;
import org.exoplatform.gwtframework.ui.client.dialog.Dialogs;
import org.exoplatform.ide.client.framework.event.RefreshBrowserEvent;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.output.event.OutputEvent;
import org.exoplatform.ide.client.framework.output.event.OutputMessage;
import org.exoplatform.ide.client.framework.output.event.OutputMessage.Type;
import org.exoplatform.ide.client.framework.ui.api.IsView;
import org.exoplatform.ide.client.framework.ui.api.View;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler;
import org.exoplatform.ide.extension.jenkins.client.event.ApplicationBuiltEvent;
import org.exoplatform.ide.extension.jenkins.client.event.ApplicationBuiltHandler;
import org.exoplatform.ide.extension.jenkins.client.event.BuildApplicationEvent;
import org.exoplatform.ide.extension.samples.client.ProjectProperties;
import org.exoplatform.ide.extension.samples.client.SamplesClientService;
import org.exoplatform.ide.extension.samples.client.SamplesExtension;
import org.exoplatform.ide.extension.samples.client.SamplesLocalizationConstant;
import org.exoplatform.ide.extension.samples.client.paas.cloudbees.CloudBeesAsyncRequestCallback;
import org.exoplatform.ide.extension.samples.client.paas.cloudfoundry.CloudFoundryAsyncRequestCallback;
import org.exoplatform.ide.extension.samples.client.paas.cloudfoundry.CloudfoundryApplication;
import org.exoplatform.ide.extension.samples.client.paas.login.LoggedInHandler;
import org.exoplatform.ide.extension.samples.client.wizard.deployment.ShowWizardDeploymentStepEvent;
import org.exoplatform.ide.extension.samples.client.wizard.event.ProjectCreationFinishedEvent;
import org.exoplatform.ide.git.client.GitPresenter;
import org.exoplatform.ide.vfs.client.VirtualFileSystem;
import org.exoplatform.ide.vfs.client.marshal.ProjectUnmarshaller;
import org.exoplatform.ide.vfs.client.model.FolderModel;
import org.exoplatform.ide.vfs.client.model.ItemContext;
import org.exoplatform.ide.vfs.client.model.ProjectModel;
import org.exoplatform.ide.vfs.shared.Folder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Presenter for Step1 (Source) of Wizard for creation Java Project.
 * 
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: SourceWizardPresenter.java Sep 7, 2011 3:00:58 PM vereshchaka $
 */
public class WizardFinishStepPresenter extends GitPresenter  implements ShowWizardFinishStepHandler, ViewClosedHandler, 
ApplicationBuiltHandler
{
   public interface Display extends IsView
   {
      HasClickHandlers getFinishButton();
      
      HasClickHandlers getBackButton();
      
      HasClickHandlers getCancelButton();
      
      HasValue<String> getNameLabel();
      
      HasValue<String> getTypeLabel();
      
      HasValue<String> getPaasLabel();
   }
   
   private static final SamplesLocalizationConstant lb = SamplesExtension.LOCALIZATION_CONSTANT;
   
   private Display display;
   
   /**
    * Project properties.
    * Got from previous step.
    */
   private ProjectProperties projectProperties;
   
   /**
    * The URL to war file of built application.
    */
   private String warUrl;
   
   private ProjectModel project;
   
   public WizardFinishStepPresenter(HandlerManager eventBus)
   {
      super(eventBus);
      
      eventBus.addHandler(ShowWizardFinishStepEvent.TYPE, this);
      eventBus.addHandler(ViewClosedEvent.TYPE, this);
      eventBus.addHandler(ApplicationBuiltEvent.TYPE, this);
   }
   
   private void bindDisplay()
   {
      display.getCancelButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            eventBus.fireEvent(new ProjectCreationFinishedEvent(true));
            closeView();
         }
      });
      
      display.getBackButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            eventBus.fireEvent(new ShowWizardDeploymentStepEvent());
            closeView();
         }
      });
      
      display.getFinishButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            if (projectProperties == null)
            {
               Dialogs.getInstance().showError(lb.wizardFinishErrorProjectPropertiesAreNull());
               return;
            }
            finishProjectCreation();
         }
      });
      
      if (projectProperties != null)
      {
         display.getNameLabel().setValue(projectProperties.getName());
         display.getTypeLabel().setValue(projectProperties.getType());
         if (ProjectProperties.Paas.CLOUDBEES.equals(projectProperties.getPaas()))
         {
            display.getPaasLabel().setValue(lb.wizardFinishDeploymentCloudBees());
         }
         else if (ProjectProperties.Paas.CLOUDFOUNDRY.equals(projectProperties.getPaas()))
         {
            display.getPaasLabel().setValue(lb.wizardFinishDeploymentCloudFoundry());
         }
         else
         {
            display.getPaasLabel().setValue(lb.wizardFinishDeploymentNone());
         }
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler#onViewClosed(org.exoplatform.ide.client.framework.ui.api.event.ViewClosedEvent)
    */
   @Override
   public void onViewClosed(ViewClosedEvent event)
   {
      if (event.getView() instanceof Display)
      {
         display = null;
      }
   }

   /**
    * @see org.exoplatform.ide.extension.samples.client.wizard.source.ShowWizardSourceHandler#onShowWizardDefinition(org.exoplatform.ide.extension.samples.client.wizard.source.ShowWizardEvent)
    */
   @Override
   public void onShowFinishWizard(ShowWizardFinishStepEvent event)
   {
      if (event.getProjectProperties() != null)
      {
         projectProperties = event.getProjectProperties();
      }
      else
      {
         Dialogs.getInstance().showError("Project properties are null. Fix the error");
         return;
      }
      openView();
   }
   
   /**
    * @see org.exoplatform.ide.extension.jenkins.client.event.ApplicationBuiltHandler#onApplicationBuilt(org.exoplatform.ide.extension.jenkins.client.event.ApplicationBuiltEvent)
    */
   @Override
   public void onApplicationBuilt(ApplicationBuiltEvent event)
   {
      eventBus.removeHandler(event.getAssociatedType(), this);
      if (event.getJobStatus().getArtifactUrl() != null)
      {
         warUrl = event.getJobStatus().getArtifactUrl();
         deployApplication();
      }
   }
   
   private void openView()
   {
      if (display == null)
      {
         Display d = GWT.create(Display.class);
         IDE.getInstance().openView((View)d);
         display = d;
         bindDisplay();
         return;
      }
      else
      {
         eventBus.fireEvent(new ExceptionThrownEvent("Show Wizard must be null"));
      }
   }
   
   private void closeView()
   {
      IDE.getInstance().closeView(display.asView().getId());
   }
   
   private void finishProjectCreation()
   {
      try
      {
         final FolderModel parent = projectProperties.getParenFolder();
         ProjectModel newProject = new ProjectModel(projectProperties.getName(), parent, projectProperties.getType(), 
            Collections.EMPTY_LIST);
         VirtualFileSystem.getInstance().createProject(parent, new AsyncRequestCallback<ProjectModel>(
                  new ProjectUnmarshaller(newProject))
         {
            
            @Override
            protected void onSuccess(ProjectModel result)
            {
               if (!ProjectProperties.Paas.NONE.equals(projectProperties.getPaas()))
               {
                  project = result;
                  buildApplication(project);
               }
               //TODO check this works:
               eventBus.fireEvent(new RefreshBrowserEvent(getFoldersToRefresh(parent), parent));
             //  eventBus.fireEvent(new RefreshBrowserEvent(parent));
               eventBus.fireEvent(new ProjectCreationFinishedEvent(false));
               closeView();
            }
            
            @Override
            protected void onFailure(Throwable exception)
            {
               exception.printStackTrace();
               eventBus.fireEvent(new ExceptionThrownEvent(exception, lb.wizardFinishErrorCantCreateProject()));
            }
         });
      }
      catch (RequestException e)
      {
         e.printStackTrace();
         eventBus.fireEvent(new ExceptionThrownEvent(e,
            "Service is not deployed.<br>Destination path does not exist<br>Folder already has item with same name."));
      }
      
   }
   
   /**
    * Work up to the root folder to create a list of folder to refresh.
    * Need to refresh all folders, that were created during "Select Location"
    * step, but not displayed in main navigation tree.
    * 
    * @param folder - the parent folder of your project
    * @return
    */
   private ArrayList<Folder> getFoldersToRefresh(Folder folder)
   {
      ArrayList<Folder> folders = new ArrayList<Folder>();
      folders.add(0, folder);
      Folder parent = ((ItemContext)folder).getParent();
      while (parent != null)
      {
         folders.add(0, parent);
         parent = ((ItemContext)parent).getParent();
      }
      return folders;
   }
   
   private LoggedInHandler deployToCloudBeesLoggedInHandler = new LoggedInHandler()
   {
      @Override
      public void onLoggedIn()
      {
         deployToCloudBees();
      }
   };
   
   private void deployToCloudBees()
   {
      final String applicationId =
         projectProperties.getProperties().get("cf-name") + "/" + projectProperties.getProperties().get("domain");
      
      SamplesClientService.getInstance().createCloudBeesApplication(applicationId, vfs.getId(), project.getId(), warUrl, null, new CloudBeesAsyncRequestCallback<Map<String, String>>(eventBus, deployToCloudBeesLoggedInHandler)
         {
            @Override
            protected void onSuccess(final Map<String, String> deployResult)
            {
               String output = lb.cloudBessDeploySuccess() + "<br>";
               output += lb.cloudBeesDeployApplicationInfo() + "<br>";

               Iterator<Entry<String, String>> it = deployResult.entrySet().iterator();
               while (it.hasNext())
               {
                  Entry<String, String> entry = (Entry<String, String>)it.next();
                  output += entry.getKey() + " : " + entry.getValue() + "<br>";
               }
               eventBus.fireEvent(new OutputEvent(output, Type.INFO));
               projectProperties = null;
            }

            /**
             * @see org.exoplatform.ide.extension.cloudbees.client.CloudBeesAsyncRequestCallback#onFailure(java.lang.Throwable)
             */
            @Override
            protected void onFailure(Throwable exception)
            {
               eventBus.fireEvent(new OutputEvent(lb.cloudBeesDeployFailure(), Type.INFO));
               projectProperties = null;
               super.onFailure(exception);
            }

         });
   }
   
   private LoggedInHandler deployToCloudFoundryLoggedInHandler = new LoggedInHandler()
   {
      @Override
      public void onLoggedIn()
      {
         deployToCloudBees();
      }
   };
   
   private void deployToCloudFoundry()
   {
      String name = projectProperties.getProperties().get("cf-name");
      String url = projectProperties.getProperties().get("url");
      String server = projectProperties.getProperties().get("target");
      
      SamplesClientService.getInstance().createCloudFoundryApplication(server, name, url, project.getPath(),
         warUrl, new CloudFoundryAsyncRequestCallback<CloudfoundryApplication>(eventBus, deployToCloudFoundryLoggedInHandler)
         {
            @Override
            protected void onSuccess(CloudfoundryApplication result)
            {
               String msg = lb.cloudFoundryDeploySuccess(result.getName());
               if ("STARTED".equals(result.getState()))
               {
                  if (result.getUris().isEmpty())
                  {
                     msg += "<br>" + lb.cloudFoundryApplicationStartedWithNoUrls();
                  }
                  else
                  {
                     msg +=
                        "<br>" + lb.cloudFoundryApplicationStartedOnUrls(result.getName(), getAppUrlsAsString(result));
                  }
               }
               eventBus.fireEvent(new OutputEvent(msg, OutputMessage.Type.INFO));
               projectProperties = null;
            }

            @Override
            protected void onFailure(Throwable exception)
            {
               eventBus.fireEvent(new OutputEvent(lb.cloudFoundryDeployFailure(), Type.INFO));
               projectProperties = null;
               super.onFailure(exception);
            }
         });
   }
   
   private void buildApplication(ProjectModel projectModel)
   {
      eventBus.addHandler(ApplicationBuiltEvent.TYPE, this);
      eventBus.fireEvent(new BuildApplicationEvent(projectModel));
   }

   /**
    * Deploty application to the paas, that was selected in previous step.
    */
   private void deployApplication()
   {
      if (ProjectProperties.Paas.CLOUDBEES.equals(projectProperties.getPaas()))
      {
         deployToCloudBees();
      }
      else if (ProjectProperties.Paas.CLOUDFOUNDRY.equals(projectProperties.getPaas()))
      {
         deployToCloudFoundry();
      }
   }
   
   private String getAppUrlsAsString(CloudfoundryApplication application)
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
         //crop unnecessary symbols
         appUris = appUris.substring(2);
      }
      return appUris;
   }
   
}
