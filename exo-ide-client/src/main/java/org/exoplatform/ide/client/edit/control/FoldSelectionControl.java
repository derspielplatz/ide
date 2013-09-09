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
package org.exoplatform.ide.client.edit.control;

import org.exoplatform.gwtframework.ui.client.command.SimpleControl;
import org.exoplatform.ide.client.IDE;
import org.exoplatform.ide.client.IDEImageBundle;
import org.exoplatform.ide.client.framework.control.IDEControl;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler;
import org.exoplatform.ide.client.framework.editor.event.EditorFoldSelectionEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewActivatedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewActivatedHandler;
import org.exoplatform.ide.editor.client.api.EditorCapability;

/**
 * Control to make a fold from any text selection.
 * 
 * @author <a href="mailto:azatsarynnyy@codenvy.com">Artem Zatsarynnyy</a>
 * @version $Id: FoldSelectionControl.java Feb 28, 2013 5:00:20 PM azatsarynnyy $
 */
public class FoldSelectionControl extends SimpleControl implements IDEControl, EditorActiveFileChangedHandler, ViewActivatedHandler {

    public static final String  ID    = "Edit/FoldSelection";

    private static final String TITLE = IDE.IDE_LOCALIZATION_CONSTANT.foldSelectionControlTitle();

    public FoldSelectionControl() {
        super(ID);
        setTitle(TITLE);
        setPrompt(TITLE);
        setImages(IDEImageBundle.INSTANCE.blankImage(), IDEImageBundle.INSTANCE.blankImage());
        setEvent(new EditorFoldSelectionEvent());
        setShowInContextMenu(true);
    }

    /** @see org.exoplatform.ide.client.framework.control.IDEControl#initialize() */
    @Override
    public void initialize() {
        IDE.addHandler(EditorActiveFileChangedEvent.TYPE, this);
        IDE.addHandler(ViewActivatedEvent.TYPE, this);
    }

    /**
     * @see org.exoplatform.ide.client.editor.event.EditorActiveFileChangedHandler#onEditorActiveFileChanged(org.exoplatform.ide.client
     *      .editor.event.EditorActiveFileChangedEvent)
     */
    public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event) {
        if (event.getFile() == null || event.getEditor() == null) {
            setVisible(false);
            setEnabled(false);
            return;
        }

        if (event.getEditor().isReadOnly()) {
            setEnabled(false);
        }

        boolean isFoldingSupported = event.getEditor().isCapable(EditorCapability.CODE_FOLDING);
        setVisible(isFoldingSupported);
        setEnabled(isFoldingSupported);
    }

    @Override
    public void onViewActivated(ViewActivatedEvent event) {
        setShowInContextMenu(event.getView().getId().contains("editor-"));
    }
}
