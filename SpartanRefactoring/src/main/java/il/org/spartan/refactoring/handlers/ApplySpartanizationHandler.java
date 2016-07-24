package il.org.spartan.refactoring.handlers;

import static il.org.spartan.idiomatic.*;
import il.org.spartan.refactoring.spartanizations.*;
import il.org.spartan.refactoring.wring.*;
import il.org.spartan.utils.*;

import org.eclipse.core.commands.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jface.text.*;
import org.eclipse.ui.*;

/** A handler for {@link Spartanizations} This handler executes all safe
 * spartanizations on all Java files in the current project, while exposing
 * static methods to spartanize only specific compulation units.
 *
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code>
 * @since 2015/08/01 */
public class ApplySpartanizationHandler extends BaseHandler {
  @SuppressWarnings("javadoc") public static void execute(final ICompilationUnit cu) {
    execute(cu, new Range(0, 0));
  }
  @SuppressWarnings("javadoc") public static void execute(final ICompilationUnit cu, final ITextSelection t) {
    for (final Spartanization s : inner)
      try {
        s.setCompilationUnit(cu);
        s.setSelection(t.getLength() > 0 && !t.isEmpty() ? t : null);
        for (int i = 0; i < max_spartanization_repetitions; ++i)
          s.setSelection(take(t).unless(t.isEmpty()));
        for (int i = 0; i < MAX_SPARTANIZATION_REPETITIONS; ++i)
          if (!s.performRule(cu, new NullProgressMonitor()))
            break;
      } catch (final CoreException x) {
        x.printStackTrace();
      }
  }
  @SuppressWarnings("javadoc") public static void execute(final ICompilationUnit cu, final Range r) {
    execute(cu, r == null || r.size() <= 0 ? new TextSelection(0, 0) : new TextSelection(r.from, r.size()));
  }
  private static ITextSelection getSelectedText() {
    return (ITextSelection) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorSite().getSelectionProvider().getSelection();
  }

  /** Sets the maximum number of repetitions made when applying a spartanization */
  public static final int max_spartanization_repetitions = 16;
  private static int MAX_SPARTANIZATION_REPETITIONS = max_spartanization_repetitions;
  static final Spartanization[] inner = { new Trimmer() };

  /** Instantiates this class */
  public ApplySpartanizationHandler() {
    super(null);
  }
  @Override public Void execute(@SuppressWarnings("unused") final ExecutionEvent __) {
    execute(currentCompilationUnit(), getSelectedText());
    return null;
  }
}
