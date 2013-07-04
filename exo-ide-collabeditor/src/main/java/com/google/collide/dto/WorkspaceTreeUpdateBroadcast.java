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

package com.google.collide.dto;

import com.codenvy.ide.dtogen.shared.RoutingType;
import com.codenvy.ide.dtogen.shared.ServerToClientDto;
import com.codenvy.ide.json.shared.JsonArray;


/** A list of mutations to the file tree that have been recorded on the sever. */
@RoutingType(type = RoutingTypes.WORKSPACETREEUPDATEBROADCAST)
public interface WorkspaceTreeUpdateBroadcast extends ServerToClientDto {

    /** The mutations that were performed. */
    JsonArray<Mutation> getMutations();

    /** The tree version after these mutations were applied. */
    String getNewTreeVersion();
}