/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.client.templates;

import org.eclipse.jdt.client.core.dom.CompilationUnit;
import org.eclipse.jdt.client.templates.api.GlobalTemplateVariables;
import org.eclipse.jdt.client.templates.api.SimpleTemplateVariableResolver;
import org.eclipse.jdt.client.templates.api.TemplateContext;
import org.exoplatform.ide.editor.shared.text.IDocument;
import org.exoplatform.ide.editor.shared.text.Position;

/** The context type for templates inside Javadoc. */
public class JavaDocContextType extends CompilationUnitContextType {

    /**
     * The word selection variable determines templates that work on a full lines selection.
     * <p>
     * This class contains additional description that tells about the 'Source &gt; Surround With > ...' menu.
     * </p>
     *
     * @see org.eclipse.jface.text.templates.GlobalTemplateVariables.WordSelection
     * @since 3.7
     */
    protected static class SurroundWithWordSelection extends SimpleTemplateVariableResolver {

        /** Creates a new word selection variable */
        public SurroundWithWordSelection() {
            super(
                    org.eclipse.jdt.client.templates.api.GlobalTemplateVariables.WordSelection.NAME,
                    "The selected word<br><br>Javadoc templates that contain this variable will also be shown in the 'Source &gt; " +
                    "Surround With > ...' menu.");
        }

        @Override
        protected String resolve(TemplateContext context) {
            String selection = context.getVariable(org.eclipse.jdt.client.templates.api.GlobalTemplateVariables.SELECTION);
            if (selection == null)
                return ""; //$NON-NLS-1$
            return selection;
        }
    }

    /** The id under which this context type is registered */
    public static final String ID = "javadoc"; //$NON-NLS-1$

    /** Creates a java context type. */
    public JavaDocContextType() {
        super(ID);
        // global
        addResolver(new GlobalTemplateVariables.Cursor());
        // addResolver(new SurroundWithLineSelection());
        addResolver(new SurroundWithWordSelection());
        addResolver(new GlobalTemplateVariables.Dollar());
        addResolver(new GlobalTemplateVariables.Date());
        addResolver(new GlobalTemplateVariables.Year());
        addResolver(new GlobalTemplateVariables.Time());
        addResolver(new GlobalTemplateVariables.User());
        // TODO
        // // compilation unit
        // addResolver(new File());
        // addResolver(new PrimaryTypeName());
        // addResolver(new Method());
        // addResolver(new ReturnType());
        // addResolver(new Arguments());
        // addResolver(new Type());
        // addResolver(new Package());
        // addResolver(new Project());
    }

    /*
     * @see
     * org.eclipse.jdt.internal.corext.template.java.CompilationUnitContextType#createContext(org.eclipse.jface.text.IDocument,
     * int, int, org.eclipse.jdt.core.ICompilationUnit)
     */
    @Override
    public CompilationUnitContext createContext(IDocument document, int offset, int length,
                                                CompilationUnit compilationUnit) {
        return new JavaDocContext(this, document, offset, length, compilationUnit);
    }

    /*
     * @see
     * org.eclipse.jdt.internal.corext.template.java.CompilationUnitContextType#createContext(org.eclipse.jface.text.IDocument,
     * org.eclipse.jface.text.Position, org.eclipse.jdt.core.ICompilationUnit)
     */
    @Override
    public CompilationUnitContext createContext(IDocument document, Position completionPosition,
                                                CompilationUnit compilationUnit) {
        return new JavaDocContext(this, document, completionPosition, compilationUnit);
    }
}