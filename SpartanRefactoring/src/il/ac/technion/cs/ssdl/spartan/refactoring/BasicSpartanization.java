package il.ac.technion.cs.ssdl.spartan.refactoring;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMarkerResolution;

/**
 * @author Boris van Sosin <boris.van.sosin@gmail.com>
 * 
 * @since 2013/07/01
 */
public class BasicSpartanization {
  /**
   * @param refactoring
   *          an instance of a Spartanization Refactoring
   * @param name
   *          the name of the Spartanization Refactoring
   * @param message
   *          the message to display in the quickfix
   */
  public BasicSpartanization(final BaseRefactoring refactoring, final String name, final String message) {
    this.refactoring = refactoring;
    this.name = name;
    this.message = message;
  }
  
  private final BaseRefactoring refactoring;
  private final String name;
  private final String message;
  
  @Override public String toString() {
    return name;
  }
  
  /**
   * @return the message to display in the quickfix
   */
  public String getMessage() {
    return message;
  }
  
  /**
   * @return a quickfix which automatically performs the spartanization
   */
  public IMarkerResolution getFix() {
    return new SpartanizationResolution();
  }
  
  /**
   * @return a quickfix which opens a refactoring wizard with the spartanization
   */
  public IMarkerResolution getFixWithPreview() {
    return new SpartanizationResolutionWithPreview();
  }
  
  /**
   * @return the underlying refactoring object
   */
  public BaseRefactoring getRefactoring() {
    return refactoring;
  }
  
  /**
   * a quickfix which automatically performs the spartanization
   * 
   * @author Boris van Sosin <boris.van.sosin@gmail.com>
   */
  public class SpartanizationResolution implements IMarkerResolution {
    @Override public String getLabel() {
      return BasicSpartanization.this.toString() + ": Do it!";
    }
    
    @Override public void run(final IMarker m) {
      try {
        getRefactoring().runAsMarkerFix(new NullProgressMonitor(), m);
      } catch (final CoreException e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * a quickfix which opens a refactoring wizard with the spartanization
   * 
   * @author Boris van Sosin
   */
  public class SpartanizationResolutionWithPreview implements IMarkerResolution {
    @Override public String getLabel() {
      return BasicSpartanization.this.toString() + ": Show me a preview first";
    }
    
    @Override public void run(final IMarker m) {
      getRefactoring().setMarker(m);
      try {
        new RefactoringWizardOpenOperation(new SpartanRefactoringWizard(getRefactoring())).run(Display.getCurrent()
            .getActiveShell(), "Spartan Refactoring: " + BasicSpartanization.this.toString());
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * @param cu
   *          the Compilation Unit (outermost ASTNode in the Java Grammar) to
   *          check for spartanization
   * @return a collection of ranges, each representing a spartanization
   *         suggestion
   */
  public Iterable<Range> findOpportunities(final CompilationUnit cu) {
    return refactoring.findOpportunities(cu);
  }
  
  /**
   * a range which contains a spartanization suggestion. used for creating text
   * markers
   * 
   * @author Boris van Sosin <boris.van.sosin@gmail.com>
   */
  public static class Range {
    /** the beginning of the range (inclusive) */
    public final int from;
    /** the end of the range (exclusive) */
    public final int to;
    
    /**
     * Instantiates from beginning and end locations
     * 
     * @param from
     *          the beginning of the range (inclusive)
     * @param to
     *          the end of the range (exclusive)
     */
    private Range(final int from, final int to) {
      this.from = from;
      this.to = to;
    }
    
    /**
     * Instantiates from a single ASTNode
     * 
     * @param n
     *          the ASTNode
     */
    public Range(final ASTNode n) {
      this(n.getStartPosition(), n.getStartPosition() + n.getLength());
    }
    
    /**
     * Instantiates from beginning and end ASTNodes
     * 
     * @param from
     *          the beginning ASTNode (inclusive)
     * @param to
     *          the end ASTNode (inclusive)
     */
    public Range(final ASTNode from, final ASTNode to) {
      this(from.getStartPosition(), to.getStartPosition() + to.getLength());
    }
  }
}
