package org.spartan.refactoring.handlers;

import static org.spartan.refactoring.handlers.ApplySpartanizationHandler.applySafeSpartanizationsTo;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;
import org.spartan.refactoring.spartanizations.Spartanizations;

/**
 * A handler for {@link Spartanizations} This handler executes all safe
 * Spartanizations on all java files in the current project.
 *
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code>
 * @since 2015/08/01
 */
public class CleanupHandler extends BaseHandler {
  /** Instantiates this class */
  public CleanupHandler() {
    super(null);
  }
  @Override public Void execute(final ExecutionEvent e) throws ExecutionException {
    final List<ICompilationUnit> compilationUnits = compilationUnits();
    final IWorkbench wb = PlatformUI.getWorkbench();
    final IProgressService ps = wb.getProgressService();
    try {
      ps.busyCursorWhile(new IRunnableWithProgress() {
        @Override public void run(final IProgressMonitor pm) {
          pm.beginTask("Spartanizing", compilationUnits.size());
          for (final ICompilationUnit cu : compilationUnits) {
            applySafeSpartanizationsTo(cu);
            pm.worked(1);
          }
          pm.done();
        }
      });
    } catch (final InvocationTargetException x) {
      x.printStackTrace();
    } catch (final InterruptedException x) {
      x.printStackTrace();
    }

    final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(e);
    MessageDialog.openInformation(
        window.getShell(),
        "Spartanization",
        "Your project has been Spartanized successfully!");
    return null;
  }
}
