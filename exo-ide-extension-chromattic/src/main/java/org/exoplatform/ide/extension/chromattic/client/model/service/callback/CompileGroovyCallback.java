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
package org.exoplatform.ide.extension.chromattic.client.model.service.callback;

import org.exoplatform.gwtframework.commons.rest.ClientRequestCallback;

/**
 * Abstract class a caller must implement to receive a response.
 * 
 * Used to compile groovy.
 * 
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: CreateNodeTypeCallback.java Feb 14, 2011 10:11:36 AM vereshchaka $
 *
 */
public abstract class CompileGroovyCallback extends ClientRequestCallback
{
   /**
    * Href of file to compile.
    */
   private String fileHref;
   
   /**
    * @return the fileHref
    */
   public String getFileHref()
   {
      return fileHref;
   }
   
   /**
    * @param fileHref the fileHref to set
    */
   public void setFileHref(String fileHref)
   {
      this.fileHref = fileHref;
   }

}
