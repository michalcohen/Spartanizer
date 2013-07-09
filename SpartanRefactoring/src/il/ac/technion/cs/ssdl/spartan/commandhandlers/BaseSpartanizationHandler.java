package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.BaseRefactoring;
import il.ac.technion.cs.ssdl.spartan.refactoring.BaseRefactoringWizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Boris van Sosin
 * 
 */
public abstract class BaseSpartanizationHandler extends AbstractHandler {
  @Override public Object execute(ExecutionEvent event) {
    final ISelection selection = HandlerUtil.getCurrentSelection(event);
    if (selection instanceof ITextSelection) {
      final ITextSelection textSelect = (ITextSelection) selection;
      final RefactoringWizardOpenOperation wop = new RefactoringWizardOpenOperation(getWizard(textSelect, getCompilationUnit()));
      try {
        wop.run(getCurrentWorkbenchWindow().getShell(), getDialogTitle());
      } catch (final InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return null;
  }
  
  private RefactoringWizard getWizard(final ITextSelection ts, final ICompilationUnit cu) {
    final BaseRefactoring r = getRefactoring();
    r.setSelection(ts);
    r.setCompilationUnit(cu);
    return new BaseRefactoringWizard(r);
  }
  
  private static ICompilationUnit getCompilationUnit() {
    final IEditorPart editorPart = getCurrentWorkbenchWindow().getActivePage().getActiveEditor();
    if (editorPart == null)
      return null;
    final IResource r = (IResource) editorPart.getEditorInput().getAdapter(IResource.class);
    return r == null ? null : JavaCore.createCompilationUnitFrom((IFile) r);
  }
  
  private static IWorkbenchWindow getCurrentWorkbenchWindow() {
    return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
  }
  
  protected abstract String getDialogTitle();
  
  protected abstract BaseRefactoring getRefactoring();
}
