/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.ide.extension.googleappengine.client.indexes;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.ui.client.dialog.Dialogs;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.output.event.OutputEvent;
import org.exoplatform.ide.client.framework.output.event.OutputMessage.Type;
import org.exoplatform.ide.extension.googleappengine.client.GoogleAppEngineAsyncRequestCallback;
import org.exoplatform.ide.extension.googleappengine.client.GoogleAppEngineClientService;
import org.exoplatform.ide.extension.googleappengine.client.GoogleAppEngineExtension;
import org.exoplatform.ide.extension.googleappengine.client.GoogleAppEnginePresenter;
import org.exoplatform.ide.extension.googleappengine.client.login.LoggedInHandler;
import org.exoplatform.ide.extension.googleappengine.client.login.PerformOperationHandler;

import com.google.gwt.http.client.RequestException;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:vparfonov@exoplatform.com">Vitaly Parfonov</a>
 * @version $Id: $
*/
public class UpdateIndexesHandlerImpl extends GoogleAppEnginePresenter implements UpdateIndexesHandler
{

   public UpdateIndexesHandlerImpl()
   {
      IDE.addHandler(UpdateIndexesEvent.TYPE, this);
   }

   private PerformOperationHandler performOperationHandler = new PerformOperationHandler()
   {
      @Override
      public void onPerformOperation(String email, String password, LoggedInHandler loggedInHandler)
      {
         onUpdateIndexes(email, password, loggedInHandler);
      }
   };

   /**
    * {@inheritDoc}
    */
   @Override
   public void onUpdateIndexes(UpdateIndexesEvent updateIndexesEvent)
   {
      if (isAppEngineProject())
      {
         onUpdateIndexes(null, null, null);
      }
      else
      {
         Dialogs.getInstance().showError(GoogleAppEngineExtension.GAE_LOCALIZATION.notAppEngineProjectError());
      }
   }

   public void onUpdateIndexes(String email, String password, final LoggedInHandler loggedInHandler)
   {
      try
      {
         GoogleAppEngineClientService.getInstance().updateIndexes(currentVfs.getId(), currentProject.getId(), email,
            password, new GoogleAppEngineAsyncRequestCallback<Object>(performOperationHandler, null)
            {

               @Override
               protected void onSuccess(Object result)
               {
                  if (loggedInHandler != null)
                  {
                     loggedInHandler.onLoggedIn();
                  }

                  IDE.fireEvent(new OutputEvent(GoogleAppEngineExtension.GAE_LOCALIZATION.updateIndexesSuccessfully(),
                     Type.INFO));
               }
            });
      }
      catch (RequestException e)
      {
         IDE.fireEvent(new ExceptionThrownEvent(e));
      }
   }

}