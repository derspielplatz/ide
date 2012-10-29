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
package org.exoplatform.ide.client.framework.editor.event;

import com.google.gwt.event.shared.GwtEvent;

import org.exoplatform.ide.vfs.client.model.FileModel;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: $
 */
public class EditorReplaceFileEvent extends GwtEvent<EditorReplaceFileHandler>
{

   public static final GwtEvent.Type<EditorReplaceFileHandler> TYPE = new GwtEvent.Type<EditorReplaceFileHandler>();

   private FileModel file;

   private FileModel newFile;

   public EditorReplaceFileEvent(FileModel file, FileModel newFile)
   {
      this.file = file;
      this.newFile = newFile;
   }

   public FileModel getFile()
   {
      return file;
   }

   public FileModel getNewFile()
   {
      return newFile;
   }

   @Override
   protected void dispatch(EditorReplaceFileHandler handler)
   {
      handler.onEditorReplaceFile(this);
   }

   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<EditorReplaceFileHandler> getAssociatedType()
   {
      return TYPE;
   }

}