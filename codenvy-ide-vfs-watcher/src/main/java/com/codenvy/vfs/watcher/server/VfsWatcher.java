/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package com.codenvy.vfs.watcher.server;

import com.codenvy.vfs.dto.Item.ItemType;
import com.codenvy.vfs.dto.ProjectClosedDto;
import com.codenvy.vfs.dto.ProjectOpenedDto;
import com.codenvy.vfs.dto.server.DtoServerImpls.ItemCreatedDtoImpl;
import com.codenvy.vfs.dto.server.DtoServerImpls.ItemDeletedDtoImpl;
import com.codenvy.vfs.dto.server.DtoServerImpls.ItemImpl;
import com.codenvy.vfs.dto.server.DtoServerImpls.ItemMovedDtoImpl;
import com.codenvy.vfs.dto.server.DtoServerImpls.ItemRenamedDtoImpl;
import com.codenvy.vfs.dto.server.DtoServerImpls.LinkImpl;
import com.codenvy.vfs.dto.server.DtoServerImpls.PropertyImpl;
import com.google.gson.internal.Pair;

import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.exoplatform.ide.vfs.server.VirtualFileSystem;
import org.exoplatform.ide.vfs.server.VirtualFileSystemRegistry;
import org.exoplatform.ide.vfs.server.exceptions.VirtualFileSystemException;
import org.exoplatform.ide.vfs.server.observation.ChangeEvent;
import org.exoplatform.ide.vfs.server.observation.ChangeEvent.ChangeType;
import org.exoplatform.ide.vfs.server.observation.ChangeEventFilter;
import org.exoplatform.ide.vfs.server.observation.EventListener;
import org.exoplatform.ide.vfs.server.observation.EventListenerList;
import org.exoplatform.ide.vfs.server.observation.PathFilter;
import org.exoplatform.ide.vfs.server.observation.TypeFilter;
import org.exoplatform.ide.vfs.server.observation.VfsIDFilter;
import org.exoplatform.ide.vfs.shared.Item;
import org.exoplatform.ide.vfs.shared.Link;
import org.exoplatform.ide.vfs.shared.Property;
import org.exoplatform.ide.vfs.shared.PropertyFilter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author <a href="mailto:evidolob@codenvy.com">Evgen Vidolob</a>
 * @version $Id:
 */
public class VfsWatcher
{

   private class EventListenerImpl implements EventListener
   {

      private String projectId;

      private EventListenerImpl(String projectId)
      {
         this.projectId = projectId;
      }

      @Override
      public void handleEvent(ChangeEvent event) throws VirtualFileSystemException
      {
         notifyUsers(event);
      }

      private void notifyUsers(ChangeEvent event)
      {
         String message;
         switch (event.getType())
         {
            case CREATED:
               ItemCreatedDtoImpl cDto = ItemCreatedDtoImpl.make();
               cDto.setItem(getDtoItem(event.getVirtualFileSystem(), event.getItemId()));
               message = cDto.toJson();
               break;
            case DELETED:
               ItemDeletedDtoImpl dto = ItemDeletedDtoImpl.make();
               dto.setFileId(event.getItemId());
               dto.setFilePath(event.getItemPath());
               message = dto.toJson();
               break;
            case MOVED:
               ItemMovedDtoImpl movedDto = ItemMovedDtoImpl.make();
               movedDto.setOldPath(event.getOldItemPath());
               movedDto.setMovedItem(getDtoItem(event.getVirtualFileSystem(),event.getItemId()));
               message = movedDto.toJson();
               break;
            case RENAMED:
               ItemRenamedDtoImpl renamedDto = ItemRenamedDtoImpl.make();
               renamedDto.setOldPath(event.getOldItemPath());
               renamedDto.setRenamedItem(getDtoItem(event.getVirtualFileSystem(), event.getItemId()));
               message = renamedDto.toJson();
               break;
            default:
               return;
         }
         Set<String> userIds = projectUsers.get(projectId);
         if (message != null && userIds != null)
         {
            broadcastToClients(message, userIds);
         }
      }
   }

   private ItemImpl getDtoItem(VirtualFileSystem vfs, String itemId)
   {
      try
      {
         Item item = vfs.getItem(itemId, PropertyFilter.ALL_FILTER);
         ItemImpl dtoItem = ItemImpl.make();
         dtoItem.setId(item.getId());
         dtoItem.setItemType(ItemType.fromValue(item.getItemType().value()));
         dtoItem.setMimeType(item.getMimeType());
         dtoItem.setName(item.getName());
         dtoItem.setPath(item.getPath());
         dtoItem.setParentId(item.getParentId());
         dtoItem.setProperties(convertProperties(item.getProperties()));
         dtoItem.setLinks(convertLinks(item.getLinks()));
         return dtoItem;
      }
      catch (VirtualFileSystemException e)
      {
         LOG.error("Can't find item: " + itemId, e);
      }
      return null;
   }

   private Map<String, LinkImpl> convertLinks(Map<String, Link> links)
   {
      Map<String, LinkImpl> converted = new HashMap<String, LinkImpl>(links.size());

      for(String key : links.keySet())
      {
         LinkImpl link = LinkImpl.make();
         Link l = links.get(key);
         link.setHref(l.getHref());
         link.setRel(l.getRel());
         link.setTypeLink(l.getType());
         converted.put(key, link);
      }
      return converted;
   }

   private List<PropertyImpl> convertProperties(List<Property> properties)
   {
      List<PropertyImpl> prop = new ArrayList<PropertyImpl>(properties.size());
      for(Property p : properties)
      {
         PropertyImpl property = PropertyImpl.make();
         property.setName(p.getName());
         property.setValue(p.getValue());
         prop.add(property);
      }
      return prop;
   }

   private static final Log LOG = ExoLogger.getLogger(VfsWatcher.class);

   /**
    * Map of per project listener.
    */
   private final ConcurrentMap<String, Pair<ChangeEventFilter, EventListener>> vfsListeners = new ConcurrentHashMap<String, Pair<ChangeEventFilter, EventListener>>();

   private final ConcurrentMap<String, Set<String>> projectUsers = new ConcurrentHashMap<String, Set<String>>();

   private VirtualFileSystemRegistry vfsRegistry;

   private EventListenerList listeners;

   public VfsWatcher(VirtualFileSystemRegistry vfsRegistry, EventListenerList listeners)
   {
      this.vfsRegistry = vfsRegistry;

      this.listeners = listeners;
   }

   public void openProject(String clientId, ProjectOpenedDto dto)
   {

      if (!projectUsers.containsKey(dto.projectId()))
      {
         projectUsers.put(dto.projectId(), new CopyOnWriteArraySet<String>());
         addListenerToProject(dto.projectId(), dto.vfsId(), dto.projectPath());
      }
      projectUsers.get(dto.projectId()).add(clientId);
   }

   private void addListenerToProject(String projectId, String vfsId, String projectPath)
   {
      LOG.debug("Add VFS listener for {} project", projectPath);
      EventListenerImpl eventListener = new EventListenerImpl(projectId);

      ChangeEventFilter filter = ChangeEventFilter.createAndFilter(new VfsIDFilter(vfsId),
         new PathFilter(projectPath + "/.*"), ChangeEventFilter.createOrFilter( // created, deleted, renamed or moved
         new TypeFilter(ChangeType.CREATED),//
         new TypeFilter(ChangeType.DELETED),//
         new TypeFilter(ChangeType.RENAMED),//
         new TypeFilter(ChangeType.MOVED)));

      listeners.addEventListener(filter, eventListener);
      Pair<ChangeEventFilter, EventListener> pair = new Pair<ChangeEventFilter, EventListener>(filter, eventListener);
      vfsListeners.putIfAbsent(projectId, pair);

   }

   public void closeProject(String clientId, ProjectClosedDto dto)
   {
      if (projectUsers.containsKey(dto.projectId()))
      {
         Set<String> ids = projectUsers.get(dto.projectId());
         ids.remove(clientId);
         if (ids.isEmpty())
         {
            LOG.debug("Remove VFS listener for {} project", dto.projectPath());
            projectUsers.remove(dto.projectId());
            Pair<ChangeEventFilter, EventListener> pair = vfsListeners.remove(dto.projectId());
            listeners.removeEventListener(pair.first, pair.second);
         }
      }
   }

   private static void broadcastToClients(String message, Set<String> collaborators)
   {
      for (String collaborator : collaborators)
      {
         ChannelBroadcastMessage broadcastMessage = new ChannelBroadcastMessage();
         broadcastMessage.setChannel("vfs_watcher." + collaborator);
         broadcastMessage.setBody(message);
         try
         {
            WSConnectionContext.sendMessage(broadcastMessage);
         }
         catch (Exception e)
         {
            LOG.error(e.getMessage(), e);
         }
      }
   }

}
