/**
 * Copyright (C) 2009 eXo Platform SAS.
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
 *
 */
package org.exoplatform.ide.client.module.edit.control;

import org.exoplatform.gwtframework.ui.client.component.command.SimpleControl;
import org.exoplatform.ide.client.IDEImageBundle;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler;
import org.exoplatform.ide.client.framework.editor.event.EditorFileContentChangedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorFileContentChangedHandler;
import org.exoplatform.ide.client.module.edit.event.RedoTypingEvent;
import org.exoplatform.ide.client.framework.module.vfs.api.Version;
import org.exoplatform.ide.client.framework.module.vfs.api.event.FileContentReceivedEvent;
import org.exoplatform.ide.client.framework.module.vfs.api.event.FileContentReceivedHandler;

import com.google.gwt.event.shared.HandlerManager;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class RedoTypingCommand extends SimpleControl implements EditorActiveFileChangedHandler,
   EditorFileContentChangedHandler, FileContentReceivedHandler
{

   public static final String ID = "Edit/Redo Typing";

   public static final String TITLE = "Redo Typing";

   public RedoTypingCommand(HandlerManager eventBus)
   {
      super(ID);
      setTitle(TITLE);
      setPrompt(TITLE);
      setImages(IDEImageBundle.INSTANCE.redo(), IDEImageBundle.INSTANCE.redoDisabled());
      setEvent(new RedoTypingEvent());

      eventBus.addHandler(EditorActiveFileChangedEvent.TYPE, this);
      eventBus.addHandler(EditorFileContentChangedEvent.TYPE, this);
      eventBus.addHandler(FileContentReceivedEvent.TYPE, this);
   }

   public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event)
   {
      if (event.getFile() == null || (event.getFile() instanceof Version))
      {
         setVisible(false);
         setEnabled(false);
         return;
      }

      setVisible(true);
      if (event.getEditor() != null)
      {
         setEnabled(event.getEditor().hasRedoChanges());
      }
      else
      {
         setEnabled(false);
      }
   }

   public void onEditorFileContentChanged(EditorFileContentChangedEvent event)
   {
      setEnabled(event.hasRedoChanges());
   }

   public void onFileContentReceived(FileContentReceivedEvent event)
   {
      setVisible(true);
      setEnabled(false);
   }

}
