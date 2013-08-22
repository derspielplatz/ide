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
package com.codenvy.ide.ext.git.client.init;

import com.codenvy.ide.annotations.NotNull;
import com.codenvy.ide.api.event.RefreshBrowserEvent;
import com.codenvy.ide.api.parts.ConsolePart;
import com.codenvy.ide.api.resources.ResourceProvider;
import com.codenvy.ide.ext.git.client.GitClientService;
import com.codenvy.ide.ext.git.client.GitLocalizationConstant;
import com.codenvy.ide.resources.model.Project;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.util.loging.Log;
import com.codenvy.ide.websocket.WebSocketException;
import com.codenvy.ide.websocket.rest.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Presenter for Git command Init Repository.
 *
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Mar 24, 2011 9:07:58 AM anya $
 */
@Singleton
public class InitRepositoryPresenter implements InitRepositoryView.ActionDelegate {
    private InitRepositoryView      view;
    private GitClientService        service;
    private Project                 project;
    private ResourceProvider        resourceProvider;
    private EventBus                eventBus;
    private ConsolePart             console;
    private GitLocalizationConstant constant;

    /**
     * Create presenter.
     *
     * @param view
     * @param service
     * @param resourceProvider
     * @param eventBus
     * @param console
     * @param constant
     */
    @Inject
    public InitRepositoryPresenter(InitRepositoryView view, GitClientService service, ResourceProvider resourceProvider,
                                   EventBus eventBus, ConsolePart console, GitLocalizationConstant constant) {
        this.view = view;
        this.view.setDelegate(this);
        this.service = service;
        this.resourceProvider = resourceProvider;
        this.eventBus = eventBus;
        this.console = console;
        this.constant = constant;
    }

    /** Show dialog. */
    public void showDialog() {
        project = resourceProvider.getActiveProject();

        view.setWorkDir(project.getPath());
        view.setBare(false);
        view.setEnableOkButton(true);
        view.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onOkClicked() {
        boolean bare = view.isBare();
        view.close();

        try {
            service.initWS(resourceProvider.getVfsId(), project.getId(), project.getName(), bare, new RequestCallback<String>() {
                @Override
                protected void onSuccess(String result) {
                    onInitSuccess();
                }

                @Override
                protected void onFailure(Throwable exception) {
                    handleError(exception);
                }
            });
        } catch (WebSocketException e) {
            initRepositoryREST(project.getId(), project.getName(), bare);
        }
    }

    /** Initialize of the repository (sends request over HTTP). */
    private void initRepositoryREST(@NotNull String projectId, @NotNull String projectName, boolean bare) {
        try {
            service.init(resourceProvider.getVfsId(), projectId, projectName, bare, new AsyncRequestCallback<String>() {
                @Override
                protected void onSuccess(String result) {
                    onInitSuccess();
                }

                @Override
                protected void onFailure(Throwable exception) {
                    handleError(exception);
                }
            });
        } catch (RequestException e) {
            handleError(e);
        }
    }

    /** Perform actions when repository was successfully init. */
    private void onInitSuccess() {
        project.refreshProperties(new AsyncCallback<Project>() {
            @Override
            public void onSuccess(Project result) {
                console.print(constant.initSuccess());
                eventBus.fireEvent(new RefreshBrowserEvent(project));
            }

            @Override
            public void onFailure(Throwable caught) {
                Log.error(InitRepositoryPresenter.class, caught);
            }
        });
    }

    /**
     * Handler some action whether some exception happened.
     *
     * @param e
     *         exception what happened
     */
    private void handleError(@NotNull Throwable e) {
        String errorMessage = (e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : constant.initFailed();
        console.print(errorMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onValueChanged() {
        String workDir = view.getWorkDir();
        view.setEnableOkButton(!workDir.isEmpty());
    }
}