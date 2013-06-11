package il.ac.technion.cs.ssdl.spartan.refactoring;

import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMarkerResolution;

public class BasicSpartanization {
  public BasicSpartanization(final BaseRefactoring ref, final String name, final String message) {
    this.refactoring = ref;
    this.name = name;
    this.message = message;
  }
  
  private final BaseRefactoring refactoring;
  private final String name;
  private final String message;
  
  @Override public String toString() {
    return name;
  }
  
  public String getMessage() {
    return message;
  }
  
  public IMarkerResolution getFix() {
    return new SpartanizationResolution();
  }
  
  public IMarkerResolution getFixWithPreview() {
    return new SpartanizationResolutionWithPreview();
  }
  
  public BaseRefactoring getRefactoring() {
    return refactoring;
  }
  
  public class SpartanizationResolution implements IMarkerResolution {
    public String getLabel() {
      return BasicSpartanization.this.toString() + ": Do it!";
    }
    
    public void run(final IMarker arg0) {
      try {
        getRefactoring().runAsMarkerFix(new org.eclipse.core.runtime.NullProgressMonitor(), arg0);
      } catch (CoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  public class SpartanizationResolutionWithPreview implements IMarkerResolution {
    public String getLabel() {
      return BasicSpartanization.this.toString() + ": Show me a preview first";
    }
    
    public void run(final IMarker arg0) {
      getRefactoring().setMarker(arg0);
      final BaseRefactoringWizard wiz = new BaseRefactoringWizard(getRefactoring());
      try {
        new RefactoringWizardOpenOperation(wiz).run(Display.getCurrent().getActiveShell(), "Spartan Refactoring: "
            + BasicSpartanization.this.toString());
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  public Collection<SpartanizationRange> checkForSpartanization(final CompilationUnit cu) {
    return refactoring.checkForSpartanization(cu);
  }
  
  public static class SpartanizationRange {
    public final int from, to;
    
    public SpartanizationRange(int from, int to) {
      this.from = from;
      this.to = to;
    }
    
    public SpartanizationRange(final ASTNode n) {
      this(n.getStartPosition(), n.getStartPosition() + n.getLength());
    }
    
    public SpartanizationRange(final ASTNode first, final ASTNode last) {
      this(first.getStartPosition(), last.getStartPosition() + last.getLength());
    }
  }
}
