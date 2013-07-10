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
    @Override public boolean visit(final IResourceDelta delta) {
      final IResource r = delta.getResource();
      switch (delta.getKind()) {
        case IResourceDelta.ADDED:
        case IResourceDelta.CHANGED:
          // handle added and changed resource
          checkJava(r);
          break;
        case IResourceDelta.REMOVED:
          // handle removed resource
          break;
        default:
          break;
      }
      // return true to continue visiting children.
      return true;
    }
  }
  
  static class Visitor implements IResourceVisitor {
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
   * the Key in the marker's properties map under which the type of the
   * spartanization is stored
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
  protected IProject[] build(final int kind, @SuppressWarnings("unused") final Map args, final IProgressMonitor m)
      throws CoreException {
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
    if (r instanceof IFile && r.getName().endsWith(".java"))
      checkJava((IFile) r);
  }
  
  private static void checkJava(final IFile f) {
    deleteMarkers(f);
    try {
      final ASTParser p = ASTParser.newParser(AST.JLS4);
      p.setResolveBindings(false);
      p.setKind(ASTParser.K_COMPILATION_UNIT);
      p.setSource(JavaCore.createCompilationUnitFrom(f));
      for (final BasicSpartanization currSpartanization : SpartanizationFactory.all())
        for (final SpartanizationRange range : currSpartanization.checkForSpartanization((CompilationUnit) p.createAST(null)))
          if (range != null) {
            final IMarker m = f.createMarker(MARKER_TYPE);
            m.setAttribute(IMarker.CHAR_START, range.from);
            m.setAttribute(IMarker.CHAR_END, range.to);
            m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
            m.setAttribute(SPARTANIZATION_TYPE_KEY, currSpartanization.toString());
            m.setAttribute(IMarker.MESSAGE, "Spartanization suggestion: " + currSpartanization.getMessage());
          }
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * deletes all spartanization suggestion markers
   * 
   * @param f
   *          the file from which to delete the markers
   */
  public static void deleteMarkers(final IFile f) {
    try {
      f.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ONE);
    } catch (final CoreException ce) {
      // we assume that other builder handle cause compilation failure on
      // CoreException
    }
  }
  
  protected void fullBuild(final IProgressMonitor m) {
    if (m != null)
      m.beginTask("Running Spartanization Builder", IProgressMonitor.UNKNOWN);
    try {
      getProject().accept(new Visitor());
    } catch (final CoreException e) {
      // we assume that other builder handle cause compilation failure on
      // CoreException
    }
    if (m != null)
      m.done();
  }
  
  protected static void incrementalBuild(final IResourceDelta delta, final IProgressMonitor m) throws CoreException {
    // the visitor does the work.
    if (m != null)
      m.beginTask("Running Spartanization Builder", IProgressMonitor.UNKNOWN);
    delta.accept(new SampleDeltaVisitor());
    if (m != null)
      m.done();
  }
}
