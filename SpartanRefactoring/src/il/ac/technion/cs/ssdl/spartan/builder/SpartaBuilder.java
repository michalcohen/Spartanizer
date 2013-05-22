package il.ac.technion.cs.ssdl.spartan.builder;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import il.ac.technion.cs.ssdl.spartan.refactoring.BasicSpartanization;
import il.ac.technion.cs.ssdl.spartan.refactoring.BasicSpartanization.SpartanizationRange;
import il.ac.technion.cs.ssdl.spartan.refactoring.SpartanizationFactory;

public class SpartaBuilder extends IncrementalProjectBuilder {

	class SampleDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
		 * .core.resources.IResourceDelta)
		 */
		public boolean visit(final IResourceDelta delta) throws CoreException {
			final IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				checkJava(resource, FULL_BUILD);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				checkJava(resource, INCREMENTAL_BUILD);
				break;
			}
			// return true to continue visiting children.
			return true;
		}
	}

	class SampleResourceVisitor implements IResourceVisitor {
		public boolean visit(final IResource resource) {
			checkJava(resource, FULL_BUILD);
			// return true to continue visiting children.
			return true;
		}
	}

	public static final String BUILDER_ID = "il.ac.technion.cs.ssdl.spartan.builder.spartaBuilder";

	private static final String MARKER_TYPE = "il.ac.technion.cs.ssdl.spartan.spartanizationSuggestion";

	public static final String SPARTANIZATION_TYPE_KEY = "il.ac.technion.cs.ssdl.spartan.spartanizationType";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	@SuppressWarnings("rawtypes") //Auto-generated code. Didn't write it.
	protected IProject[] build(final int kind, final Map args,
			final IProgressMonitor monitor) throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			final IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	void checkJava(final IResource resource, final int buildLevel) {
		if (resource instanceof IFile && resource.getName().endsWith(".java")) {
			final IFile file = (IFile) resource;
			deleteMarkers(file);
			try {
				final ICompilationUnit cu = JavaCore
						.createCompilationUnitFrom(file);
				final ASTParser parser = ASTParser.newParser(AST.JLS4);
				parser.setResolveBindings(false);
				parser.setKind(ASTParser.K_COMPILATION_UNIT);
				parser.setSource(cu);
				final CompilationUnit concreteCu = (CompilationUnit) parser
						.createAST(null);
				for (final BasicSpartanization currSpartanization : SpartanizationFactory
						.getAllSpartanizations()) {
					for (final SpartanizationRange rng : currSpartanization
							.checkForSpartanization(concreteCu)) {
						if (rng != null) {
							final IMarker blahMarker = file
									.createMarker(MARKER_TYPE);
							blahMarker.setAttribute(IMarker.CHAR_START,
									rng.from);
							blahMarker.setAttribute(IMarker.CHAR_END,
									rng.to);
							blahMarker.setAttribute(IMarker.SEVERITY,
									IMarker.SEVERITY_WARNING);
							blahMarker.setAttribute(SPARTANIZATION_TYPE_KEY,
									currSpartanization.toString());
							blahMarker
									.setAttribute(IMarker.MESSAGE,
											"Spartanization suggestion: " + currSpartanization.getMessage());
						}
					}
				}
			} catch (final Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private void deleteMarkers(final IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ONE);
		} catch (final CoreException ce) {
		}
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		try {
			getProject().accept(new SampleResourceVisitor());
		} catch (final CoreException e) {
		}
	}

	protected void incrementalBuild(final IResourceDelta delta,
			final IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new SampleDeltaVisitor());
	}
}
