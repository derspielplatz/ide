/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.ide.client.module.navigation.handler;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;

import org.exoplatform.gwtframework.commons.component.Handlers;
import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.exception.ExceptionThrownHandler;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler;
import org.exoplatform.ide.client.framework.event.FileSavedEvent;
import org.exoplatform.ide.client.framework.event.SaveFileAsEvent;
import org.exoplatform.ide.client.framework.event.SaveFileEvent;
import org.exoplatform.ide.client.framework.event.SaveFileHandler;
import org.exoplatform.ide.client.framework.settings.ApplicationSettings.Store;
import org.exoplatform.ide.client.framework.settings.event.ApplicationSettingsReceivedEvent;
import org.exoplatform.ide.client.framework.settings.event.ApplicationSettingsReceivedHandler;
import org.exoplatform.ide.client.framework.vfs.File;
import org.exoplatform.ide.client.framework.vfs.FileContentSaveCallback;
import org.exoplatform.ide.client.framework.vfs.ItemPropertiesCallback;
import org.exoplatform.ide.client.framework.vfs.VirtualFileSystem;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class SaveFileCommandHandler implements ExceptionThrownHandler, SaveFileHandler, 
EditorActiveFileChangedHandler, ApplicationSettingsReceivedHandler
{

   private Handlers handlers;

   private HandlerManager eventBus;

   private File activeFile;

   private Map<String, String> lockTokens;

   public SaveFileCommandHandler(HandlerManager eventBus)
   {
      this.eventBus = eventBus;

      handlers = new Handlers(eventBus);

      eventBus.addHandler(SaveFileEvent.TYPE, this);
      eventBus.addHandler(EditorActiveFileChangedEvent.TYPE, this);
      eventBus.addHandler(ApplicationSettingsReceivedEvent.TYPE, this);
   }

   public void onSaveFile(SaveFileEvent event)
   {
      File file = event.getFile() != null ? event.getFile() : activeFile;

      if (file.isNewFile())
      {
         eventBus.fireEvent(new SaveFileAsEvent(file, SaveFileAsEvent.SaveDialogType.YES_CANCEL, null, null));
         return;
      }

      handlers.addHandler(ExceptionThrownEvent.TYPE, this);
      
      String lockToken = lockTokens.get(file.getHref());

      if (file.isContentChanged())
      {
         VirtualFileSystem.getInstance().saveContent(file, lockToken, new FileContentSaveCallback(eventBus)
         {
            public void onResponseReceived(Request request, Response response)
            {
               getProperties(this.getFile());
            }
         });
         return;
      }
      else
      {
         if (file.isPropertiesChanged())
         {
            VirtualFileSystem.getInstance().saveProperties(file, lockToken, new ItemPropertiesCallback()
            {
               
               public void onResponseReceived(Request request, Response response)
               {
                  handlers.removeHandlers();
                  eventBus.fireEvent(new FileSavedEvent((File)this.getItem(), null));
               }
               
               @Override
               public void fireErrorEvent()
               {
                  eventBus.fireEvent(new ExceptionThrownEvent("Service is not deployed.<br>Resource not found."));
               }
            });
            return;
         }
      }

      handlers.removeHandlers();
   }

   private void getProperties(File file)
   {
      VirtualFileSystem.getInstance().getPropertiesCallback(file, new ItemPropertiesCallback()
      {
         
         public void onResponseReceived(Request request, Response response)
         {
            handlers.removeHandlers();
            eventBus.fireEvent(new FileSavedEvent((File)this.getItem(), null));
         }
         
         @Override
         public void fireErrorEvent()
         {
            eventBus.fireEvent(new ExceptionThrownEvent("Service is not deployed.<br>Resource not found."));
            handlers.removeHandlers();
         }
      });
   }

   public void onError(ExceptionThrownEvent event)
   {
      handlers.removeHandlers();
      event.getError().printStackTrace();
   }

   public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event)
   {
      activeFile = event.getFile();
   }

   /**
    * @see org.exoplatform.ide.client.model.settings.event.ApplicationSettingsReceivedHandler#onApplicationSettingsReceived(org.exoplatform.ide.client.model.settings.event.ApplicationSettingsReceivedEvent)
    */
   public void onApplicationSettingsReceived(ApplicationSettingsReceivedEvent event)
   {
      if (event.getApplicationSettings().getValueAsMap("lock-tokens") == null)
      {
         event.getApplicationSettings().setValue("lock-tokens", new LinkedHashMap<String, String>(), Store.COOKIES);
      }
      
      lockTokens = event.getApplicationSettings().getValueAsMap("lock-tokens");
   }

}
