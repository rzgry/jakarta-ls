package org.jakarta.lsp4e;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.Collections;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.jakarta.codeAction.JavaCodeActionContext;
import org.jakarta.jdt.JDTServicesManager;
import org.jakarta.jdt.JDTUtils;

import io.microshed.jakartals.api.JakartaLanguageClientAPI;
import io.microshed.jakartals.commons.JakartaDiagnosticsParams;
import io.microshed.jakartals.commons.JakartaJavaCodeActionParams;

public class JakartaLanguageClient extends LanguageClientImpl implements JakartaLanguageClientAPI {

	public JakartaLanguageClient() {
		// do nothing
	}

	private IProgressMonitor getProgressMonitor(CancelChecker cancelChecker) {
		IProgressMonitor monitor = new NullProgressMonitor() {
			public boolean isCanceled() {
				cancelChecker.checkCanceled();
				return false;
			};
		};
		return monitor;
	}

	@Override
	public CompletableFuture<Hover> getJavaHover(HoverParams params) {
		return CompletableFuture.completedFuture(null);
		// return dummy test hover object
//		return CompletableFutures.computeAsync((cancelChecker) -> {
//			IProgressMonitor monitor = getProgressMonitor(cancelChecker);
//			Hover testHover = new Hover();
//			List<Either<String, MarkedString>> contents = new ArrayList<>();
//			contents.add(Either.forLeft("this is test hover"));
//			testHover.setContents(contents);
//			return testHover;
//		});
	}

	@Override
	public CompletableFuture<List<PublishDiagnosticsParams>> getJavaDiagnostics(JakartaDiagnosticsParams javaParams) {
		Activator.log(new Status(IStatus.INFO, "diagnostic request received", "diagnostic request receieved"));
		// creating a test diagnostic
		// problem! the Async leads to diagnostic msg not sync with the changes on the client side
		return CompletableFutures.computeAsync((cancelChecker) -> {
			IProgressMonitor monitor = getProgressMonitor(cancelChecker);

			List<PublishDiagnosticsParams> publishDiagnostics = new ArrayList<PublishDiagnosticsParams>();
			publishDiagnostics = JDTServicesManager.getInstance().getJavaDiagnostics(javaParams);
			return publishDiagnostics;
		});
	}
	
	/**
 	 * @author ankushsharma
 	 * @brief creates a filter to let the language server know which contexts exist in the Java Project
 	 * @param uri - String representing file from which to derive project classpath
 	 * @param snippetContext - get all the context fields from the snippets and check if they exist in this method
 	 * @return List<String>
 	 */
 	@Override
 	public CompletableFuture<List<String>> getContextBasedFilter(String uri, List<String> snippetContexts) {
 		return CompletableFutures.computeAsync((cancelChecker) -> {
 			return JDTServicesManager.getInstance().getExistingContextsFromClassPath(uri, snippetContexts);
 		});
 	}

	public CompletableFuture<List<CodeAction>> getCodeAction(CodeActionParams params){
		JDTUtils utils = new JDTUtils();
		JakartaJavaCodeActionParams JakartaParams = new JakartaJavaCodeActionParams(params.getTextDocument(), params.getRange(), params.getContext());
		return CompletableFutures.computeAsync((cancelChecker) -> {
			IProgressMonitor monitor = getProgressMonitor(cancelChecker);
			try {
				return JDTServicesManager.getInstance().getCodeAction(JakartaParams, utils, monitor);
			} catch (JavaModelException e) {
				return null;
			}
		});
	}
	
}
