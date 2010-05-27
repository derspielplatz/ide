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
package org.exoplatform.ideall.client.common.command.edit;

import org.exoplatform.ideall.client.Images;
import org.exoplatform.ideall.client.application.component.IDECommand;
import org.exoplatform.ideall.client.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ideall.client.editor.event.EditorActiveFileChangedHandler;
import org.exoplatform.ideall.client.event.edit.CutItemsEvent;
import org.exoplatform.ideall.client.event.edit.DeleteCurrentLineEvent;

/**
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: $
 *
 */
public class DeleteLineControl extends IDECommand implements EditorActiveFileChangedHandler
{
   
   private static final String ID = "Edit/Delete Line";
   
   public DeleteLineControl()
   {
      super(ID);
      setTitle("Delete Line");
      setPrompt("Delete Line");
      setIcon(Images.Edit.DELETE_LINE);
      setEvent(new DeleteCurrentLineEvent());
   }

   @Override
   protected void onInitializeApplication()
   {
      
   }

   @Override
   protected void onRegisterHandlers()
   {
      addHandler(EditorActiveFileChangedEvent.TYPE, this);
     
   }

   /**
    * @see org.exoplatform.ideall.client.editor.event.EditorActiveFileChangedHandler#onEditorActiveFileChanged(org.exoplatform.ideall.client.editor.event.EditorActiveFileChangedEvent)
    */
   public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event)
   {
      
      if (event.getFile() == null)
      {
         setVisible(false);
         setEnabled(false);
         return;
      }
      else
      {
         setVisible(true);
         setEnabled(true);
      }
   }
   
}
