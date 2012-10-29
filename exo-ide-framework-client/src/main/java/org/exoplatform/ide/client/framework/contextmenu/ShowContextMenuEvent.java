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
package org.exoplatform.ide.client.framework.contextmenu;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event occurs, when user tries to view context menu.
 * 
 * @author <a href="mailto:azhuleva@exoplatform.com">Ann Shumilova</a>
 * @version $Id: Apr 23, 2012 11:30:45 AM anya $
 * 
 */
public class ShowContextMenuEvent extends GwtEvent<ShowContextMenuHandler>
{
   /**
    * Type, used to register the event.
    */
   public static final GwtEvent.Type<ShowContextMenuHandler> TYPE = new GwtEvent.Type<ShowContextMenuHandler>();

   /**
    * X coordinate of the context menu.
    */
   private int x;

   /**
    * Y coordinate of the context menu.
    */
   private int y;

   /**
    * Object, on which context menu was called.
    */
   private Object object;

   /**
    * @param x coordinate of the context menu
    * @param y coordinate of the context menu
    * @param object object, on which context menu was called
    */
   public ShowContextMenuEvent(int x, int y, Object object)
   {
      this.x = x;
      this.y = y;
      this.object = object;
   }

   /**
    * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
    */
   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<ShowContextMenuHandler> getAssociatedType()
   {
      return TYPE;
   }

   /**
    * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
    */
   @Override
   protected void dispatch(ShowContextMenuHandler handler)
   {
      handler.onShowContextMenu(this);
   }

   /**
    * @return the x
    */
   public int getX()
   {
      return x;
   }

   /**
    * @return the y
    */
   public int getY()
   {
      return y;
   }

   public Object getObject()
   {
      return object;
   }
}