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
package org.exoplatform.ide.extension.cloudfoundry.client.create;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler for {@link CreateApplicationEvent} event.
 *
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: CreateApplicationHandler.java Jul 12, 2011 10:26:30 AM vereshchaka $
 */
public interface CreateApplicationHandler extends EventHandler {
    /**
     * Perform actions, when user tries to initialize application.
     *
     * @param event
     */
    void onCreateApplication(CreateApplicationEvent event);

}
