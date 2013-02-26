// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.collide.server.participants;

import com.google.collide.dto.GetWorkspaceParticipantsResponse;
import com.google.collide.dto.server.DtoServerImpls.GetWorkspaceParticipantsResponseImpl;
import com.google.collide.dto.server.DtoServerImpls.ParticipantImpl;
import com.google.collide.dto.server.DtoServerImpls.ParticipantUserDetailsImpl;
import com.google.collide.dto.server.DtoServerImpls.UserDetailsImpl;
import com.google.collide.dto.server.DtoServerImpls.UserLogOutDtoImpl;
import com.google.collide.server.WSUtil;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Participants
{
   private static final Log LOG = ExoLogger.getLogger(Participants.class);

   /**
    * Map of per-user session IDs LoggedInUsers.
    */
   private final ConcurrentMap<String, LoggedInUser> loggedInUsers = new ConcurrentHashMap<String, LoggedInUser>();

   public GetWorkspaceParticipantsResponse getParticipants()
   {
      GetWorkspaceParticipantsResponseImpl resp = GetWorkspaceParticipantsResponseImpl.make();
      List<ParticipantUserDetailsImpl> collaboratorsArr = new ArrayList<ParticipantUserDetailsImpl>();

      for (LoggedInUser user : loggedInUsers.values())
      {
         final String userId = user.getId();
         final String username = user.getName();
         ParticipantUserDetailsImpl participantDetails = ParticipantUserDetailsImpl.make();
         ParticipantImpl participant = ParticipantImpl.make().setId(userId).setUserId(userId);
         UserDetailsImpl userDetails = UserDetailsImpl.make().setUserId(userId).setDisplayEmail(
            username).setDisplayName(username).setGivenName(username);

         participantDetails.setParticipant(participant);
         participantDetails.setUserDetails(userDetails);
         collaboratorsArr.add(participantDetails);
      }

      resp.setParticipants(collaboratorsArr);
      return resp;
   }

   public Set<String> getAllParticipantId()
   {
      return loggedInUsers.keySet();
   }

   public List<ParticipantUserDetailsImpl> getParticipants(Set<String> userIds)
   {
      List<ParticipantUserDetailsImpl> result = new ArrayList<ParticipantUserDetailsImpl>();

      for (LoggedInUser user : loggedInUsers.values())
      {
         final String userId = user.getId();
         if (userIds.contains(userId))
         {
            final String username = user.getName();
            ParticipantUserDetailsImpl participantDetails = ParticipantUserDetailsImpl.make();
            ParticipantImpl participant = ParticipantImpl.make().setId(userId).setUserId(userId);
            UserDetailsImpl userDetails = UserDetailsImpl.make().setUserId(userId).setDisplayEmail(
               username).setDisplayName(username).setGivenName(username);

            participantDetails.setParticipant(participant);
            participantDetails.setUserDetails(userDetails);
            result.add(participantDetails);
         }
      }

      return result;
   }

   public ParticipantUserDetailsImpl getParticipant(String userId)
   {
      LoggedInUser user = loggedInUsers.get(userId);
      if (user != null)
      {
         ParticipantUserDetailsImpl participantDetails = ParticipantUserDetailsImpl.make();
         ParticipantImpl participant = ParticipantImpl.make().setId(userId).setUserId(userId);
         UserDetailsImpl userDetails = UserDetailsImpl.make().setUserId(userId).setDisplayEmail(
            user.getName()).setDisplayName(user.getName()).setGivenName(user.getName());
         participantDetails.setParticipant(participant);
         participantDetails.setUserDetails(userDetails);
         return participantDetails;
      }
      return null;
   }

   public boolean removeParticipant(String userId)
   {
      LOG.debug("Remove participant: {} ", userId);
      if (loggedInUsers.containsKey(userId))
      {
         ParticipantUserDetailsImpl participant = getParticipant(userId);
         loggedInUsers.remove(userId);
         Set<String> allParticipantId = getAllParticipantId();
         UserLogOutDtoImpl userLogOutDto = UserLogOutDtoImpl.make();
         userLogOutDto.setParticipant((ParticipantImpl)participant.getParticipant());
         WSUtil.broadcastToClients(userLogOutDto.toJson(), allParticipantId);
         return true;
      }
      else
      {
         return false;
      }
      //      return loggedInUsers.remove(userId) != null;
   }

   public void addParticipant(LoggedInUser user)
   {
      LOG.debug("Add participant: name={}, id={} ", user.getName(), user.getId());
      loggedInUsers.putIfAbsent(user.getId(), user);
   }
}
