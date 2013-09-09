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
package org.exoplatform.ide.client.ui.panel;

import com.google.gwt.event.shared.GwtEvent;

import org.exoplatform.ide.client.framework.ui.api.Panel;

/**
 * Created by The eXo Platform SAS .
 *
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class MaximizePanelEvent extends GwtEvent<MaximizePanelHandler> {

    public static final GwtEvent.Type<MaximizePanelHandler> TYPE = new GwtEvent.Type<MaximizePanelHandler>();

    private Panel panel;

    public MaximizePanelEvent(Panel panel) {
        this.panel = panel;
    }

    public Panel getPanel() {
        return panel;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<MaximizePanelHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(MaximizePanelHandler handler) {
        handler.onMaximizePanel(this);
    }

}
