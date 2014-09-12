/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.api.projecttree.generic;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * An interface defines methods common to all nodes represent an item
 * retrieved from Codenvy Project API (e.g. file, folder, project, module).
 *
 * @author Artem Zatsarynnyy
 */
public interface StorableNode {
    /** Returns name of the item which this node represents. */
    String getName();

    /** Returns path of the item which this node represents. */
    String getPath();

    /**
     * Provides a way to rename node.
     *
     * @param newName
     *         new name
     * @param callback
     *         callback to return result
     */
    void rename(String newName, AsyncCallback<Void> callback);

    /**
     * Provides a way to delete node.
     *
     * @param callback
     *         callback to return result
     */
    public void delete(AsyncCallback<Void> callback);
}
