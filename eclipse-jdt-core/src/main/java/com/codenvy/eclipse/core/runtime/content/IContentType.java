/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.codenvy.eclipse.core.runtime.content;

import com.codenvy.eclipse.core.runtime.QualifiedName;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;


/**
 * Content types represent and provide information on file types, such as
 * associated file names/extensions, default charset, etc.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @since 3.0
 */
public interface IContentType extends IContentTypeSettings
{
   /**
    * File spec type flag constant, indicating that predefined file
    * specifications should not be taken into account.
    */
   public static final int IGNORE_PRE_DEFINED = 0x01;
   /**
    * File spec type flag constant, indicating that user-defined file
    * specifications should not be taken into account.
    */
   public static final int IGNORE_USER_DEFINED = 0x02;
   /**
    * File spec type constant, indicating a file name specification.
    */
   public static final int FILE_NAME_SPEC = 0x04;
   /**
    * File spec type constant, indicating a file extension specification.
    */
   public static final int FILE_EXTENSION_SPEC = 0x08;

   /**
    * Returns a reference to this content type's base type. If this content type
    * does not have a base type (it is a root type), returns <code>null</code>.
    *
    * @return this content type's base type, or <code>null</code>
    */
   public IContentType getBaseType();

   /**
    * Returns the default content description for this content type. A default
    * content description is returned by the content type API whenever
    * content analysis could not find any particular information to be described
    * about the contents being processed, so all default attributes for the
    * content type in question apply.
    * <p>
    * Clients doing caching of content descriptions may choose to treat default
    * descriptions in a special manner, since they are easily recoverable
    * through this API.
    * </p>
    *
    * @return a content description
    * @since 3.1
    */
   public IContentDescription getDefaultDescription();

   /**
    * Tries to obtain a description for the given contents.
    * <p>
    * Any IOExceptions that may occur while reading the given input stream
    * will flow to the caller.  The input stream will not be closed by this
    * operation.
    * </p>
    *
    * @param contents the contents to be interpreted
    * @param options  an array of keys for all properties that should be described
    * @return a content description if one could be obtained, or
    *         <code>null</code>
    * @throws IOException if an error occurs while reading the contents
    * @see IContentDescription
    */
   public IContentDescription getDescriptionFor(InputStream contents, QualifiedName[] options) throws IOException;

   /**
    * Tries to obtain a description for the given contents.
    * <p>
    * Any IOExceptions that may occur while reading the given reader
    * will flow to the caller.  The reader will not be closed by this
    * operation.
    * </p>
    *
    * @param contents the contents to be interpreted
    * @param options  an array of keys for all properties that should be described
    * @return a content description if one could be obtained, or
    *         <code>null</code>
    * @throws UnsupportedOperationException if this content type
    *                                       has a describer that does not implement
    *                                       <code>ITextContentDescriber</code>
    * @throws IOException                   if an error occurs while reading the contents
    * @see IContentDescription
    */
   public IContentDescription getDescriptionFor(Reader contents, QualifiedName[] options) throws IOException;

   /**
    * Returns the default charset for this content type if one has been defined,
    * <code>null</code> otherwise.
    * This refinement of the corresponding <code>IContentTypeSettings</code>
    * method also takes into account the charset defined by the content type
    * provider (or its base content type).
    *
    * @return the default charset, or <code>null</code>
    */
   public String getDefaultCharset();

   /**
    * Returns file specifications from this content type. The type mask
    * is a bit-wise or of file specification type constants indicating the
    * file specification types of interest.
    * This refinement of the corresponding <code>IContentTypeSettings</code>
    * method supports additional flags because it also takes into account the
    * file specifications defined by the content type provider (or its base
    * content type).
    *
    * @param type a bit-wise or of file specification type constants. Valid
    *             flags are:
    *             <ul>
    *             <li>one of <code>FILE_EXTENSION_SPEC</code> or
    *             <code>FILE_NAME_SPEC</code></li>
    *             <li>and optionally, one of <code>IGNORE_PRE_DEFINED</code>
    *             or <code>IGNORE_USER_DEFINED</code></li>
    *             </ul>
    * @return the file specification
    * @see #FILE_NAME_SPEC
    * @see #FILE_EXTENSION_SPEC
    * @see #IGNORE_PRE_DEFINED
    * @see #IGNORE_USER_DEFINED
    */
   public String[] getFileSpecs(int type);

   /**
    * Returns this content type's unique identifier. Each content type has an
    * identifier by which they can be retrieved from the content type catalog.
    *
    * @return this content type's unique identifier
    */
   public String getId();

   /**
    * Returns a user-friendly name for this content type.
    *
    * @return this content type's name
    */
   public String getName();

   /**
    * Returns whether this content type is associated with the
    * given file name.
    *
    * @param fileName the file name
    * @return <code>true</code> if this content type is associated with
    *         the given file name, <code>false</code> otherwise
    * @see #isAssociatedWith(String, IScopeContext)
    */
   public boolean isAssociatedWith(String fileName);

   //   /**
   //    * Returns whether this content type is associated with the
   //    * given file name in the given preference scope.
   //    *
   //    * @param fileName the file name
   //    * @param context a preference scope context
   //    * @return <code>true</code> if this content type is associated with
   //    * the given file name, <code>false</code> otherwise
   //    * @since 3.1
   //    */
   //   public boolean isAssociatedWith(String fileName, IScopeContext context);

   /**
    * Returns whether this content type is a kind of the given content
    * type. A content type A is a kind of a content type B if:
    * <ol>
    * <li>A and B are the same content type, or</li>
    * <li>A's base type is B, or</li>
    * <li>A's base type is a kind of B.</li>
    * </ol>
    *
    * @param another a content type
    * @return <code>true</code> if this content type is a kind of the
    *         given content type, <code>false</code> otherwise
    */
   public boolean isKindOf(IContentType another);

   //   /**
   //    * Returns the settings for this content type in the given
   //    * preference context.
   //    *
   //    * @param context a preference scope context
   //    * @return setting in the given context
   //    * @throws CoreException if this method fails. Reasons include:
   //    * <ul>
   //    * <li> An error occurred obtaining the settings.</li>
   //    * </ul>
   //    * @since 3.1
   //    */
   //   public IContentTypeSettings getSettings(IScopeContext context) throws CoreException;
}
