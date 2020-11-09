
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.manipulation.CoreASTProvider;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.jakarta.jdt.DiagnosticsCollector;

import io.microshed.jakartals.commons.JakartaJavaCodeActionParams;

import org.jakarta.jdt.JDTUtils;
import org.jakarta.jdt.DiagnosticsCollector;
import org.jakarta.codeAction.HttpServletQuickFix;

/**
 * Code action handler.
 * Partially reused from https://github.com/eclipse/lsp4mp/blob/b88710cc54170844717f655b9bff8bb4c4649a8d/microprofile.jdt/org.eclipse.lsp4mp.jdt.core/src/main/java/org/eclipse/lsp4mp/jdt/internal/core/java/codeaction/CodeActionHandler.java#L46
 * @author credit to Angelo ZERR
 *
 */
public class CodeActionHandler{
	public List<CodeAction> codeAction(JakartaJavaCodeActionParams params, JDTUtils utils,
			IProgressMonitor monitor, DiagnosticsCollector d){
		String uri = params.getUri();
 		ICompilationUnit unit = utils.resolveCompilationUnit(uri);
 		if (unit == null) {
			return Collections.emptyList();
		}
 		
 		
		ISourceRange nameRange = d.getRange();
		if (nameRange == null) {
			return Collections.emptyList();
		}
		int rangeLen = nameRange.getLength();
		int rangeOffset = nameRange.getOffset();
		
		int start = rangeOffset;
		int end = rangeOffset+ rangeLen;
		JavaCodeActionContext context = new JavaCodeActionContext(unit, start, end - start, utils, params);
		context.setASTRoot(getASTRoot(unit, monitor));
		
		List<CodeAction> codeActions = new ArrayList<>();
		// Loop for each diagnostics to perform codeAction
//		List<Diagnostic> diagnostics = new ArrayList<>();
//		d.collectDiagnostics(unit, diagnostics);

		
		HttpServletQuickFix HttpServletQuickFix = new HttpServletQuickFix();
//		for (Diagnostic diagnostic : diagnostics) {
		for (Diagnostic diagnostic : params.getContext().getDiagnostics()) {
			try {
				codeActions.addAll(HttpServletQuickFix.getCodeActions(context, diagnostic, monitor));
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
 		return codeActions;
	}
	
	private static CompilationUnit getASTRoot(ICompilationUnit unit, IProgressMonitor monitor) {
		return CoreASTProvider.getInstance().getAST(unit, CoreASTProvider.WAIT_YES, monitor);
	}
}