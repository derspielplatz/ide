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
package org.exoplatform.ide.extension.groovy.client.service.groovy;

import com.google.gwt.http.client.Request;

import org.exoplatform.gwtframework.commons.rest.ClientRequestCallback;

/**
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: GroovyValidateCallback.java Feb 14, 2011 4:41:16 PM vereshchaka $
 *
 */
public abstract class GroovyValidateCallback extends ClientRequestCallback
{
   
   private String fileName;
   
   private String fileHref;
   
   /**
    * @return the fileName
    */
   public String getFileName()
   {
      return fileName;
   }
   
   /**
    * @param fileName the fileName to set
    */
   public void setFileName(String fileName)
   {
      this.fileName = fileName;
   }
   
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

   /**
    * @see com.google.gwt.http.client.RequestCallback#onError(com.google.gwt.http.client.Request, java.lang.Throwable)
    */
   @Override
   public void onError(Request request, Throwable exception)
   {
      fireErrorEvent(exception);
   }

   /**
    * @see org.exoplatform.gwtframework.commons.rest.ClientRequestCallback#onUnsuccess(java.lang.Throwable)
    */
   @Override
   public void onUnsuccess(Throwable exception)
   {
      fireErrorEvent(exception);
   }
   
   public abstract void fireErrorEvent(Throwable exception);

}
