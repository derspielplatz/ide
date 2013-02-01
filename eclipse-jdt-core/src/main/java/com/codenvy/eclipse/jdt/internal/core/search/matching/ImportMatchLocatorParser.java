/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.codenvy.eclipse.jdt.internal.core.search.matching;

import com.codenvy.eclipse.jdt.core.search.IJavaSearchConstants;
import com.codenvy.eclipse.jdt.internal.compiler.problem.ProblemReporter;

class ImportMatchLocatorParser extends MatchLocatorParser {

	boolean reportImportMatch;

protected ImportMatchLocatorParser(ProblemReporter problemReporter, MatchLocator locator) {
	super(problemReporter, locator);
	this.reportImportMatch = this.patternFineGrain == 0 || (this.patternFineGrain & IJavaSearchConstants.IMPORT_DECLARATION_TYPE_REFERENCE) != 0;
}
protected void consumeStaticImportOnDemandDeclarationName() {
	super.consumeStaticImportOnDemandDeclarationName();
	if (this.reportImportMatch) {
		this.patternLocator.match(this.astStack[this.astPtr], this.nodeSet);
	}
}
protected void consumeSingleStaticImportDeclarationName() {
	super.consumeSingleStaticImportDeclarationName();
	if (this.reportImportMatch) {
		this.patternLocator.match(this.astStack[this.astPtr], this.nodeSet);
	}
}
protected void consumeSingleTypeImportDeclarationName() {
	super.consumeSingleTypeImportDeclarationName();
	if (this.reportImportMatch) {
		this.patternLocator.match(this.astStack[this.astPtr], this.nodeSet);
	}
}
protected void consumeTypeImportOnDemandDeclarationName() {
	super.consumeTypeImportOnDemandDeclarationName();
	if (this.reportImportMatch) {
		this.patternLocator.match(this.astStack[this.astPtr], this.nodeSet);
	}
}

}
