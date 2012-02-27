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

package org.exoplatform.ide.client.marking;

import org.exoplatform.ide.editor.problem.Problem;

/**
 * 
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class ProblemError implements Problem
{
   
   private int id;
   
   private String message;
   
   private int lineNumber;
   
   private int start;
   
   private int end;
   
   public ProblemError(int id, String message, int lineNumber, int start, int end) {
      this.id = id;
      this.message = message;
      this.lineNumber = lineNumber;
      this.start = start;
      this.end = end;
   }

   @Override
   public int getID()
   {
      return id;
   }

   @Override
   public String getMessage()
   {
      return message;
   }

   @Override
   public int getLineNumber()
   {
      return lineNumber;
   }

   @Override
   public int getEnd()
   {
      return end;
   }

   @Override
   public int getStart()
   {
      return start;
   }

   @Override
   public boolean isError()
   {
      return true;
   }

   @Override
   public boolean isWarning()
   {
      return false;
   }

}
