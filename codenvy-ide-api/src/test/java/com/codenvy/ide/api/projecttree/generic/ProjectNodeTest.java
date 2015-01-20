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
package com.codenvy.ide.api.projecttree.generic;

import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;

import static com.codenvy.ide.api.projecttree.TreeNode.DeleteCallback;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link ProjectNode} functionality.
 *
 * @author Artem Zatsarynnyy
 */
public class ProjectNodeTest extends BaseNodeTest {
    private static final String ITEM_PATH       = "/project_name";
    private static final String ITEM_NAME       = "project_name";
    private static final String PROJECT_TYPE_ID = "project_type";
    @Mock
    private ProjectDescriptor projectDescriptor;
    @Mock
    private ProjectNode       parentProjectNode;
    @InjectMocks
    private ProjectNode       projectNode;

    @Before
    public void setUp() {
        super.setUp();

        when(projectDescriptor.getPath()).thenReturn(ITEM_PATH);
        when(projectDescriptor.getName()).thenReturn(ITEM_NAME);
        when(projectDescriptor.getType()).thenReturn(PROJECT_TYPE_ID);

        Array<TreeNode<?>> children = Collections.createArray();
        when(parentProjectNode.getChildren()).thenReturn(children);
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals(ITEM_NAME, projectNode.getName());
    }

    @Test
    public void testGetPath() throws Exception {
        assertEquals(ITEM_PATH, projectNode.getPath());
    }

    @Test
    public void testGetProject() throws Exception {
        assertEquals(projectNode, projectNode.getProject());
    }

    @Test
    public void shouldNotBeLeaf() throws Exception {
        assertFalse(projectNode.isLeaf());
    }

    @Test
    public void shouldBeRenemable() throws Exception {
        assertTrue(projectNode.isRenamable());
    }

    @Test
    public void shouldBeDeletable() throws Exception {
        assertTrue(projectNode.isDeletable());
    }

    @Test
    public void testDeleteWhenDeleteIsSuccessful() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[1];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, (Void)null);
                return callback;
            }
        }).when(projectServiceClient).delete(anyString(), (AsyncRequestCallback<Void>)anyObject());
        DeleteCallback callback = mock(DeleteCallback.class);

        projectNode.delete(callback);

        verify(projectServiceClient).delete(eq(ITEM_PATH), Matchers.<AsyncRequestCallback<Void>>anyObject());
        verify(callback).onDeleted();
    }

    @Test
    public void testDeleteWhenDeleteIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[1];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(projectServiceClient).delete(anyString(), (AsyncRequestCallback<Void>)anyObject());
        DeleteCallback callback = mock(DeleteCallback.class);

        projectNode.delete(callback);

        verify(projectServiceClient).delete(eq(ITEM_PATH), Matchers.<AsyncRequestCallback<Void>>anyObject());
        verify(callback).onFailure(Matchers.<Throwable>anyObject());
    }

    @Test
    public void shouldReturnProjectTypeId() throws Exception {
        assertEquals(projectDescriptor.getType(), projectNode.getProjectTypeId());
    }

    @Test
    public void shouldCreateChildFileNode() {
        ItemReference fileItem = mock(ItemReference.class);
        when(fileItem.getType()).thenReturn("file");

        projectNode.createChildNode(fileItem);

        verify(treeStructure).newFileNode(eq(projectNode), eq(fileItem));
    }

    @Test
    public void shouldCreateChildFolderNode() {
        ItemReference folderItem = mock(ItemReference.class);
        when(folderItem.getType()).thenReturn("folder");

        projectNode.createChildNode(folderItem);

        verify(treeStructure).newFolderNode(eq(projectNode), eq(folderItem));
    }
}
