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

import org.eclipse.jdt.client.codeassistant.api.ICompletionProposal;
import org.eclipse.jdt.client.runtime.Assert;
import org.eclipse.jdt.client.text.IDocument;
import org.eclipse.jdt.client.text.Position;



/**
 *
 */
public class VariablePosition extends Position {

	private MultiVariableGuess fGuess;
	private MultiVariable fVariable;
   private final IDocument document;
   private final int sequence;

//	public VariablePosition(IDocument document, int offset, int length, MultiVariableGuess guess, MultiVariable variable) {
//		this(document, offset, length, guess, variable);
//	}

	public VariablePosition(IDocument document, int offset, int length, int sequence, MultiVariableGuess guess, MultiVariable variable) {
		super(offset, length);
      this.document = document;
      this.sequence = sequence;
		Assert.isNotNull(guess);
		Assert.isNotNull(variable);
		fVariable= variable;
		fGuess= guess;
	}


	/*
	 * @see org.eclipse.jface.text.link.ProposalPosition#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof VariablePosition && super.equals(o)) {
			return fGuess.equals(((VariablePosition) o).fGuess);
		}
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.link.ProposalPosition#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() | fGuess.hashCode();
	}

	/*
	 * @see org.eclipse.jface.text.link.ProposalPosition#getChoices()
	 */
	public ICompletionProposal[] getChoices() {
		return fGuess.getProposals(fVariable, offset, length);
	}

	/**
	 * Returns the variable.
	 *
	 * @return the variable.
	 */
	public MultiVariable getVariable() {
		return fVariable;
	}

}
