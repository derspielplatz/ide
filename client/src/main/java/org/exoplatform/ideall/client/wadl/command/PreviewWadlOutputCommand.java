/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ideall.client.wadl.command;

import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.ideall.client.Images;
import org.exoplatform.ideall.client.application.component.IDECommand;
import org.exoplatform.ideall.client.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ideall.client.editor.event.EditorActiveFileChangedHandler;
import org.exoplatform.ideall.client.wadl.event.PreviewWadlOutputEvent;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: $
*/
public class PreviewWadlOutputCommand extends IDECommand implements EditorActiveFileChangedHandler
{
   private static final String ID = "Run/Wadl";

   private static final String TITLE = "Preview REST Service Output...";

   public PreviewWadlOutputCommand()
   {
      super(ID);
      setTitle(TITLE);
      setIcon(Images.MainMenu.GROOVY_OUTPUT);
      setEvent(new PreviewWadlOutputEvent());

      //super(ID, TITLE, Images.MainMenu.GROOVY_OUTPUT, new PreviewWadlOutputEvent());
   }

   @Override
   protected void onRegisterHandlers()
   {
      addHandler(EditorActiveFileChangedEvent.TYPE, this);
   }

   public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event)
   {
      if (event.getFile() == null)
      {
         setEnabled(false);
         setVisible(false);
         return;
      }

      if (MimeType.SCRIPT_GROOVY.equals(event.getFile().getContentType()))
      {
         setVisible(true);
         setEnabled(true);
      }
      else
      {
         setVisible(false);
         setEnabled(false);
      }
   }

}
