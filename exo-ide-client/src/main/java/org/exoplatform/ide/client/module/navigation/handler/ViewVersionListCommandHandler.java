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

import org.exoplatform.gwtframework.commons.component.Handlers;
import org.exoplatform.gwtframework.commons.dialogs.Dialogs;
import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.exception.ExceptionThrownHandler;
import org.exoplatform.ide.client.event.EnableStandartErrorsHandlingEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler;
import org.exoplatform.ide.client.module.navigation.event.versioning.ViewVersionListEvent;
import org.exoplatform.ide.client.module.navigation.event.versioning.ViewVersionListHandler;
import org.exoplatform.ide.client.module.vfs.api.File;
import org.exoplatform.ide.client.module.vfs.api.Version;
import org.exoplatform.ide.client.module.vfs.api.VirtualFileSystem;
import org.exoplatform.ide.client.module.vfs.api.event.ItemVersionsReceivedEvent;
import org.exoplatform.ide.client.module.vfs.api.event.ItemVersionsReceivedHandler;
import org.exoplatform.ide.client.versioning.ViewVersionsForm;

/**
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Oct 12, 2010 $
 *
 */
public class ViewVersionListCommandHandler implements ViewVersionListHandler, ExceptionThrownHandler,
   ItemVersionsReceivedHandler, EditorActiveFileChangedHandler
{
   private HandlerManager eventBus;

   private Handlers handlers;

   private File activeFile;

   public ViewVersionListCommandHandler(HandlerManager eventBus)
   {
      this.eventBus = eventBus;
      handlers = new Handlers(eventBus);
      handlers.addHandler(ViewVersionListEvent.TYPE, this);
      handlers.addHandler(EditorActiveFileChangedEvent.TYPE, this);
   }

   private void getVersionHistory()
   {
      if (activeFile != null && !(activeFile instanceof Version))
      {
         handlers.addHandler(ExceptionThrownEvent.TYPE, this);
         handlers.addHandler(ItemVersionsReceivedEvent.TYPE, this);
         VirtualFileSystem.getInstance().getVersions(activeFile);
      }
      else
      {
         Dialogs.getInstance().showInfo("Please, open file.");
      }
   }

   /**
    * @see org.exoplatform.gwtframework.commons.exception.ExceptionThrownHandler#onError(org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent)
    */
   public void onError(ExceptionThrownEvent event)
   {
      handlers.removeHandler(ExceptionThrownEvent.TYPE);
      handlers.removeHandler(ItemVersionsReceivedEvent.TYPE);
      eventBus.fireEvent(new EnableStandartErrorsHandlingEvent(false));
   }

   /**
    * @see org.exoplatform.ide.client.module.vfs.api.event.ItemVersionsReceivedHandler#onItemVersionsReceived(org.exoplatform.ide.client.module.vfs.api.event.ItemVersionsReceivedEvent)
    */
   public void onItemVersionsReceived(final ItemVersionsReceivedEvent event)
   {
      handlers.removeHandler(ExceptionThrownEvent.TYPE);
      handlers.removeHandler(ItemVersionsReceivedEvent.TYPE);
      if (event.getVersions() != null && event.getVersions().size() > 0)
      {
         new ViewVersionsForm(eventBus, event.getItem(), event.getVersions());
      }
      else
      {
         Dialogs.getInstance().showInfo("Item \"" + event.getItem().getName() + "\" has no versions.");
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler#onEditorActiveFileChanged(org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent)
    */
   public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event)
   {
      activeFile = event.getFile();
   }

   /**
    * @see org.exoplatform.ide.client.module.navigation.event.versioning.ViewVersionListHandler#onViewVersionList(org.exoplatform.ide.client.module.navigation.event.versioning.ViewVersionListEvent)
    */
   public void onViewVersionList(ViewVersionListEvent event)
   {
      getVersionHistory();
   }
}