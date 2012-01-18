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
package org.eclipse.jdt.client.internal.core;

import org.eclipse.jdt.client.core.compiler.CategorizedProblem;
import org.eclipse.jdt.client.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.client.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.client.runtime.IProgressMonitor;
import org.eclipse.jdt.client.runtime.OperationCanceledException;

public class CancelableProblemFactory extends DefaultProblemFactory
{
   public IProgressMonitor monitor;

   public CancelableProblemFactory(IProgressMonitor monitor)
   {
      super();
      this.monitor = monitor;
   }

   public CategorizedProblem createProblem(char[] originatingFileName, int problemId, String[] problemArguments,
      String[] messageArguments, int severity, int startPosition, int endPosition, int lineNumber, int columnNumber)
   {
      if (this.monitor != null && this.monitor.isCanceled())
         throw new AbortCompilation(true/* silent */, new OperationCanceledException());
      return super.createProblem(originatingFileName, problemId, problemArguments, messageArguments, severity,
         startPosition, endPosition, lineNumber, columnNumber);
   }

   public CategorizedProblem createProblem(char[] originatingFileName, int problemId, String[] problemArguments,
      int elaborationId, String[] messageArguments, int severity, int startPosition, int endPosition, int lineNumber,
      int columnNumber)
   {
      if (this.monitor != null && this.monitor.isCanceled())
         throw new AbortCompilation(true/* silent */, new OperationCanceledException());
      return super.createProblem(originatingFileName, problemId, problemArguments, elaborationId, messageArguments,
         severity, startPosition, endPosition, lineNumber, columnNumber);
   }
}