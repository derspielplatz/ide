/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.api.wizard1;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The main responsibility of a {@link Wizard} subclass is doing the real work when the wizard finishes                                                                                              .
 *
 * @param <T>
 *         the type of the data object
 * @author Artem Zatsarynnyy
 */
public interface Wizard<T> {
    /**
     * Performs some actions required for flipping to first page and returning to the first page of a wizard.
     *
     * @return first page
     */
    @Nullable
    WizardPage<T> flipToFirst();

    /**
     * Performs actions required for flipping to next page and return next page of wizard.
     *
     * @return if the wizard has next page, it returns next page, otherwise returns <code>null</code>.
     */
    @Nullable
    WizardPage<T> flipToNext();

    /**
     * Performs some actions required for jumping to the previous page and returning to the previous page of a wizard.
     *
     * @return if the wizard has previous page, it returns to the previous page, otherwise return <code>null</code>.
     */
    @Nullable
    WizardPage<T> flipToPrevious();

    /**
     * Returns whether the wizard has the next page.
     * The result of this method is typically used by the wizard container to enable or disable the Next button.
     *
     * @return <code>true</code> if the wizard has next page, and <code>false</code> otherwise
     */
    boolean hasNext();

    /**
     * Returns whether the wizard has previous page.
     * The result of this method is typically used by the wizard container to enable or disable the Back button.
     *
     * @return <code>true</code> if the wizard has previous page, and <code>false</code> otherwise
     */
    boolean hasPrevious();

    /**
     * Returns whether the wizard could be finished without further user interaction.
     * The result of this method is typically used by the wizard container to enable or disable the Finish button.
     *
     * @return <code>true</code> if the wizard could be finished, and <code>false</code> otherwise
     */
    boolean canFinish();

    /** Performs some actions in response to a user's click on Finish button. */
    void onFinish();

    /**
     * Sets update control delegate.
     *
     * @param delegate
     */
    void setUpdateDelegate(@Nonnull UpdateDelegate delegate);

    /** Required for delegating update function in a wizard model. */
    interface UpdateDelegate {
        /** Performs some actions in response to a user's updating controls. */
        void updateControls();
    }
}
