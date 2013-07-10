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
  protected abstract String getDialogTitle();
  
  protected abstract BaseRefactoring getRefactoring();
  
  @Override public Object execute(ExecutionEvent event) {
    final ISelection s = HandlerUtil.getCurrentSelection(event);
    if (s instanceof ITextSelection)
      execute((ITextSelection) s);
    return null;
  }
  
  private void execute(final ITextSelection textSelect) {
    execute(new RefactoringWizardOpenOperation(getWizard(textSelect, getCompilationUnit())));
  }
  
  private void execute(final RefactoringWizardOpenOperation wop) {
    try {
      wop.run(getCurrentWorkbenchWindow().getShell(), getDialogTitle());
    } catch (final InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  private RefactoringWizard getWizard(final ITextSelection ts, final ICompilationUnit cu) {
    final BaseRefactoring r = getRefactoring();
    r.setSelection(ts);
    r.setCompilationUnit(cu);
    return new BaseRefactoringWizard(r);
  }
  
  private static ICompilationUnit getCompilationUnit() {
    return getCompilationUnit(getCurrentWorkbenchWindow().getActivePage().getActiveEditor());
  }
  
  private static ICompilationUnit getCompilationUnit(final IEditorPart ep) {
    return ep == null ? null : getCompilationUnit((IResource) ep.getEditorInput().getAdapter(IResource.class));
  }
  
  private static ICompilationUnit getCompilationUnit(final IResource r) {
    return r == null ? null : JavaCore.createCompilationUnitFrom((IFile) r);
  }
  
  private static IWorkbenchWindow getCurrentWorkbenchWindow() {
    return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
  }
}
