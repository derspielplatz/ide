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
package com.codenvy.ide.ext.aws.client.beanstalk.environments.configuration;

import com.codenvy.ide.ext.aws.client.AWSLocalizationConstant;
import com.codenvy.ide.json.JsonArray;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author <a href="mailto:vzhukovskii@codenvy.com">Vladislav Zhukovskii</a>
 * @version $Id: $
 */
@Singleton
public class ContainerTabPainViewImpl extends Composite implements ContainerTabPainView {
    interface ContainerTabPainViewImplUiBinder extends UiBinder<Widget, ContainerTabPainViewImpl> {}

    private static ContainerTabPainViewImplUiBinder uiBinder = GWT.create(ContainerTabPainViewImplUiBinder.class);

    @UiField
    TextBox initialJVMHeapSizeField;

    @UiField
    TextBox maximumJVMHeapSizeField;

    @UiField
    TextBox maxPermSizeField;

    @UiField
    TextBox jvmOptionsField;

    @UiField(provided = true)
    AWSLocalizationConstant constant;

    private ActionDelegate delegate;

    @Inject
    protected ContainerTabPainViewImpl(AWSLocalizationConstant constant) {
        this.constant = constant;

        Widget widget = uiBinder.createAndBindUi(this);

        this.initWidget(widget);
    }

    @Override
    public void setInitialHeapSize(String heapSize) {
        initialJVMHeapSizeField.setText(heapSize);
    }

    @Override
    public String getInitialHeapSize() {
        return initialJVMHeapSizeField.getText();
    }

    @Override
    public void setMaxHeapSize(String maxHeapSize) {
        maximumJVMHeapSizeField.setText(maxHeapSize);
    }

    @Override
    public String getMaxHeapSize() {
        return maximumJVMHeapSizeField.getText();
    }

    @Override
    public void setMaxPermGenSize(String maxPermGenSize) {
        maximumJVMHeapSizeField.setText(maxPermGenSize);
    }

    @Override
    public String getMaxPermGenSize() {
        return maximumJVMHeapSizeField.getText();
    }

    @Override
    public void setJVMCommandLineOpt(String jvmCommandLineOpt) {
        jvmOptionsField.setText(jvmCommandLineOpt);
    }

    @Override
    public String getJVMCommandLineOpt() {
        return jvmOptionsField.getText();
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }
}
