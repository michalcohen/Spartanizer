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
    final BaseRefactoring refactoring = getRefactoring();
    refactoring.setSelection(selection);
    refactoring.setCompilationUnit(compilationUnit);
    return new BaseRefactoringWizard(refactoring);
  }
  
  @Override public void run(final IAction action) {
    if (window == null)
      return;
    final RefactoringWizardOpenOperation wop = new RefactoringWizardOpenOperation(getWizard());
    try {
      wop.run(window.getShell(), getDialogTitle());
    } catch (final InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  @Override public void selectionChanged(final IAction action, final ISelection selection_) {
    compilationUnit = getCompilationUnit();
    selection = selection_ instanceof ITextSelection ? (ITextSelection) selection_ : null;
    action.setEnabled(compilationUnit != null);
  }
  
  private ICompilationUnit getCompilationUnit() {
    final IEditorPart editorPart = window.getActivePage().getActiveEditor();
    if (editorPart == null)
      return null;
    final IResource r = (IResource) editorPart.getEditorInput().getAdapter(IResource.class);
    return r == null ? null : JavaCore.createCompilationUnitFrom((IFile) r);
    // cu be null
  }
  
  @Override public void dispose() {
    return;
  }
  
  @Override public void init(final IWorkbenchWindow window_) {
    window = window_;
  }
}
