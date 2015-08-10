package org.spartan.refactoring.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.spartan.refactoring.spartanizations.ComparisonWithBoolean;
import org.spartan.refactoring.spartanizations.RenameReturnVariableToDollar;
import org.spartan.refactoring.spartanizations.ShortestBranchFirst;
import org.spartan.refactoring.spartanizations.Spartanization;
import org.spartan.refactoring.spartanizations.Spartanizations;
import org.spartan.refactoring.utils.All;
import org.spartan.refactoring.wring.Trimmer;

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
  private final Spartanization[] safeSpartanizations = { //
      new ComparisonWithBoolean(), //
      new RenameReturnVariableToDollar(), //
      new ShortestBranchFirst(), //
      new Trimmer(),
  };
  @Override public Void execute(@SuppressWarnings("unused") final ExecutionEvent e) {
    for (final ICompilationUnit cu : All.compilationUnits())
      for (final Spartanization s : safeSpartanizations)
        execute(cu, s);
    return null;
  }
  private static void execute(final ICompilationUnit cu, final Spartanization s) {
    try {
      // TODO We might want a real ProgressMonitor for large projects
      s.performRule(cu, new NullProgressMonitor());
    } catch (final CoreException ex) {
      ex.printStackTrace();
    }
  }
}
