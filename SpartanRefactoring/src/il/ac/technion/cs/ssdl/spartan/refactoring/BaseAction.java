package il.ac.technion.cs.ssdl.spartan.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public abstract class BaseAction implements IWorkbenchWindowActionDelegate {
  IWorkbenchWindow window = null;
  ITextSelection selection = null;
  ICompilationUnit compilationUnit = null;
  
  /**
   * 
   * @return The title of the dialog that will be opened.
   */
  protected abstract String getDialogTitle();
  
  /**
   * 
   * @return A new instance of a refactoring.
   */
  protected abstract BaseRefactoring getRefactoring();
  
  /**
   * Creates an instance of a refactoring wizard.
   * 
   * @return
   */
  protected RefactoringWizard getWizard() {
    BaseRefactoring refactoring = getRefactoring();
    refactoring.selection = selection;
    refactoring.compilationUnit = compilationUnit;
    return new BaseRefactoringWizard(refactoring);
  }
  
  @Override
  public void run(IAction action) {
    if (window == null)
      return;
    RefactoringWizardOpenOperation wop = new RefactoringWizardOpenOperation(getWizard());
    try {
      wop.run(window.getShell(), getDialogTitle());
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  @Override
  public void selectionChanged(final IAction action,final ISelection selection_) {
    this.compilationUnit = getCompilationUnit();
    this.selection = selection_ instanceof ITextSelection ? (ITextSelection) selection_ : null;
    action.setEnabled(compilationUnit != null);
  }
  
  private ICompilationUnit getCompilationUnit() {
    IEditorPart editorPart = window.getActivePage().getActiveEditor();
    if (editorPart == null)
      return null;
    IEditorInput editorInput = editorPart.getEditorInput();
    IResource resource = (IResource) editorInput.getAdapter(IResource.class);
    if (resource == null)
      return null;
    ICompilationUnit cu = JavaCore.createCompilationUnitFrom((IFile) resource);
    return cu; // cu may be null
  }
  
  @Override
  public void dispose() {
    return;
  }
  
  @Override
  public void init(final IWorkbenchWindow window_) {
    this.window = window_;
  }
}
