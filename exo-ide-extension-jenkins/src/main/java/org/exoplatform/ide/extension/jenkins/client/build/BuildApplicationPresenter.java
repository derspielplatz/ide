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
package org.exoplatform.ide.extension.jenkins.client.build;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Image;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.gwtframework.commons.rest.AutoBeanUnmarshaller;
import org.exoplatform.gwtframework.commons.rest.RequestStatusHandler;
import org.exoplatform.gwtframework.ui.client.dialog.Dialogs;
import org.exoplatform.ide.client.framework.event.RefreshBrowserEvent;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.output.event.OutputEvent;
import org.exoplatform.ide.client.framework.output.event.OutputMessage.Type;
import org.exoplatform.ide.client.framework.ui.api.IsView;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler;
import org.exoplatform.ide.client.framework.userinfo.UserInfo;
import org.exoplatform.ide.client.framework.userinfo.event.UserInfoReceivedEvent;
import org.exoplatform.ide.client.framework.userinfo.event.UserInfoReceivedHandler;
import org.exoplatform.ide.client.framework.websocket.MessageBus.Channels;
import org.exoplatform.ide.client.framework.websocket.WebSocket;
import org.exoplatform.ide.client.framework.websocket.WebSocketEventHandler;
import org.exoplatform.ide.client.framework.websocket.WebSocketException;
import org.exoplatform.ide.client.framework.websocket.messages.WebSocketEventMessage;
import org.exoplatform.ide.extension.jenkins.client.JenkinsExtension;
import org.exoplatform.ide.extension.jenkins.client.JenkinsService;
import org.exoplatform.ide.extension.jenkins.client.JobResult;
import org.exoplatform.ide.extension.jenkins.client.event.ApplicationBuiltEvent;
import org.exoplatform.ide.extension.jenkins.client.event.BuildApplicationEvent;
import org.exoplatform.ide.extension.jenkins.client.event.BuildApplicationHandler;
import org.exoplatform.ide.extension.jenkins.client.marshal.StringContentUnmarshaller;
import org.exoplatform.ide.extension.jenkins.shared.Job;
import org.exoplatform.ide.extension.jenkins.shared.JobStatus;
import org.exoplatform.ide.extension.jenkins.shared.JobStatusBean.Status;
import org.exoplatform.ide.git.client.GitClientService;
import org.exoplatform.ide.git.client.GitExtension;
import org.exoplatform.ide.git.client.GitPresenter;
import org.exoplatform.ide.git.client.init.InitRequestStatusHandler;
import org.exoplatform.ide.vfs.client.VirtualFileSystem;
import org.exoplatform.ide.vfs.client.marshal.ChildrenUnmarshaller;
import org.exoplatform.ide.vfs.client.model.ItemContext;
import org.exoplatform.ide.vfs.client.model.ProjectModel;
import org.exoplatform.ide.vfs.shared.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id: $
 * 
 */
public class BuildApplicationPresenter extends GitPresenter implements BuildApplicationHandler,
   UserInfoReceivedHandler, ViewClosedHandler
{

   public interface Display extends IsView
   {

      void output(String text);

      void clearOutput();

      void startAnimation();

      void stopAnimation();

      void setBlinkIcon(Image icon, boolean blinking);

   }

   private Display display;

   private boolean closed = true;

   private String jobName;

   private UserInfo userInfo;

   /**
    * Delay in millisecond between job status request
    */
   private static final int delay = 10000;

   private Status prevStatus = null;

   private boolean buildInProgress = false;

   /**
    * Project for build on Jenkins.
    */
   private ProjectModel project;

   private RequestStatusHandler gitInitStatusHandler;

   /**
    *
    */
   public BuildApplicationPresenter()
   {
      IDE.addHandler(BuildApplicationEvent.TYPE, this);
      IDE.addHandler(UserInfoReceivedEvent.TYPE, this);
      IDE.addHandler(ViewClosedEvent.TYPE, this);
   }

   /**
    * @see org.exoplatform.ide.extension.jenkins.client.event.BuildApplicationHandler#onBuildApplication(org.exoplatform.ide.extension.jenkins.client.event.BuildApplicationEvent)
    */
   @Override
   public void onBuildApplication(BuildApplicationEvent event)
   {
      if (buildInProgress)
      {
         String message = "You can not start the build of two projects at the same time.<br>";
         message += "Building of project <b>" + project.getPath() + "</b> is performed.";

         Dialogs.getInstance().showError(message);
         return;
      }

      project = event.getProject();
      if (project == null && makeSelectionCheck())
      {
         project = ((ItemContext)selectedItems.get(0)).getProject();
      }
      checkIsGitRepository(project);

   }

   private void checkIsGitRepository(final ProjectModel project)
   {
      try
      {
         VirtualFileSystem.getInstance().getChildren(project,
            new AsyncRequestCallback<List<Item>>(new ChildrenUnmarshaller(new ArrayList<Item>()))
            {

               @Override
               protected void onSuccess(List<Item> result)
               {
                  for (Item item : result)
                  {
                     if (".git".equals(item.getName()))
                     {
                        beforeBuild();
                        return;
                     }
                  }
                  initRepository(project);
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  initRepository(project);
               }
            });
      }
      catch (RequestException e)
      {
      }
   }

   /**
    * Perform check, that job already exists. If it doesn't exist, then create job.
    */
   private void beforeBuild()
   {
      jobName = (String)project.getPropertyValue("jenkins-job");
      if (jobName != null && !jobName.isEmpty())
      {
         build(jobName);
      }
      else
      {
         createJob();
      }
   }

   /**
    * Create new Jenkins job.
    * 
    * @param repository repository URL (public location of local repository)
    */
   private void createJob()
   {
      // dummy check that user name is e-mail.
      // Jenkins create git tag on build. Marks user as author of tag.
      String mail = userInfo.getName().contains("@") ? userInfo.getName() : userInfo.getName() + "@exoplatform.local";
      String uName = userInfo.getName().split("@")[0];// Jenkins don't alow in job name '@' character
      try
      {
         AutoBean<Job> job = JenkinsExtension.AUTO_BEAN_FACTORY.create(Job.class);
         AutoBeanUnmarshaller<Job> marshaller = new AutoBeanUnmarshaller<Job>(job);
         JenkinsService.get().createJenkinsJob(
            uName + "-" + getProjectName() + "-" + Random.nextInt(Integer.MAX_VALUE), uName, mail, vfs.getId(),
            project.getId(), new AsyncRequestCallback<Job>(marshaller)
            {
               @Override
               protected void onSuccess(Job result)
               {
                  build(result.getName());
                  jobName = result.getName();
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  IDE.fireEvent(new ExceptionThrownEvent(exception));
               }
            });
      }
      catch (RequestException e)
      {
         IDE.fireEvent(new ExceptionThrownEvent(e));
      }
   }

   /**
    * Get project name (last URL segment of workDir value)
    * 
    * @return project name
    */
   private String getProjectName()
   {
      String projectName = project.getPath();
      if (projectName.endsWith("/"))
      {
         projectName = projectName.substring(0, projectName.length() - 1);
      }
      projectName = projectName.substring(projectName.lastIndexOf("/") + 1, projectName.length() - 1);
      return projectName;
   }

   /**
    * Start building application
    * 
    * @param jobName name of Jenkins job
    */
   private void build(String jobName)
   {
      try
      {
         boolean useWebSocketForCallback = false;
         final WebSocket ws = null;//WebSocket.getInstance(); TODO: temporary disable web-sockets
         if (ws != null && ws.getReadyState() == WebSocket.ReadyState.OPEN)
         {
            useWebSocketForCallback = true;
            ws.messageBus().subscribe(Channels.JENKINS_BUILD_STATUS, jenkinsBuildStatusHandler);
         }
         final boolean useWebSocket = useWebSocketForCallback;

         JenkinsService.get().buildJob(vfs.getId(), project.getId(), jobName, useWebSocket,
            new AsyncRequestCallback<Object>()
            {
               @Override
               protected void onSuccess(Object result)
               {
                  buildInProgress = true;

                  showBuildMessage("Building project <b>" + project.getPath() + "</b>");

                  display.startAnimation();
                  display.setBlinkIcon(new Image(JenkinsExtension.RESOURCES.grey()), true);

                  prevStatus = null;

                  if (!useWebSocket)
                  {
                     refreshJobStatusTimer.schedule(delay);
                  }
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  IDE.fireEvent(new ExceptionThrownEvent(exception));
                  if (useWebSocket)
                  {
                     ws.messageBus().unsubscribe(Channels.JENKINS_BUILD_STATUS, jenkinsBuildStatusHandler);
                  }
               }
            });
      }
      catch (RequestException e)
      {
         IDE.fireEvent(new ExceptionThrownEvent(e));
      }
      catch (WebSocketException e)
      {
         IDE.fireEvent(new ExceptionThrownEvent(e));
      }
   }

   /**
    * Sets Building status: Queue
    * 
    * @param status
    */
   private void setBuildStatusQueue(JobStatus status)
   {
      prevStatus = Status.QUEUE;
      showBuildMessage("Status: " + status.getStatus());
      display.setBlinkIcon(new Image(JenkinsExtension.RESOURCES.grey()), true);
   }

   /**
    * Sets Building status: Building
    * 
    * @param status
    */
   private void setBuildStatusBuilding(JobStatus status)
   {
      prevStatus = Status.BUILD;
      showBuildMessage("Status: " + status.getStatus());
      display.setBlinkIcon(new Image(JenkinsExtension.RESOURCES.blue()), true);
   }

   /**
    * Sets Building status: Finished
    * 
    * @param status
    */
   private void setBuildStatusFinished(JobStatus status)
   {
      buildInProgress = false;

      if (display != null)
      {
         if (closed)
         {
            IDE.getInstance().openView(display.asView());
            closed = false;
         }
         else
         {
            display.asView().activate();
         }
      }

      prevStatus = Status.END;

      String message =
         "Building project <b>" + project.getPath() + "</b> has been finished.\r\nResult: "
            + status.getLastBuildResult() == null ? "Unknown" : status.getLastBuildResult();

      showBuildMessage(message);
      display.stopAnimation();

      if (status.getLastBuildResult() == null)
      {
         display.setBlinkIcon(new Image(JenkinsExtension.RESOURCES.red()), false);
         return;
      }

      switch (JobResult.valueOf(status.getLastBuildResult()))
      {
         case SUCCESS :
            display.setBlinkIcon(new Image(JenkinsExtension.RESOURCES.blue()), false);
            break;

         case FAILURE :
            display.setBlinkIcon(new Image(JenkinsExtension.RESOURCES.red()), false);
            break;

         default :
            display.setBlinkIcon(new Image(JenkinsExtension.RESOURCES.yellow()), false);
            break;
      }
   }

   /**
    * Check for status and display necessary messages.
    * 
    * @param status
    */
   private void updateJobStatus(JobStatus status)
   {
      if (status.getStatus() == Status.QUEUE && prevStatus != Status.QUEUE)
      {
         setBuildStatusQueue(status);
         return;
      }

      if (status.getStatus() == Status.BUILD && prevStatus != Status.BUILD)
      {
         setBuildStatusBuilding(status);
         return;
      }

      if (status.getStatus() == Status.END && prevStatus != Status.END)
      {
         setBuildStatusFinished(status);
         return;
      }
   }

   private Timer refreshJobStatusTimer = new Timer()
   {
      @Override
      public void run()
      {
         try
         {
            AutoBean<JobStatus> jobStatus = JenkinsExtension.AUTO_BEAN_FACTORY.create(JobStatus.class);
            AutoBeanUnmarshaller<JobStatus> unmarshaller = new AutoBeanUnmarshaller<JobStatus>(jobStatus);
            JenkinsService.get().jobStatus(vfs.getId(), project.getId(), jobName,
               new AsyncRequestCallback<JobStatus>(unmarshaller)
               {
                  @Override
                  protected void onSuccess(JobStatus status)
                  {
                     updateJobStatus(status);

                     if (status.getStatus() == Status.END)
                     {
                        onJobFinished(status);
                     }
                     else
                     {
                        schedule(delay);
                     }
                  }

                  protected void onFailure(Throwable exception)
                  {
                     buildInProgress = false;
                     IDE.fireEvent(new ExceptionThrownEvent(exception));
                  };

               });
         }
         catch (RequestException e)
         {
            IDE.fireEvent(new ExceptionThrownEvent(e));
         }
      }
   };

   /**
    * Performs actions when job status received.
    * 
    * @param status build job status
    */
   private void onJobFinished(JobStatus status)
   {
      IDE.fireEvent(new ApplicationBuiltEvent(status));

      try
      {
         JenkinsService.get().getJenkinsOutput(vfs.getId(), project.getId(), jobName,
            new AsyncRequestCallback<StringBuilder>(new StringContentUnmarshaller(new StringBuilder()))
            {
               @Override
               protected void onSuccess(StringBuilder result)
               {
                  showBuildMessage(result.toString());
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  IDE.fireEvent(new ExceptionThrownEvent(exception));
               }
            });
      }
      catch (RequestException e)
      {
         IDE.fireEvent(new ExceptionThrownEvent(e));
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.userinfo.event.UserInfoReceivedHandler#onUserInfoReceived(org.exoplatform.ide.client.framework.userinfo.event.UserInfoReceivedEvent)
    */
   @Override
   public void onUserInfoReceived(UserInfoReceivedEvent event)
   {
      userInfo = event.getUserInfo();
   }

   /**
    * Initialize Git repository.
    * 
    * @param path working directory of the repository
    */
   private void initRepository(final ProjectModel project)
   {
      try
      {
         boolean useWebSocketForCallback = false;
         final WebSocket ws = null;//WebSocket.getInstance(); TODO: temporary disable web-sockets
         if (ws != null && ws.getReadyState() == WebSocket.ReadyState.OPEN)
         {
            useWebSocketForCallback = true;
            gitInitStatusHandler = new InitRequestStatusHandler(project.getName());
            gitInitStatusHandler.requestInProgress(project.getId());
            ws.messageBus().subscribe(Channels.GIT_REPO_INITIALIZED, repoInitializedHandler);
         }
         final boolean useWebSocket = useWebSocketForCallback;

         GitClientService.getInstance().init(vfs.getId(), project.getId(), project.getName(), false, useWebSocket,
            new AsyncRequestCallback<String>()
            {
               @Override
               protected void onSuccess(String result)
               {
                  if (!useWebSocket)
                  {
                     showBuildMessage(GitExtension.MESSAGES.initSuccess());
                     IDE.fireEvent(new RefreshBrowserEvent());
                     createJob();
                  }
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  handleError(exception);
                  if (useWebSocket)
                  {
                     ws.messageBus().unsubscribe(Channels.GIT_REPO_INITIALIZED, repoInitializedHandler);
                  }
               }
            });
      }
      catch (RequestException e)
      {
         handleError(e);
      }
      catch (WebSocketException e)
      {
         handleError(e);
      }
   }

   private void handleError(Throwable e)
   {
      String errorMessage =
         (e.getMessage() != null && e.getMessage().length() > 0) ? e.getMessage() : GitExtension.MESSAGES.initFailed();
      IDE.fireEvent(new OutputEvent(errorMessage, Type.ERROR));
   }

   private void showBuildMessage(String message)
   {
      if (display != null)
      {
         if (closed)
         {
            IDE.getInstance().openView(display.asView());
            closed = false;
         }
      }
      else
      {
         display = GWT.create(Display.class);
         IDE.getInstance().openView(display.asView());
         closed = false;
      }

      display.output(message);
   }

   @Override
   public void onViewClosed(ViewClosedEvent event)
   {
      if (event.getView() instanceof Display)
      {
         closed = true;
      }
   }

   /**
    * Performs actions after Jenkins job status was received.
    */
   private WebSocketEventHandler jenkinsBuildStatusHandler = new WebSocketEventHandler()
   {
      @Override
      public void onMessage(WebSocketEventMessage event)
      {
         JobStatus buildStatus =
            AutoBeanCodex.decode(JenkinsExtension.AUTO_BEAN_FACTORY, JobStatus.class, event.getPayload()).as();

         updateJobStatus(buildStatus);
         if (buildStatus.getStatus() == Status.END)
         {
            WebSocket.getInstance().messageBus().unsubscribe(Channels.JENKINS_BUILD_STATUS, this);
            onJobFinished(buildStatus);
         }
      }

      @Override
      public void onError(Exception exception)
      {
         WebSocket.getInstance().messageBus().unsubscribe(Channels.JENKINS_BUILD_STATUS, this);

         String exceptionMessage = null;
         if (exception.getMessage() != null && exception.getMessage().length() > 0)
         {
            exceptionMessage = exception.getMessage();
         }

         buildInProgress = false;
         display.setBlinkIcon(new Image(JenkinsExtension.RESOURCES.red()), false);

         if (exceptionMessage != null)
         {
            IDE.fireEvent(new OutputEvent(exceptionMessage, Type.ERROR));
         }
      }
   };

   /**
    * Performs actions after the Git-repository was initialized.
    */
   private WebSocketEventHandler repoInitializedHandler = new WebSocketEventHandler()
   {
      @Override
      public void onMessage(WebSocketEventMessage event)
      {
         WebSocket.getInstance().messageBus().unsubscribe(Channels.GIT_REPO_INITIALIZED, this);

         gitInitStatusHandler.requestFinished(project.getId());
         showBuildMessage(GitExtension.MESSAGES.initSuccess());
         IDE.fireEvent(new RefreshBrowserEvent());
         createJob();
      }

      @Override
      public void onError(Exception exception)
      {
         WebSocket.getInstance().messageBus().unsubscribe(Channels.GIT_REPO_INITIALIZED, this);

         gitInitStatusHandler.requestError(project.getId(), exception);
         handleError(exception);
      }
   };
}
