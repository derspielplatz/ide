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
package org.exoplatform.ide.extension.cloudbees.shared;

import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public interface CloudBeesUser
{
   String getEmail();

   void setEmail(String email);

   String getFirst_name();

   void setFirst_name(String name);

   String getLast_name();

   void setLast_name(String name);

   String getName();

   void setName(String name);

   String getPassword();

   void setPassword(String password);

   //

   String getId();

   void setId(String id);

   //

   List<CloudBeesAccount> getAccounts();

   void setAccounts(List<CloudBeesAccount> accounts);

   //

   List<CloudBeesSshKey> getSsh_keys();

   void setSsh_keys(List<CloudBeesSshKey> keys);
}