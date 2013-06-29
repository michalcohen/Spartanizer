package il.ac.technion.cs.ssdl.spartan.refactoring;

import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMarkerResolution;

/**
 * @author Boris van Sosin
 *
 */
public class BasicSpartanization {
  /**
 * @param ref
 * 		an instance of a Spartanization Refactoring
 * @param name
 * 		the name of the Spartanization Refactoring
 * @param message
 * 		the message to display in the quickfix
 */
public BasicSpartanization(final BaseRefactoring ref, final String name, final String message) {
    refactoring = ref;
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
  * @author Boris van Sosin
  * a quickfix which automatically performs the spartanization
  */
  public class SpartanizationResolution implements IMarkerResolution {
    @Override public String getLabel() {
      return BasicSpartanization.this.toString() + ": Do it!";
    }
    
    @Override public void run(final IMarker arg0) {
      try {
        getRefactoring().runAsMarkerFix(new org.eclipse.core.runtime.NullProgressMonitor(), arg0);
      } catch (final CoreException e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
  * @author Boris van Sosin
  * a quickfix which opens a refactoring wizard with the spartanization
  */
  public class SpartanizationResolutionWithPreview implements IMarkerResolution {
    @Override public String getLabel() {
      return BasicSpartanization.this.toString() + ": Show me a preview first";
    }
    
    @Override public void run(final IMarker arg0) {
      getRefactoring().setMarker(arg0);
      try {
        new RefactoringWizardOpenOperation(new BaseRefactoringWizard(getRefactoring())).run(Display.getCurrent().getActiveShell(),
            "Spartan Refactoring: " + BasicSpartanization.this.toString());
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
  * @param cu
  * 	the Compilation Unit (outermost ASTNode in the Java Grammar) to check for spartanization
  * @return a collection of ranges, each representing a spartanization suggestion
  */
  public Collection<SpartanizationRange> checkForSpartanization(final CompilationUnit cu) {
    return refactoring.checkForSpartanization(cu);
  }
  
  /**
  * @author Boris van Sosin
  * a range which contains a spartanization siggestion.
  * used for creating text markers
  */
  public static class SpartanizationRange {
    /**
     * the beginning of the range
     */
    public final int from;
    /**
     * the end of the range
     */
    public final int to;
    
    /**
     * creates a SpartanizationRange from beginning and end locations
     * @param from
     * 		the beginning of the range
     * @param to
     * 		the end of the range
     */
    public SpartanizationRange(final int from, final int to) {
      this.from = from;
      this.to = to;
    }
    
    /**
     * creates a SpartanizationRange from a single ASTNode
     * @param n
     * 		the ASTNode
     */
    public SpartanizationRange(final ASTNode n) {
      this(n.getStartPosition(), n.getStartPosition() + n.getLength());
    }
    
    /**
     * creates a SpartanizationRange from beginning and end ASTNodes
     * @param first
     * 		the beginning ASTNode (inclusive)
     * @param last
     * 		the end ASTNode (inclusive)
     */
    public SpartanizationRange(final ASTNode first, final ASTNode last) {
      this(first.getStartPosition(), last.getStartPosition() + last.getLength());
    }
  }
}
