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
package org.exoplatform.ide.extension.java.jdi.server.expression;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.VMCannotBeModifiedException;
import com.sun.jdi.Value;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class InstanceValue implements ExpressionValue
{
   private final ObjectReference instance;
   private final Field field;
   private Value value;

   public InstanceValue(ObjectReference instance, Field field)
   {
      this.instance = instance;
      this.field = field;
   }

   @Override
   public Value getValue()
   {
      if (value == null)
      {
         try
         {
            value = instance.getValue(field);
         }
         catch (IllegalArgumentException e)
         {
            throw new ExpressionException(e.getMessage(), e);
         }
      }
      return value;
   }

   @Override
   public void setValue(Value value)
   {
      try
      {
         instance.setValue(field, value);
      }
      catch (InvalidTypeException e)
      {
         throw new ExpressionException(e.getMessage(), e);
      }
      catch (ClassNotLoadedException e)
      {
         throw new ExpressionException(e.getMessage(), e);
      }
      catch (VMCannotBeModifiedException e)
      {
         throw new ExpressionException(e.getMessage(), e);
      }
      catch (IllegalArgumentException e)
      {
         throw new ExpressionException(e.getMessage(), e);
      }
      this.value = value;
   }
}
