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
package org.exoplatform.ideall.client.common.command.edit;

import org.exoplatform.ideall.client.Images;
import org.exoplatform.ideall.client.application.component.IDECommand;
import org.exoplatform.ideall.client.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ideall.client.editor.event.EditorActiveFileChangedHandler;
import org.exoplatform.ideall.client.event.edit.HideLineNumbersEvent;
import org.exoplatform.ideall.client.model.File;
import org.exoplatform.ideall.client.model.settings.event.ApplicationContextSavedEvent;
import org.exoplatform.ideall.client.model.settings.event.ApplicationContextSavedHandler;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class HideLineNumbersCommand extends IDECommand implements EditorActiveFileChangedHandler,
   ApplicationContextSavedHandler
{

   private static final String ID = "Edit/Hide Line Numbers";

   private static final String TITLE = "Hide Line Numbers";

   private File activeFile;

   public HideLineNumbersCommand()
   {
      super(ID);
      setTitle(TITLE);
      setPrompt(TITLE);
      setIcon(Images.Edit.HIDE_LINE_NUMBERS);
      setEvent(new HideLineNumbersEvent());
   }

   @Override
   protected void onRegisterHandlers()
   {
      addHandler(EditorActiveFileChangedEvent.TYPE, this);
      addHandler(ApplicationContextSavedEvent.TYPE, this);
   }

   @Override
   protected void onInitializeApplication()
   {
      updateState();
   }

   private void updateState()
   {
      if (!context.isShowLineNumbers())
      {
         setVisible(false);
         return;
      }

      // verify and show
      if (activeFile == null)
      {
         setVisible(false);
         setEnabled(false);
      }
      else
      {
         setVisible(true);
         setEnabled(true);
      }
   }

   public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event)
   {
      activeFile = event.getFile();
      updateState();
   }

   public void onApplicationContextSaved(ApplicationContextSavedEvent event)
   {
      updateState();
   }

}
