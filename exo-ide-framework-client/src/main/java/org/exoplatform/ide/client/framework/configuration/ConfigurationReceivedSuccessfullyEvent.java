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
package org.exoplatform.ide.client.framework.configuration;

import org.exoplatform.ide.client.framework.configuration.IDEConfiguration;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Called after success configuration loading.
 * 
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class ConfigurationReceivedSuccessfullyEvent extends GwtEvent<ConfigurationReceivedSuccessfullyHandler>
{

   public static final GwtEvent.Type<ConfigurationReceivedSuccessfullyHandler> TYPE =
      new GwtEvent.Type<ConfigurationReceivedSuccessfullyHandler>();

   private IDEConfiguration configuration;

   public ConfigurationReceivedSuccessfullyEvent(IDEConfiguration configuration)
   {
      this.configuration = configuration;
   }

   public IDEConfiguration getConfiguration()
   {
      return configuration;
   }

   @Override
   protected void dispatch(ConfigurationReceivedSuccessfullyHandler handler)
   {
      handler.onConfigurationReceivedSuccessfully(this);
   }

   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<ConfigurationReceivedSuccessfullyHandler> getAssociatedType()
   {
      return TYPE;
   }

}