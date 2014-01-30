/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2013] Codenvy, S.A. 
 *  All Rights Reserved.
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
package com.codenvy.ide.ext.tutorials.client;

import com.codenvy.ide.api.resources.ResourceProvider;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.resources.model.Property;
import com.codenvy.ide.rest.AsyncRequest;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.ui.loader.Loader;
import com.codenvy.ide.util.Utils;
import com.google.gwt.http.client.RequestException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javax.validation.constraints.NotNull;

import static com.codenvy.ide.resources.marshal.JSONSerializer.PROPERTY_SERIALIZER;
import static com.codenvy.ide.rest.HTTPHeader.CONTENT_TYPE;
import static com.google.gwt.http.client.RequestBuilder.POST;

/**
 * Implementation of {@link TutorialsClientService}.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class TutorialsClientServiceImpl implements TutorialsClientService {
    /** Create sample project method's path. */
    private static final String CREATE_DTO_TUTORIAL                 = "/dto";
    private static final String CREATE_NOTIFICATION_TUTORIAL        = "/notification";
    private static final String CREATE_ACTION_TUTORIAL              = "/action";
    private static final String CREATE_WIZARD_TUTORIAL              = "/wizard";
    private static final String CREATE_NEW_PROJECT_WIZARD_TUTORIAL  = "/newproject";
    private static final String CREATE_NEW_RESOURCE_WIZARD_TUTORIAL = "/newresource";
    private static final String CREATE_PARTS_TUTORIAL               = "/parts";
    private static final String CREATE_EDITOR_TUTORIAL              = "/editor";
    private static final String CREATE_GIN_TUTORIAL                 = "/gin";
    private static final String CREATE_WYSIWYG_TUTORIAL             = "/wysiwyg";
    /** REST-service context. */
    private String           baseUrl;
    /** Loader to be displayed. */
    private Loader           loader;
    /** Provider of Codenvy IDE resources. */
    private ResourceProvider resourceProvider;

    /**
     * Creates service.
     *
     * @param baseUrl
     *         REST-service context
     * @param loader
     *         loader to show on server request
     * @param resourceProvider
     *         provider of IDE resources
     */
    @Inject
    protected TutorialsClientServiceImpl(@Named("restContext") String baseUrl, Loader loader, ResourceProvider resourceProvider) {
        this.loader = loader;
        this.baseUrl = baseUrl + "/tutorials/" + Utils.getWorkspaceId();
        this.resourceProvider = resourceProvider;
    }

    /** {@inheritDoc} */
    @Override
    public void createDTOTutorialProject(@NotNull String projectName, @NotNull Array<Property> properties,
                                         @NotNull AsyncRequestCallback<Void> callback) throws RequestException {
        String requestUrl = baseUrl + CREATE_DTO_TUTORIAL;
        sendRequest(requestUrl, projectName, properties, callback);
    }

    /** {@inheritDoc} */
    @Override
    public void createNotificationTutorialProject(@NotNull String projectName, @NotNull Array<Property> properties,
                                                  @NotNull AsyncRequestCallback<Void> callback) throws RequestException {
        String requestUrl = baseUrl + CREATE_NOTIFICATION_TUTORIAL;
        sendRequest(requestUrl, projectName, properties, callback);
    }

    /** {@inheritDoc} */
    @Override
    public void createActionTutorialProject(@NotNull String projectName, @NotNull Array<Property> properties,
                                            @NotNull AsyncRequestCallback<Void> callback) throws RequestException {
        String requestUrl = baseUrl + CREATE_ACTION_TUTORIAL;
        sendRequest(requestUrl, projectName, properties, callback);
    }

    /** {@inheritDoc} */
    @Override
    public void createWizardTutorialProject(@NotNull String projectName, @NotNull Array<Property> properties,
                                            @NotNull AsyncRequestCallback<Void> callback) throws RequestException {
        String requestUrl = baseUrl + CREATE_WIZARD_TUTORIAL;
        sendRequest(requestUrl, projectName, properties, callback);
    }

    /** {@inheritDoc} */
    @Override
    public void createNewProjectWizardTutorialProject(@NotNull String projectName, @NotNull Array<Property> properties,
                                                      @NotNull AsyncRequestCallback<Void> callback) throws RequestException {
        String requestUrl = baseUrl + CREATE_NEW_PROJECT_WIZARD_TUTORIAL;
        sendRequest(requestUrl, projectName, properties, callback);
    }

    /** {@inheritDoc} */
    @Override
    public void createNewResourceWizardTutorialProject(@NotNull String projectName, @NotNull Array<Property> properties,
                                                       @NotNull AsyncRequestCallback<Void> callback) throws RequestException {
        String requestUrl = baseUrl + CREATE_NEW_RESOURCE_WIZARD_TUTORIAL;
        sendRequest(requestUrl, projectName, properties, callback);
    }

    /** {@inheritDoc} */
    @Override
    public void createPartsTutorialProject(@NotNull String projectName, @NotNull Array<Property> properties,
                                           @NotNull AsyncRequestCallback<Void> callback) throws RequestException {
        String requestUrl = baseUrl + CREATE_PARTS_TUTORIAL;
        sendRequest(requestUrl, projectName, properties, callback);
    }

    /** {@inheritDoc} */
    @Override
    public void createEditorTutorialProject(@NotNull String projectName, @NotNull Array<Property> properties,
                                            @NotNull AsyncRequestCallback<Void> callback) throws RequestException {
        String requestUrl = baseUrl + CREATE_EDITOR_TUTORIAL;
        sendRequest(requestUrl, projectName, properties, callback);
    }

    /** {@inheritDoc} */
    @Override
    public void createGinTutorialProject(@NotNull String projectName, @NotNull Array<Property> properties,
                                         @NotNull AsyncRequestCallback<Void> callback) throws RequestException {
        String requestUrl = baseUrl + CREATE_GIN_TUTORIAL;
        sendRequest(requestUrl, projectName, properties, callback);
    }

    @Override
    public void createWYSIWYGEditorProject(@NotNull String projectName, @NotNull Array<Property> properties,
                                           @NotNull AsyncRequestCallback<Void> callback) throws RequestException {
        String requestUrl = baseUrl + CREATE_WYSIWYG_TUTORIAL;
        sendRequest(requestUrl, projectName, properties, callback);
    }

    /**
     * Send request for given request url.
     *
     * @param requestUrl
     *         url where request need to be sent
     * @param projectName
     *         name of the project to create
     * @param properties
     *         properties to set to a newly created project
     * @param callback
     *         callback
     * @throws RequestException
     */
    private void sendRequest(@NotNull String requestUrl, @NotNull String projectName, @NotNull Array<Property> properties,
                             @NotNull AsyncRequestCallback<Void> callback) throws RequestException {
        final String param = "?vfsid=" + resourceProvider.getVfsInfo().getId() + "&name=" + projectName;
        loader.setMessage("Creating new project...");
        AsyncRequest.build(POST, requestUrl + param)
                    .data(PROPERTY_SERIALIZER.fromCollection(properties).toString())
                    .header(CONTENT_TYPE, "application/json").loader(loader).send(callback);
    }
}