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
package org.exoplatform.ide.editor.extension.php.client;

import com.google.gwt.resources.client.ImageResource.RepeatStyle;

import com.google.gwt.resources.client.ImageResource.ImageOptions;

import com.google.gwt.core.client.GWT;

import com.google.gwt.resources.client.ImageResource;

import com.google.gwt.resources.client.ClientBundle;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id: $
 *
 */
public interface PhpClientBundle extends ClientBundle
{
   PhpClientBundle INSTANCE = GWT.create(PhpClientBundle.class);

   @Source("org/exoplatform/ide/editor/extension/php/client/styles/php.css")
   PhpCss css();
   
   @Source("org/exoplatform/ide/editor/extension/php/client/images/codeassistant/class.gif")
   ImageResource classItem();

   @Source("org/exoplatform/ide/editor/extension/php/client/images/codeassistant/innerinterface_public.gif")
   ImageResource interfaceItem();

   @Source("org/exoplatform/ide/editor/extension/php/client/images/codeassistant/constant-item.png")
   ImageResource constantItem();

   @Source("org/exoplatform/ide/editor/extension/php/client/images/codeassistant/public-method.png")
   ImageResource publicMethod();

   @Source("org/exoplatform/ide/editor/extension/php/client/images/codeassistant/private-field.png")
   ImageResource privateField();

   @Source("org/exoplatform/ide/editor/extension/php/client/images/codeassistant/protected-field.png")
   ImageResource protectedField();

   @Source("org/exoplatform/ide/editor/extension/php/client/images/codeassistant/public-field.png")
   ImageResource publicField();

   @Source("org/exoplatform/ide/editor/extension/php/client/images/blank.png")
   ImageResource blankImage();

   @Source("org/exoplatform/ide/editor/extension/php/client/images/codeassistant/local.png")
   ImageResource variable();

   @Source("org/exoplatform/ide/editor/extension/php/client/images/codeassistant/row-selected.png")
   @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
   ImageResource itemSelected();



}
