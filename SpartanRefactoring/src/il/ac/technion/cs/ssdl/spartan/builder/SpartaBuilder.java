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

/**
 * @author Eclipse (auto-generated)
 * @author Boris van Sosin (mostly auto-generated)
 *
 */
public class SpartaBuilder extends IncrementalProjectBuilder {
  static class SampleDeltaVisitor implements IResourceDeltaVisitor {
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
     * .core.resources.IResourceDelta)
     */
    @Override public boolean visit(final IResourceDelta delta) throws CoreException {
      final IResource r = delta.getResource();
      switch (delta.getKind()) {
        case IResourceDelta.ADDED:
          // handle added resource
          checkJava(r);
          break;
        case IResourceDelta.REMOVED:
          // handle removed resource
          break;
        case IResourceDelta.CHANGED:
          // handle changed resource
          checkJava(r);
          break;
        default:
          break;
      }
      // return true to continue visiting children.
      return true;
    }
  }
  
  static class SampleResourceVisitor implements IResourceVisitor {
    @Override public boolean visit(final IResource r) {
      checkJava(r);
      // return true to continue visiting children.
      return true;
    }
  }
  
  /**
   * the ID which which the builder is registered 
  */
  public static final String BUILDER_ID = "il.ac.technion.cs.ssdl.spartan.builder.spartaBuilder";
  private static final String MARKER_TYPE = "il.ac.technion.cs.ssdl.spartan.spartanizationSuggestion";
  
  /**
   * the Key in the marker's properties map under which the type of the spartanization is stored
  */
  public static final String SPARTANIZATION_TYPE_KEY = "il.ac.technion.cs.ssdl.spartan.spartanizationType";
  
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
   * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override @SuppressWarnings("rawtypes")// Auto-generated code. Didn't write
  // it.
  protected IProject[] build(final int kind, final Map args, final IProgressMonitor m) throws CoreException {
    if (kind == FULL_BUILD)
      fullBuild(m);
    else {
      final IResourceDelta delta = getDelta(getProject());
      if (delta == null)
        fullBuild(m);
      else
        incrementalBuild(delta, m);
    }
    return null;
  }
  
  static void checkJava(final IResource r) {
    if (r instanceof IFile && r.getName().endsWith(".java")) {
      final IFile f = (IFile) r;
      deleteMarkers(f);
      try {
        final ASTParser p = ASTParser.newParser(AST.JLS4);
        p.setResolveBindings(false);
        p.setKind(ASTParser.K_COMPILATION_UNIT);
        p.setSource(JavaCore.createCompilationUnitFrom(f));
        final CompilationUnit concreteCu = (CompilationUnit) p.createAST(null);
        for (final BasicSpartanization currSpartanization : SpartanizationFactory.getAllSpartanizations())
          for (final SpartanizationRange range : currSpartanization.checkForSpartanization(concreteCu))
            if (range != null) {
              final IMarker spartanizationMarker = f.createMarker(MARKER_TYPE);
              spartanizationMarker.setAttribute(IMarker.CHAR_START, range.from);
              spartanizationMarker.setAttribute(IMarker.CHAR_END, range.to);
              spartanizationMarker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
              spartanizationMarker.setAttribute(SPARTANIZATION_TYPE_KEY, currSpartanization.toString());
              spartanizationMarker.setAttribute(IMarker.MESSAGE, "Spartanization suggestion: " + currSpartanization.getMessage());
            }
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * deletes all spartanization suggestion markers
   * @param file
   * 	the file from which to delete the markers
  */
  public static void deleteMarkers(final IFile file) {
    try {
      file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ONE);
    } catch (final CoreException ce) {
      // we assume that other builder handle cause compilation failure on
      // CoreException
    }
  }
  
  protected void fullBuild(final IProgressMonitor m) {
    if (m != null)
      m.beginTask("Running Spartanization Builder", IProgressMonitor.UNKNOWN);
    try {
      getProject().accept(new SampleResourceVisitor());
    } catch (final CoreException e) {
      // we assume that other builder handle cause compilation failure on
      // CoreException
    }
    done(m);
  }
  
  protected static void incrementalBuild(final IResourceDelta delta, final IProgressMonitor m) throws CoreException {
    // the visitor does the work.
    if (m != null)
      m.beginTask("Running Spartanization Builder", IProgressMonitor.UNKNOWN);
    delta.accept(new SampleDeltaVisitor());
    done(m);
  }
  
  /**
   * indicates to the progress monitor that the spartanization check is done
   * @param m
   * 	the progress monitor
  */
  public static void done(final IProgressMonitor m) {
    if (m != null)
      m.done();
  }
}
