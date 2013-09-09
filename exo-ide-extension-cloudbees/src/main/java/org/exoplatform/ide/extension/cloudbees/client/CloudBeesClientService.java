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
package org.exoplatform.ide.extension.cloudbees.client;

import com.google.gwt.http.client.RequestException;

import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.ide.client.framework.websocket.WebSocketException;
import org.exoplatform.ide.extension.cloudbees.shared.ApplicationInfo;
import org.exoplatform.ide.extension.cloudbees.shared.CloudBeesAccount;
import org.exoplatform.ide.extension.cloudbees.shared.CloudBeesUser;

import java.util.List;
import java.util.Map;

/**
 * Client service for CloudBees.
 *
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: CloudBessService.java Jun 23, 2011 10:11:13 AM vereshchaka $
 */
public abstract class CloudBeesClientService {

    private static CloudBeesClientService instance;

    public static CloudBeesClientService getInstance() {
        return instance;
    }

    protected CloudBeesClientService() {
        instance = this;
    }

    /**
     * Initialize application.
     *
     * @param appId
     *         application's id
     * @param vfsId
     *         virtual file system's id
     * @param projectId
     *         project's id
     * @param warFile
     *         location of the build war with application
     * @param message
     *         initialization message
     * @param callback
     *         callback
     */
    public abstract void initializeApplication(String appId, String vfsId, String projectId, String warFile,
                                               String message, CloudBeesAsyncRequestCallback<ApplicationInfo> callback)
            throws RequestException;

    /**
     * Initialize application by sending request over WebSocket.
     *
     * @param appId
     *         application's id
     * @param vfsId
     *         virtual file system's id
     * @param projectId
     *         project's id
     * @param warFile
     *         location of the build war with application
     * @param message
     *         initialization message
     * @param callback
     *         callback
     */
    public abstract void initializeApplicationWS(String appId, String vfsId, String projectId, String warFile,
                                                 String message, CloudBeesRESTfulRequestCallback<ApplicationInfo> callback)
            throws WebSocketException;

    /**
     * Get the available domains.
     *
     * @param callback
     *         - callback that client has to implement
     */
    public abstract void getDomains(CloudBeesAsyncRequestCallback<List<String>> callback) throws RequestException;

    /**
     * Login CloudBees.
     *
     * @param email
     *         user's email (login)
     * @param password
     *         user's password
     * @param callback
     *         callback
     */
    public abstract void login(String email, String password, AsyncRequestCallback<String> callback)
            throws RequestException;

    /**
     * Logout CloudBees.
     *
     * @param callback
     *         callback
     */
    public abstract void logout(AsyncRequestCallback<String> callback) throws RequestException;

    /**
     * Get the application info.
     *
     * @param appId
     *         application's id
     * @param vfsId
     *         virtual file system's id
     * @param projectId
     *         project's id
     * @param callback
     *         callback
     */
    public abstract void getApplicationInfo(String appId, String vfsId, String projectId,
                                            CloudBeesAsyncRequestCallback<ApplicationInfo> callback) throws RequestException;

    /**
     * Delete application from CloudBees.
     *
     * @param appId
     *         application's id
     * @param vfsId
     *         virtual file system's id
     * @param projectId
     *         project's id
     * @param callback
     *         callback
     */
    public abstract void deleteApplication(String appId, String vfsId, String projectId,
                                           CloudBeesAsyncRequestCallback<String> callback) throws RequestException;

    /**
     * Update application on CloudBees.
     *
     * @param appId
     *         application's id
     * @param vfsId
     *         virtual file system's id
     * @param projectId
     *         project's id
     * @param warFile
     *         location of the build war with application
     * @param message
     *         initialization message
     * @param callback
     *         callback
     */
    public abstract void updateApplication(String appId, String vfsId, String projectId, String warFile, String message,
                                           CloudBeesAsyncRequestCallback<ApplicationInfo> callback) throws RequestException;

    /**
     * Deploy war with the application.
     *
     * @param appId
     *         application's id
     * @param warFile
     *         deploy built war with the application
     * @param message
     *         message for deploying war
     * @param callback
     *         callback
     */
    public abstract void deployWar(String appId, String warFile, String message,
                                   CloudBeesAsyncRequestCallback<Map<String, String>> callback) throws RequestException;

    /** Receive all CB applications for this account. */
    public abstract void applicationList(CloudBeesAsyncRequestCallback<List<ApplicationInfo>> callback)
            throws RequestException;

    /**
     * Create new CloudBees account/domain.
     *
     * @param account
     *         CloudBees account
     * @param callback
     *         callback
     * @throws RequestException
     */
    public abstract void createAccount(CloudBeesAccount account, AsyncRequestCallback<CloudBeesAccount> callback)
            throws RequestException;

    /**
     * Adds user to CloudBees account.
     *
     * @param account
     *         account's name
     * @param user
     *         user's data
     * @param isExisting
     *         is user exists or create new one
     * @param callback
     * @throws RequestException
     */
    public abstract void addUserToAccount(String account, CloudBeesUser user, boolean isExisting,
                                          AsyncRequestCallback<CloudBeesUser> callback) throws RequestException;
}
