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
package org.exoplatform.ide.client.outline;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;

import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.outline.OutlineItemCreator;
import org.exoplatform.ide.editor.api.codeassitant.Token;
import org.exoplatform.ide.editor.api.codeassitant.TokenBeenImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id:
 */
public class OutlineTreeGrid extends org.exoplatform.gwtframework.ui.client.component.Tree<TokenBeenImpl> {

    public OutlineTreeGrid() {
    }

    public OutlineTreeGrid(String id) {
        getElement().setId(id);
    }

    /** @see org.exoplatform.gwtframework.ui.client.component.Tree#doUpdateValue() */
    @Override
    public void doUpdateValue() {
        if (value == null)
            return;

        if (value.getName() == null || value.getType() == null) {
            fillTreeItems(value.getSubTokenList());
            return;
        } else {
            TreeItem addItem = tree.addItem(createItemWidget(value));
            addItem.setUserObject(value);
            addItem.getElement().setId(getIdForToken(value));
            fillTreeItems(addItem, getValue().getSubTokenList());
        }
    }

    /**
     * Generate id for token tree item for selenium testing
     *
     * @param child
     *         token
     * @return String id
     */
    private String getIdForToken(TokenBeenImpl child) {
        return child.getName() + ":" + child.getType() + ":" + child.getLineNumber();
    }

    /**
     * Create tree nodes of pointed parent and its child nodes.
     *
     * @param parentNode
     * @param children
     */
    private void fillTreeItems(TreeItem parentNode, List<TokenBeenImpl> children) {
        if (parentNode == null || children == null)
            return;
        // Clear parent node children:
        parentNode.removeItems();
        for (TokenBeenImpl child : children) {
            if (child != null && child.getName() != null && child.getType() != null) {
                TreeItem node = parentNode.addItem(createItemWidget(child));
                node.setUserObject(child);
                node.getElement().setId(getIdForToken(child));
                if (child.getSubTokenList() != null && child.getSubTokenList().size() > 0) {
                    fillTreeItems(node, child.getSubTokenList());
                }
            }
        }
    }

    /**
     * Create tree nodes in the root of the tree.
     *
     * @param children
     */
    private void fillTreeItems(List<TokenBeenImpl> children) {
        if (children == null)
            return;
        tree.removeItems();
        for (TokenBeenImpl child : children) {
            if (child != null && child.getName() != null && child.getType() != null) {
                TreeItem node = tree.addItem(createItemWidget(child));
                node.setUserObject(child);
                node.getElement().setId(getIdForToken(child));
                if (child.getSubTokenList() != null && child.getSubTokenList().size() > 0) {
                    fillTreeItems(node, child.getSubTokenList());
                }
            }
        }
    }

    /**
     * Select token in the tree.
     *
     * @param token
     */
    public void selectToken(TokenBeenImpl token) {
        if (token.getName() == null)
            return;
        TreeItem nodeToSelect = getTreeItemByToken(token);
        if (nodeToSelect == null) {
            return;
        }
        TreeItem parent = nodeToSelect.getParentItem();
        while (parent != null) {
            parent.setState(true, true);
            parent = parent.getParentItem();
        }
        tree.setSelectedItem(nodeToSelect);
    }

    /** Deselect all tokens in the tree. */
    public void deselectAllTokens() {
        tree.setSelectedItem(null);
        hideHighlighter();
    }

    /**
     * Get the the list of selected tokens in outline tree.
     *
     * @return {@link List}
     */
    public List<TokenBeenImpl> getSelectedTokens() {
        List<TokenBeenImpl> selectedItems = new ArrayList<TokenBeenImpl>();
        if (tree.getSelectedItem() != null)
            selectedItems.add((TokenBeenImpl)tree.getSelectedItem().getUserObject());
        return selectedItems;
    }

    /** @see org.exoplatform.gwtframework.ui.client.component.Tree#createItemWidget(java.lang.String, java.lang.String) */
    @Override
    protected Widget createTreeNodeWidget(Image icon, String text) {
        Grid grid = new Grid(1, 2);
        grid.setWidth("100%");

        icon.setHeight("16px");
        grid.setWidget(0, 0, icon);
        Label l = new Label();
        l.getElement().setInnerHTML(text);
        l.setWordWrap(false);
        grid.setWidget(0, 1, l);

        grid.getCellFormatter().setWidth(0, 0, "16px");
        grid.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
        grid.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT);
        grid.getCellFormatter().setWidth(0, 1, "100%");
        DOM.setStyleAttribute(grid.getElement(), "display", "block");
        return grid;
    }

    protected Widget createItemWidget(Token token) {
        OutlineItemCreator outlineItemCreator =
                IDE.getInstance().getOutlineItemCreator(((TokenBeenImpl)token).getMimeType());

        if (outlineItemCreator != null) {
            return outlineItemCreator.getOutlineItemWidget(token);
        } else {
            Grid grid = new Grid(1, 2);
            grid.setWidth("100%");

            Label l = new Label();
            l.getElement().setInnerHTML(token.getName());
            l.setWordWrap(false);
            grid.setWidget(0, 1, l);

            grid.getCellFormatter().setWidth(0, 0, "16px");
            grid.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
            grid.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT);
            grid.getCellFormatter().setWidth(0, 1, "100%");
            DOM.setStyleAttribute(grid.getElement(), "display", "block");
            return grid;
        }
    }

    /**
     * Find {@link TreeItem} in the whole of the pointed token.
     *
     * @param token
     *         token
     * @return {@link TreeItem}
     */
    private TreeItem getTreeItemByToken(TokenBeenImpl token) {
        for (int i = 0; i < tree.getItemCount(); i++) {
            TreeItem child = tree.getItem(i);
            if (child.getUserObject() == null)
                continue;
            if (((TokenBeenImpl)child.getUserObject()).getName().equals(token.getName())
                && ((TokenBeenImpl)child.getUserObject()).getLineNumber() == token.getLineNumber()) {
                return child;
            }
            TreeItem item = getChild(child, token);
            if (item != null)
                return item;
        }
        return null;
    }

    /**
     * Get child tree node of pointed parent, that represents the pointed token.
     *
     * @param parent
     *         parent
     * @param token
     *         token
     * @return {@link TreeItem}
     */
    private TreeItem getChild(TreeItem parent, TokenBeenImpl token) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            TreeItem child = parent.getChild(i);
            if (child.getUserObject() == null)
                continue;
            if (((TokenBeenImpl)child.getUserObject()).getName().equals(token.getName())
                && ((TokenBeenImpl)child.getUserObject()).getLineNumber() == token.getLineNumber()) {
                return child;
            }
            TreeItem item = getChild(child, token);
            if (item != null)
                return item;
        }
        return null;
    }

    @Override
    public void setValue(TokenBeenImpl value) {
        super.setValue(value);
        hideHighlighter();
    }

    ;

    public void setTreeGridId(String id) {
        getElement().setId(id);
    }

}
