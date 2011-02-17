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
package org.exoplatform.ide.client.framework.vfs.callback;

import com.google.gwt.http.client.Request;

import org.exoplatform.gwtframework.commons.rest.ClientRequestCallback;
import org.exoplatform.ide.client.framework.vfs.Item;

/**
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: ItemUnlockCallback.java Feb 11, 2011 11:16:17 AM vereshchaka $
 *
 */
public abstract class ItemUnlockCallback extends ClientRequestCallback
{
   
   private Item item;
   
   /**
    * @return the item
    */
   public Item getItem()
   {
      return item;
   }
   
   /**
    * @param item the item to set
    */
   public void setItem(Item item)
   {
      this.item = item;
   }

   /**
    * @see com.google.gwt.http.client.RequestCallback#onError(com.google.gwt.http.client.Request, java.lang.Throwable)
    */
   @Override
   public void onError(Request request, Throwable exception)
   {
      fireErrorEvent();
   }

   /**
    * @see org.exoplatform.gwtframework.commons.rest.ClientRequestCallback#onUnsuccess(java.lang.Throwable)
    */
   @Override
   public void onUnsuccess(Throwable exception)
   {
      fireErrorEvent();
   }
   
   public abstract void fireErrorEvent();

}
