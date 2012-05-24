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
package org.exoplatform.ide.extension.googleappengine.client.login;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event occurs, when user tries to login to Google App Engine account.
 * 
 * @author <a href="mailto:azhuleva@exoplatform.com">Ann Shumilova</a>
 * @version $Id: May 18, 2012 12:10:28 PM anya $
 * 
 */
public class LoginEvent extends GwtEvent<LoginHandler>
{
   /**
    * Type used to register this event.
    */
   public static final GwtEvent.Type<LoginHandler> TYPE = new GwtEvent.Type<LoginHandler>();

   /**
    * Handler for performing operation with passing credentials.
    */
   private PerformOperationHandler performOperationHandler;

   /**
    * Login canceled handler.
    */
   private LoginCanceledHandler loginCanceled;

   /**
    * @param performOperationHandler handler for performing operation with passing credentials
    * @param loginCanceled login canceled handler
    */
   public LoginEvent(PerformOperationHandler performOperationHandler, LoginCanceledHandler loginCanceled)
   {
      this.performOperationHandler = performOperationHandler;
      this.loginCanceled = loginCanceled;
   }

   /**
    * @return {@link PerformOperationHandler} handler for performing operation with passing credentials
    */
   public PerformOperationHandler getPerformOperationHandler()
   {
      return performOperationHandler;
   }

   /**
    * @return {@link LoginCanceledHandler} login canceled handler
    */
   public LoginCanceledHandler getLoginCanceled()
   {
      return loginCanceled;
   }

   /**
    * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
    */
   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<LoginHandler> getAssociatedType()
   {
      return TYPE;
   }

   /**
    * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
    */
   @Override
   protected void dispatch(LoginHandler handler)
   {
      handler.onLogin(this);
   }
}