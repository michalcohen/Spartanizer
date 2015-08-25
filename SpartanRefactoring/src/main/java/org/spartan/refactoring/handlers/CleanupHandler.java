package org.spartan.refactoring.handlers;

import static org.spartan.refactoring.handlers.ApplySpartanizationHandler.applySafeSpartanizationsTo;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.spartan.refactoring.spartanizations.Spartanization;
import org.spartan.refactoring.spartanizations.Spartanizations;

/**
 * A handler for {@link Spartanizations}. This handler executes all safe
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

  static final int MaxSpartanizationTries = 20;

  @Override public Void execute(@SuppressWarnings("unused") final ExecutionEvent e) throws ExecutionException {
    final List<ICompilationUnit> compilationUnits = compilationUnits();
    final IWorkbench wb = PlatformUI.getWorkbench();
    int loopCounter = 0;
    boolean haveSuggestions = true;
    while (haveSuggestions){
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
      haveSuggestions = false;
      for (final Spartanization s : ApplySpartanizationHandler.safeSpartanizations)
        if (s.haveSuggestions()) haveSuggestions = true;
      loopCounter++;
      if (loopCounter > MaxSpartanizationTries) throw new ExecutionException(null);
    }
    final ImageIcon i = new ImageIcon(this.getClass().getResource("/res/Spartan64.gif"));
    JOptionPane.showMessageDialog(null, "Your project has been Spartanized successfully!", "Spartanization", JOptionPane.INFORMATION_MESSAGE, i);

    return null;
  }
}
