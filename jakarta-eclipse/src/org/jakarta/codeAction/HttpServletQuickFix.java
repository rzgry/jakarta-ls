/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.jakarta.codeAction;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.core.manipulation.util.BasicElementLabels;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.ltk.core.refactoring.NullChange;

import org.jakarta.jdt.ServletConstants;
import org.jakarta.lsp4e.Activator;
import org.jakarta.codeActionProposal.ChangeCorrectionProposal;
import org.jakarta.codeActionProposal.ExtendClassProposal;

/**
 * QuickFix for fixing HttpServlet extension
 * error by providing the code actions which implements
 * IJavaCodeActionParticipant
 *
 * Adapted from https://github.com/eclipse/lsp4mp/blob/master/microprofile.jdt/org.eclipse.lsp4mp.jdt.core/src/main/java/org/eclipse/lsp4mp/jdt/internal/health/java/ImplementHealthCheckQuickFix.java
 *
 * @author Credit to Angelo ZERR
 *
 */

public class HttpServletQuickFix implements IJavaCodeActionParticipant{
	@Override
	public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
			IProgressMonitor monitor) throws CoreException {
		ASTNode node = context.getCoveredNode();
		ITypeBinding parentType = Bindings.getBindingOfParentType(node);
		if (parentType != null) {
			List<CodeAction> codeActions = new ArrayList<>();
			// Create code action
			// interface
			final String TITLE_MESSAGE = "Let ''{0}'' extend ''{1}''";
			String args[] = { BasicElementLabels.getJavaElementName(parentType.getName()),
					BasicElementLabels.getJavaElementName(ServletConstants.HTTP_SERVLET) };
			ChangeCorrectionProposal proposal = new ExtendClassProposal(MessageFormat.format(TITLE_MESSAGE, args), context.getCompilationUnit(), parentType,
					context.getASTRoot(),"jakarta.servlet.http.HttpServlet", 0);
			CodeAction codeAction = context.convertToCodeAction(proposal, diagnostic);
			codeAction.setTitle(MessageFormat.format(TITLE_MESSAGE, args));
			System.out.println("--Test test: " + codeAction);
			codeActions.add(codeAction);
			return codeActions;
		}
		return null;
	}
}