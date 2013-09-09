/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package org.exoplatform.ide.extension.gadget.server.opensocial.service;

import org.exoplatform.ide.extension.gadget.server.opensocial.model.AppData;
import org.exoplatform.ide.extension.gadget.server.opensocial.model.EscapeType;

import java.util.List;

/**
 * Service to manipulate with applications data, used for reading and writing user-specific data
 *
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Nov 19, 2010 $
 */
public interface AppDataService {
    /**
     * Retrieve AppData.
     *
     * @param userId
     *         user ID of the person whose AppData is to be returned
     * @param groupId
     *         group ID of the group of users whose AppData is to be returned
     * @param appId
     *         Specifies that the response should only contain AppData generated by the given appId (optional)
     * @param fields
     *         list of AppData keys specifying the fields to retrieve
     * @param escapeType
     *         specifies the type of escaping to use on AppData values
     * @return {@link AppData} application data
     */
    AppData getAppData(String userId, String groupId, String appId, List<String> fields, EscapeType escapeType);

    /**
     * Create application data.
     *
     * @param userId
     *         user ID of the person to associate the AppData with
     * @param appId
     *         specifies that the response should only contain AppData generated by the given appId (optional)
     * @param appData
     *         AppData to create
     * @return {@link AppData} created application data
     */
    AppData createAppData(String userId, String appId, AppData appData);

    /**
     * Update application data.
     *
     * @param userId
     *         user ID of the person to associate the AppData with
     * @param appId
     *         specifies that the response should only contain AppData generated by the given appId (optional)
     * @param appData
     *         AppData to update
     */
    void updateAppData(String userId, String appId, AppData appData);

    /**
     * Remove AppData for the currently authenticated user. If the request is successful, the container MUST return the AppData
     * that was removed.
     *
     * @param userId
     *         user ID of the person the AppData belongs to
     * @param appId
     *         Specifies that the response should only contain AppData generated by the given appId (optional)
     * @param keys
     *         keys of the AppData to delete
     * @return {@link AppData} removed application data
     */
    AppData deleteAppData(String userId, String appId, List<String> keys);
}
