/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package com.codenvy.ide.perspective;

import com.codenvy.ide.api.mvp.Presenter;
import com.codenvy.ide.api.ui.perspective.PartPresenter;
import com.codenvy.ide.api.ui.perspective.WorkBenchPresenter;
import com.codenvy.ide.api.ui.perspective.WorkBenchPresenter.PartStackType;
import com.codenvy.ide.api.ui.perspective.WorkspaceAgent;
import com.codenvy.ide.json.JsonArray;
import com.codenvy.ide.json.JsonCollections;
import com.codenvy.ide.json.JsonStringMap;
import com.codenvy.ide.json.JsonStringMap.IterationCallback;
import com.codenvy.ide.menu.MainMenuPresenter;
import com.codenvy.ide.toolbar.ToolbarPresenter;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;


/**
 * Root Presenter that implements Workspace logic. Descendant Presenters are injected via
 * constructor and exposed to coresponding UI containers.
 * It contains Menu, Toolbar and Perspective Presenter to exopse their views into corresponding places
 * and to maintain their interactions.
 *
 * @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a>
 */
@Singleton
public class WorkspacePresenter implements Presenter, WorkspaceView.ActionDelegate, WorkspaceAgent {
    private final WorkspaceView view;

    private final MainMenuPresenter menu;

    private WorkBenchPresenter activePerspective;

    private JsonStringMap<PerspectiveDescriptor> perspectives = JsonCollections.createStringMap();

    private final ToolbarPresenter toolbarPresenter;

    /**
     * Instantiates Presenter
     *
     * @param view
     * @param menu
     * @param genericPerspectiveProvider
     */
    @Inject
    protected WorkspacePresenter(WorkspaceView view, MainMenuPresenter menu, ToolbarPresenter toolbarPresenter,
                                 Provider<WorkBenchPresenter> genericPerspectiveProvider) {
        super();
        this.view = view;
        this.toolbarPresenter = toolbarPresenter;
        this.view.setDelegate(this);
        this.menu = menu;

        // register default perspective
//        registerPerspective(GENERAL_PERSPECTIVE, null, genericPerspectiveProvider);
        this.activePerspective = genericPerspectiveProvider.get();
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        // Expose Project Explorer into Tools Panel
        menu.go(view.getMenuPanel());
        toolbarPresenter.go(view.getToolbarPanel());
        activePerspective.go(view.getPerspectivePanel());
        container.setWidget(view);
    }

    /**
     * Provides active Perspective instance
     *
     * @return
     */
    public WorkBenchPresenter getActivePerspective() {
        return activePerspective;
    }

    /** {@inheritDoc} */
    @Override
    public void setActivePart(PartPresenter part) {
        activePerspective.setActivePart(part);
    }

    /** {@inheritDoc} */
    @Override
    public void showPart(PartPresenter part, PartStackType type) {
        activePerspective.openPart(part, type);
    }

    /**
     * Returns registered perspectives.
     *
     * @return
     */
    public JsonArray<PerspectiveDescriptor> getPerspectives() {
        final JsonArray<PerspectiveDescriptor> perspectives = JsonCollections.createArray();

        this.perspectives.iterate(new IterationCallback<PerspectiveDescriptor>() {
            @Override
            public void onIteration(String key, PerspectiveDescriptor value) {
                perspectives.add(value);
            }
        });

        return perspectives;
    }

    /** Wrapper containing the information about Perspective and it's Provider. */
    class PerspectiveDescriptor {
        protected String title;

        protected ImageResource icon;

        protected Provider<? extends WorkBenchPresenter> pespectiveProvider;

        public PerspectiveDescriptor(String title, ImageResource icon,
                                     Provider<? extends WorkBenchPresenter> pespectiveProvider) {
            super();
            this.title = title;
            this.icon = icon;
            this.pespectiveProvider = pespectiveProvider;
        }

        /**
         * Returns title.
         *
         * @return
         */
        public String getTitle() {
            return title;
        }

        /**
         * Returns icon.
         *
         * @return
         */
        public ImageResource getIcon() {
            return icon;
        }
    }
}
