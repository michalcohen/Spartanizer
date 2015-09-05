package org.spartan.refactoring.handlers;

import static org.spartan.refactoring.handlers.ApplySpartanizationHandler.applySafeSpartanizationsTo;
import static org.spartan.refactoring.spartanizations.DialogBoxes.announce;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

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
  static final int MAX_PASSES = 20;
  @Override public Void execute(@SuppressWarnings("unused") final ExecutionEvent e) throws ExecutionException {
    final List<ICompilationUnit> compilationUnits = compilationUnits();
    final IWorkbench wb = PlatformUI.getWorkbench();
    final int initialCount = countSuggestios();
    if (initialCount == 0)
      return announce("Nothing to do");
    final StringBuilder message = new StringBuilder().append(initialCount);
    for (int i = 0, totalSuggestions = initialCount; i < MAX_PASSES; ++i) {
      final IProgressService ps = wb.getProgressService();
      try {
        ps.busyCursorWhile(new IRunnableWithProgress() {
          @Override public void run(final IProgressMonitor pm) {
            pm.beginTask("Spartanizing", compilationUnits.size());
            for (final ICompilationUnit u : compilationUnits) {
              applySafeSpartanizationsTo(u);
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
      final int countSuggestios = countSuggestios();
      totalSuggestions += countSuggestios;
      if (countSuggestios <= 0)
        return announce("Completed in " + (1 + i) + " passes. \n" + "Total changes: " + totalSuggestions + " = " + message);
      message.append(" + " + countSuggestios);
    }
    throw new ExecutionException("Too many iterations");
  }
  private static int countSuggestios() {
    int $ = 0;
    for (final Spartanization s : ApplySpartanizationHandler.safeSpartanizations)
      $ += s.countSuggestions();
    return $;
  }
}
