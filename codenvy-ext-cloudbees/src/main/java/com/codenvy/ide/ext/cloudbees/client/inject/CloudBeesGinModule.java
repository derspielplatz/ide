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
package com.codenvy.ide.ext.cloudbees.client.inject;

import com.codenvy.ide.api.extension.ExtensionGinModule;
import com.codenvy.ide.ext.cloudbees.client.CloudBeesClientService;
import com.codenvy.ide.ext.cloudbees.client.CloudBeesClientServiceImpl;
import com.codenvy.ide.ext.cloudbees.client.account.CreateAccountView;
import com.codenvy.ide.ext.cloudbees.client.account.CreateAccountViewImpl;
import com.codenvy.ide.ext.cloudbees.client.apps.ApplicationsView;
import com.codenvy.ide.ext.cloudbees.client.apps.ApplicationsViewImpl;
import com.codenvy.ide.ext.cloudbees.client.create.CreateApplicationView;
import com.codenvy.ide.ext.cloudbees.client.create.CreateApplicationViewImpl;
import com.codenvy.ide.ext.cloudbees.client.info.ApplicationInfoView;
import com.codenvy.ide.ext.cloudbees.client.info.ApplicationInfoViewImpl;
import com.codenvy.ide.ext.cloudbees.client.login.LoginView;
import com.codenvy.ide.ext.cloudbees.client.login.LoginViewImpl;
import com.codenvy.ide.ext.cloudbees.client.project.CloudBeesProjectView;
import com.codenvy.ide.ext.cloudbees.client.project.CloudBeesProjectViewImpl;
import com.codenvy.ide.ext.cloudbees.client.wizard.CloudBeesPageView;
import com.codenvy.ide.ext.cloudbees.client.wizard.CloudBeesPageViewImpl;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

/** @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a> */
@ExtensionGinModule
public class CloudBeesGinModule extends AbstractGinModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(CloudBeesClientService.class).to(CloudBeesClientServiceImpl.class).in(Singleton.class);

        // Views
        bind(LoginView.class).to(LoginViewImpl.class).in(Singleton.class);
        bind(ApplicationsView.class).to(ApplicationsViewImpl.class).in(Singleton.class);
        bind(ApplicationInfoView.class).to(ApplicationInfoViewImpl.class).in(Singleton.class);
        bind(CreateAccountView.class).to(CreateAccountViewImpl.class).in(Singleton.class);
        bind(CloudBeesPageView.class).to(CloudBeesPageViewImpl.class).in(Singleton.class);
        bind(CreateApplicationView.class).to(CreateApplicationViewImpl.class).in(Singleton.class);
        bind(CloudBeesProjectView.class).to(CloudBeesProjectViewImpl.class).in(Singleton.class);
    }
}