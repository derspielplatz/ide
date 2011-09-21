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
package org.exoplatform.ide.client.progress;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HTML;

import org.exoplatform.gwtframework.ui.client.command.StatusTextControl;
import org.exoplatform.gwtframework.ui.client.util.ImageHelper;
import org.exoplatform.ide.client.IDEImageBundle;
import org.exoplatform.ide.client.framework.control.IDEControl;
import org.exoplatform.ide.client.framework.job.Job;
import org.exoplatform.ide.client.progress.event.ShowProgressEvent;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:  Sep 16, 2011 evgen $
 *
 */
public class ProgressNotificationControl extends StatusTextControl implements IDEControl
{

   public static final String ID = "__request-notification-control";

   /**
    * @param id
    */
   public ProgressNotificationControl()
   {
      super(ID);
      setEnabled(true);
      setVisible(false);
      setText("&nbsp;");
      setSize(300);
      setEvent(new ShowProgressEvent());
   }

   /**
    * @see org.exoplatform.ide.client.framework.control.IDEControl#initialize(com.google.gwt.event.shared.HandlerManager)
    */
   @Override
   public void initialize(HandlerManager eventBus)
   {
   }

   public void updateState(Job job)
   {
      setVisible(true);

      ImageResource resource = null;
      String message = "";
      switch (job.getStatus())
      {
         case STARTED :
            resource = IDEImageBundle.INSTANCE.asyncRequest();
            message = job.getStartMessage();
            break;

         case FINISHED :
            message = job.getFinishMessage();
            resource = IDEImageBundle.INSTANCE.ok();
            break;
         case ERROR :
            message = job.getError().getMessage();
            resource = IDEImageBundle.INSTANCE.cancel();
            break;
      }

      setPrompt(new HTML(message).getText());
      setText(prepareText(message, resource));
   }

   /**
    * Set massage and icon
    * @param message
    * @param icon
    * @return
    */
   private String prepareText(String message, ImageResource icon)
   {
      String table =
         "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"height:16px; border-collapse: collapse;width:100%; table-layout: fixed;\">"
            + "<tr>"
            + "<td style=\"border: none; font-family:Verdana,Bitstream Vera Sans,sans-serif; font-size:11px; font-style:normal; text-align: left; overflow: hidden; white-space: nowrap; width:100%; \"><nobr>"
            + message
            + "</nobr></td><td style=\"width=100%;\">&nbsp;</td>"
            + "<td style=\"width:16px; height:16px;text-align: right;\">"
            + ImageHelper.getImageHTML(icon)
            + "</td>"
            + "</tr>" + "</table>";
      return table;
   }
}
