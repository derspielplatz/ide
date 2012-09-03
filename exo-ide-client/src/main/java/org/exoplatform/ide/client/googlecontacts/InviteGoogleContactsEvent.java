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
package org.exoplatform.ide.client.googlecontacts;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Generated by pressing on Invite Google Contacts item in Help menu.
 * Listening this event IDE shows dialog for inviting user's Google Contacts.
 * 
 * @author <a href="mailto:azatsarynnyy@exoplatform.org">Artem Zatsarynnyy</a>
 * @version $Id: InviteGoogleContactsEvent.java Aug 27, 2012 10:00:55 AM azatsarynnyy $
 *
 */

public class InviteGoogleContactsEvent extends GwtEvent<InviteGoogleContactsHandler>
{
   public static final GwtEvent.Type<InviteGoogleContactsHandler> TYPE = new GwtEvent.Type<InviteGoogleContactsHandler>();

   @Override
   protected void dispatch(InviteGoogleContactsHandler handler)
   {
      handler.onShowGoogleContacts(this);
   }

   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<InviteGoogleContactsHandler> getAssociatedType()
   {
      return TYPE;
   }
}