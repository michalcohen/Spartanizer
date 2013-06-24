package il.ac.technion.cs.ssdl.spartan.builder;

import il.ac.technion.cs.ssdl.spartan.refactoring.BasicSpartanization;
import il.ac.technion.cs.ssdl.spartan.refactoring.BasicSpartanization.SpartanizationRange;
import il.ac.technion.cs.ssdl.spartan.refactoring.SpartanizationFactory;

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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class SpartaBuilder extends IncrementalProjectBuilder {
  static class SampleDeltaVisitor implements IResourceDeltaVisitor {
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
     * .core.resources.IResourceDelta)
     */
    @Override public boolean visit(final IResourceDelta delta) throws CoreException {
      final IResource resource = delta.getResource();
      switch (delta.getKind()) {
        case IResourceDelta.ADDED:
          // handle added resource
          checkJava(resource);
          break;
        case IResourceDelta.REMOVED:
          // handle removed resource
          break;
        case IResourceDelta.CHANGED:
          // handle changed resource
          checkJava(resource);
          break;
        default:
          break;
      }
      // return true to continue visiting children.
      return true;
    }
  }
  
  static class SampleResourceVisitor implements IResourceVisitor {
    @Override public boolean visit(final IResource resource) {
      checkJava(resource);
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
  @Override @SuppressWarnings("rawtypes")// Auto-generated code. Didn't write
  // it.
  protected IProject[] build(final int kind, final Map args, final IProgressMonitor monitor) throws CoreException {
    if (kind == FULL_BUILD)
      fullBuild(monitor);
    else {
      final IResourceDelta delta = getDelta(getProject());
      if (delta == null)
        fullBuild(monitor);
      else
        incrementalBuild(delta, monitor);
    }
    return null;
  }
  
  static void checkJava(final IResource resource) {
    if (resource instanceof IFile && resource.getName().endsWith(".java")) {
      final IFile file = (IFile) resource;
      deleteMarkers(file);
      try {
        final ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setResolveBindings(false);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(JavaCore.createCompilationUnitFrom(file));
        final CompilationUnit concreteCu = (CompilationUnit) parser.createAST(null);
        for (final BasicSpartanization currSpartanization : SpartanizationFactory.getAllSpartanizations())
          for (final SpartanizationRange rng : currSpartanization.checkForSpartanization(concreteCu))
            if (rng != null) {
              final IMarker spartanizationMarker = file.createMarker(MARKER_TYPE);
              spartanizationMarker.setAttribute(IMarker.CHAR_START, rng.from);
              spartanizationMarker.setAttribute(IMarker.CHAR_END, rng.to);
              spartanizationMarker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
              spartanizationMarker.setAttribute(SPARTANIZATION_TYPE_KEY, currSpartanization.toString());
              spartanizationMarker.setAttribute(IMarker.MESSAGE, "Spartanization suggestion: " + currSpartanization.getMessage());
            }
      } catch (final Exception e1) {
        e1.printStackTrace();
      }
    }
  }
  
  public static void deleteMarkers(final IFile file) {
    try {
      file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ONE);
    } catch (final CoreException ce) {
      // we assume that other builder handle cause compilation failure on
      // CoreException
    }
  }
  
  protected void fullBuild(final IProgressMonitor monitor) {
    try {
      if (monitor != null)
        monitor.beginTask("Running Spartanization Builder", IProgressMonitor.UNKNOWN);
      getProject().accept(new SampleResourceVisitor());
    } catch (final CoreException e) {
      // we assume that other builder handle cause compilation failure on
      // CoreException
    }
    if (monitor != null)
      monitor.done();
  }
  
  protected static void incrementalBuild(final IResourceDelta delta, final IProgressMonitor monitor) throws CoreException {
    // the visitor does the work.
    if (monitor != null)
      monitor.beginTask("Running Spartanization Builder", IProgressMonitor.UNKNOWN);
    delta.accept(new SampleDeltaVisitor());
    if (monitor != null)
      monitor.done();
  }
}
