package org.jakarta.jdt;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.jakarta.codeAction.JavaCodeActionContext;
import org.jakarta.lsp4e.Activator;

import io.microshed.jakartals.commons.JakartaDiagnosticsParams;
import io.microshed.jakartals.commons.JakartaJavaCodeActionParams;

import org.jakarta.codeAction.CodeActionHandler;

/**
 * JDT manager for Java files
 *
 */
public class JDTServicesManager {

	private List<DiagnosticsCollector> diagnosticsCollectors = new ArrayList<>();

	private static final JDTServicesManager INSTANCE = new JDTServicesManager();

	private final CodeActionHandler codeActionHandler;

	public static JDTServicesManager getInstance() {
		return INSTANCE;
	}

	public JDTServicesManager() {
		diagnosticsCollectors.add(new ServletDiagnosticsCollector());
		diagnosticsCollectors.add(new FilterDiagnosticsCollector());
		diagnosticsCollectors.add(new ListenerDiagnosticsCollector());
		this.codeActionHandler = new CodeActionHandler();
	}

	/**
	 * Returns diagnostics for the given uris from the JakartaDiagnosticsParams.
	 * 
	 * @param javaParams the diagnostics parameters
	 * @return diagnostics
	 */
	public List<PublishDiagnosticsParams> getJavaDiagnostics(JakartaDiagnosticsParams javaParams) {
		List<PublishDiagnosticsParams> publishDiagnostics = new ArrayList<PublishDiagnosticsParams>();
		List<Diagnostic> diagnostics = new ArrayList<>();
		List<String> uris = javaParams.getUris();
		
		for (String uri : uris) {
			
			URI u = JDTUtils.toURI(uri);
			ICompilationUnit unit = JDTUtils.resolveCompilationUnit(u);
//			System.out.println("--compiled unit: " + unit);
			for (DiagnosticsCollector d : diagnosticsCollectors) {
				d.collectDiagnostics(unit, diagnostics);
			}
			
			PublishDiagnosticsParams publishDiagnostic = new PublishDiagnosticsParams(uri, diagnostics);
			publishDiagnostics.add(publishDiagnostic);
		}
		return publishDiagnostics;
	}
	
	/**
 	 * @author ankushsharma
 	 * @brief Gets all snippet contexts that exist in the current project classpath
 	 * @param uri - String representing file from which to derive project classpath
 	 * @param snippetContext - get all the context fields from the snippets and check if they exist in this method
 	 * @return List<String>
 	 */
 	public List<String> getExistingContextsFromClassPath(String uri, List<String> snippetContexts) {
 		// Initialize the list that will hold the classpath
 		List<String> classpath = new ArrayList<>();
 		// Convert URI into a compilation unit
 		ICompilationUnit unit = JDTUtils.resolveCompilationUnit(JDTUtils.toURI(uri));
 		// Get Java Project
 		JavaProject project = (JavaProject) unit.getJavaProject();
 		// Get Java Project
 		if (project != null) {
 			snippetContexts.forEach(ctx -> {
 				IType classPathctx = null;
 				try {
 					classPathctx = project.findType(ctx);
 					if (classPathctx != null) {
 						classpath.add(ctx);
 					} else {
 						classpath.add(null);
 					}
 				} catch (JavaModelException e) {
 					Activator.logException("Failed to retrieve projectContext from JDT...", e);
 				}
 			});
 		} else {
 			// Populate the Array with nulls up to length of snippetContext
 			snippetContexts.forEach(ctx -> {
 				classpath.add(null);
 			});
 		}
 		return classpath;
 	}
 	
	public List<CodeAction> getCodeAction(JakartaJavaCodeActionParams params,
			JDTUtils utils, IProgressMonitor monitor) throws JavaModelException {
		return codeActionHandler.codeAction(params, utils, monitor, diagnosticsCollectors.get(0));
 	}
}
