/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ide.client.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.gwtframework.commons.component.Handlers;
import org.exoplatform.gwtframework.commons.dialogs.Dialogs;
import org.exoplatform.gwtframework.commons.dialogs.callback.BooleanValueReceivedCallback;
import org.exoplatform.gwtframework.commons.initializer.RegistryConstants;
import org.exoplatform.gwtframework.ui.client.component.command.Control;
import org.exoplatform.gwtframework.ui.client.component.menu.event.UpdateMainMenuEvent;
import org.exoplatform.gwtframework.ui.client.component.statusbar.event.UpdateStatusBarEvent;
import org.exoplatform.gwtframework.ui.client.component.toolbar.event.UpdateToolbarEvent;
import org.exoplatform.ide.client.ExceptionThrownEventHandlerInitializer;
import org.exoplatform.ide.client.IDELoader;
import org.exoplatform.ide.client.framework.application.ApplicationConfiguration;
import org.exoplatform.ide.client.framework.application.event.EntryPointChangedEvent;
import org.exoplatform.ide.client.framework.application.event.InitializeApplicationEvent;
import org.exoplatform.ide.client.framework.application.event.InitializeServicesEvent;
import org.exoplatform.ide.client.framework.application.event.RegisterEventHandlersEvent;
import org.exoplatform.ide.client.framework.control.event.ControlsUpdatedEvent;
import org.exoplatform.ide.client.model.ApplicationContext;
import org.exoplatform.ide.client.model.configuration.Configuration;
import org.exoplatform.ide.client.model.configuration.ConfigurationReceivedSuccessfullyEvent;
import org.exoplatform.ide.client.model.configuration.ConfigurationReceivedSuccessfullyHandler;
import org.exoplatform.ide.client.model.conversation.ConversationService;
import org.exoplatform.ide.client.model.conversation.ConversationServiceImpl;
import org.exoplatform.ide.client.model.conversation.event.UserInfoReceivedEvent;
import org.exoplatform.ide.client.model.conversation.event.UserInfoReceivedHandler;
import org.exoplatform.ide.client.model.settings.ApplicationSettings;
import org.exoplatform.ide.client.model.settings.SettingsService;
import org.exoplatform.ide.client.model.settings.ApplicationSettings.Store;
import org.exoplatform.ide.client.model.settings.event.ApplicationSettingsReceivedEvent;
import org.exoplatform.ide.client.model.settings.event.ApplicationSettingsReceivedHandler;
import org.exoplatform.ide.client.model.template.TemplateServiceImpl;
import org.exoplatform.ide.client.module.gadget.service.GadgetServiceImpl;
import org.exoplatform.ide.client.module.preferences.event.SelectWorkspaceEvent;
import org.exoplatform.ide.client.module.vfs.api.File;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Timer;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $Id: $
 */

public class IDEConfigurationLoader implements ConfigurationReceivedSuccessfullyHandler, UserInfoReceivedHandler,
   ApplicationSettingsReceivedHandler
{

   private HandlerManager eventBus;

   private ApplicationContext context;

   private Handlers handlers;

   private ApplicationConfiguration applicationConfiguration;

   private ApplicationSettings applicationSettings;

   private List<Control> controls;

   private List<String> toolbarItems;

   private List<String> statusBarItems;

   public IDEConfigurationLoader(HandlerManager eventBus, ApplicationContext context, List<Control> controls,
      List<String> toolbarItems, List<String> statusBarItems)
   {
      this.eventBus = eventBus;
      this.context = context;
      this.controls = controls;
      this.toolbarItems = toolbarItems;
      this.statusBarItems = statusBarItems;

      handlers = new Handlers(eventBus);
      handlers.addHandler(ConfigurationReceivedSuccessfullyEvent.TYPE, this);
      handlers.addHandler(UserInfoReceivedEvent.TYPE, this);
      handlers.addHandler(ApplicationSettingsReceivedEvent.TYPE, this);

      Configuration configuration = new Configuration(eventBus, context);
      configuration.loadConfiguration(IDELoader.getInstance());
   }

   /**
    * Called in case the valid configuration of the application is received
    * 
    * @see org.exoplatform.ide.client.model.configuration.ApplicationConfigurationReceivedHandler#onApplicationConfigurationReceived(org.exoplatform.ide.client.model.configuration.ApplicationConfigurationReceivedEvent)
    */
   public void onConfigurationReceivedSuccessfully(ConfigurationReceivedSuccessfullyEvent event)
   {
      try
      {
         applicationConfiguration = event.getConfiguration();

         new ConversationServiceImpl(eventBus, IDELoader.getInstance(), context.getApplicationConfiguration()
            .getContext());

         new TemplateServiceImpl(eventBus, IDELoader.getInstance(), context.getApplicationConfiguration()
            .getRegistryURL()
            + "/" + RegistryConstants.EXO_APPLICATIONS + "/" + Configuration.APPLICATION_NAME);

         new GadgetServiceImpl(eventBus, IDELoader.getInstance(), context.getApplicationConfiguration().getContext(),
            context.getApplicationConfiguration().getGadgetServer(), context.getApplicationConfiguration()
               .getPublicContext());

         new Timer()
         {
            @Override
            public void run()
            {
               ConversationService.getInstance().getUserInfo();
            }
         }.schedule(10);

      }
      catch (Throwable e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Called when user information ( name, ect ) is received
    * 
    * @see org.exoplatform.ide.client.model.conversation.event.UserInfoReceivedHandler#onUserInfoReceived(org.exoplatform.ide.client.model.conversation.event.UserInfoReceivedEvent)
    */
   public void onUserInfoReceived(final UserInfoReceivedEvent event)
   {
      context.setUserInfo(event.getUserInfo());

      new Timer()
      {
         @Override
         public void run()
         {
            new ControlsFormatter(eventBus).format(controls);
            eventBus.fireEvent(new ControlsUpdatedEvent(controls));

            new SettingsService(eventBus, applicationConfiguration.getRegistryURL(), event.getUserInfo().getName(),
               IDELoader.getInstance());
         }
      }.schedule(10);
   }

   public void onApplicationSettingsReceived(ApplicationSettingsReceivedEvent event)
   {
      applicationSettings = event.getApplicationSettings();
      
      System.out.println("entry point: " + applicationSettings.getValue("entry-point"));
      
      if (applicationSettings.getValue("entry-point") == null)
      {
         String defaultEntryPoint = context.getApplicationConfiguration().getDefaultEntryPoint();
         if (!defaultEntryPoint.endsWith("/")) {
            defaultEntryPoint += "/";
         }
         
         applicationSettings.setValue("entry-point", context.getApplicationConfiguration().getDefaultEntryPoint(),
            Store.COOKIES);
      }

      new Timer()
      {
         @Override
         public void run()
         {
            initServices();
         }
      }.schedule(10);
   }

   private void initializeApplication()
   {
      new Timer()
      {
         @Override
         public void run()
         {
            eventBus.fireEvent(new RegisterEventHandlersEvent());

            new Timer()
            {
               @Override
               public void run()
               {
                  try
                  {
                     Map<String, File> openedFiles = new LinkedHashMap<String, File>();
                     eventBus.fireEvent(new InitializeApplicationEvent(openedFiles, null));
                  }
                  catch (Throwable e)
                  {
                     e.printStackTrace();
                  }
               }

            }.schedule(10);

         }
      }.schedule(10);
   }

   private void initServices()
   {
      eventBus.fireEvent(new InitializeServicesEvent(applicationConfiguration, IDELoader.getInstance()));

      new Timer()
      {
         @Override
         public void run()
         {
            initialize();
         }
      }.schedule(10);
   }

   @SuppressWarnings("unchecked")
   private void initialize()
   {
      /*
       * Updating top menu
       */
      eventBus.fireEvent(new UpdateMainMenuEvent(controls));
      eventBus.fireEvent(new UpdateStatusBarEvent(context.getStatusBarItems(), controls));
      eventBus.fireEvent(new UpdateToolbarEvent(toolbarItems, controls));
      eventBus.fireEvent(new UpdateStatusBarEvent(statusBarItems, controls));

      if (applicationSettings.getValue("entry-point") != null)
      {
         String entryPoint = (String)applicationSettings.getValue("entry-point");
         eventBus.fireEvent(new EntryPointChangedEvent(entryPoint));
         new WorkspaceChecker(eventBus, entryPoint, applicationSettings);
      }
      else
      {
         initializeApplication();

         Dialogs
            .getInstance()
            .ask(
               "Working workspace",
               "Workspace is not set. Goto <strong>Window->Select workspace</strong> in main menu for set working workspace?",
               new BooleanValueReceivedCallback()
               {
                  public void execute(Boolean value)
                  {
                     if (value)
                     {
                        eventBus.fireEvent(new SelectWorkspaceEvent());
                     }
                     else
                     {
                        ExceptionThrownEventHandlerInitializer.initialize(eventBus);
                     }
                  }
               });
      }
   }

}
