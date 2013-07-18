package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.BaseSpartanization;
import il.ac.technion.cs.ssdl.spartan.refactoring.Wizard;

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
 * @author Boris van Sosin <code><boris.van.sosin@gmail.com></code>: original
 *         version
 * @author Yossi Gil <code><yossi.gil@gmail.com></code>: major refactoring
 *         2013/07/11
 * @since 2013/07/01
 */
public abstract class BaseSpartanizationHandler extends AbstractHandler {
  private final BaseSpartanization refactoring;
  
  protected BaseSpartanization getRefactoring() {
    return refactoring;
  }
  
  protected final String getDialogTitle() {
    return refactoring.getName();
  }
  
  protected BaseSpartanizationHandler(final BaseSpartanization refactoring) {
    this.refactoring = refactoring;
  }
  
  @Override public Void execute(final ExecutionEvent e) {
    return execute(HandlerUtil.getCurrentSelection(e));
  }
  
  private Void execute(final ISelection s) {
    return !(s instanceof ITextSelection) ? null : execute((ITextSelection) s);
  }
  
  private Void execute(final ITextSelection textSelect) {
    return execute(new RefactoringWizardOpenOperation(getWizard(textSelect, getCompilationUnit())));
  }
  
  private Void execute(final RefactoringWizardOpenOperation wop) {
    try {
      wop.run(getCurrentWorkbenchWindow().getShell(), getDialogTitle());
    } catch (final InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
  
  private RefactoringWizard getWizard(final ITextSelection ts, final ICompilationUnit cu) {
    final BaseSpartanization $ = getRefactoring();
    $.setSelection(ts);
    $.setCompilationUnit(cu);
    return new Wizard($);
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
