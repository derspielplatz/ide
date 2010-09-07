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
package org.exoplatform.ide.client.module.development;

import org.exoplatform.ide.client.framework.control.event.RegisterControlEvent;
import org.exoplatform.ide.client.framework.module.IDEModule;
import org.exoplatform.ide.client.module.development.control.ShowOutlineControl;
import org.exoplatform.ide.client.module.development.control.ShowPreviewCommand;

import com.google.gwt.event.shared.HandlerManager;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $Id: $
 */

public class DevelopmentModule implements IDEModule
{
   
   public DevelopmentModule(HandlerManager eventBus)
   {
      eventBus.fireEvent(new RegisterControlEvent(new ShowOutlineControl(eventBus), true));
      eventBus.fireEvent(new RegisterControlEvent(new ShowPreviewCommand(eventBus), true, true));
      
      new DevelopmentModuleEventHandler(eventBus);
   }

}
