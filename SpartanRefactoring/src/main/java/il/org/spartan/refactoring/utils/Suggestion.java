package il.org.spartan.refactoring.utils;

import static org.eclipse.core.runtime.IProgressMonitor.*;
import il.org.spartan.LaxySymbolicSpreadsheet.Cell;
import il.org.spartan.LaxySymbolicSpreadsheet.Computed;
import il.org.spartan.LaxySymbolicSpreadsheet.Valued;
import il.org.spartan.refactoring.suggestions.*;
import il.org.spartan.refactoring.wring.*;
import il.org.spartan.utils.*;

import java.util.*;
import java.util.function.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.ltk.core.refactoring.*;
import org.eclipse.text.edits.*;
import org.eclipse.ui.*;

/**
 * A function object representing a sequence of operations on an
 * {@link ASTRewrite} object.
 *
 * @author Yossi Gil
 * @since 2015-08-28
 */
@SuppressWarnings("javadoc") public class Suggestion extends Context {
  /* A factory function that converts a sequence of ASTNodes into a {@link final
   * Range}
   *
   * @param n arbitrary
   *
   * @param ns JD */
  static Range range(final ASTNode n, final ASTNode... ns) {
    return range(singleNodeRange(n), ns);
  }
  static Range range(final Range r, final ASTNode... ns) {
    Range $ = r;
    for (final ASTNode n : ns)
      $ = $.merge(singleNodeRange(n));
    return $;
  }
  static Range singleNodeRange(final ASTNode n) {
    final int from = n.getStartPosition();
    return new Range(from, from + n.getLength());
  }
  /**
   * Instantiates this class
   *
   * @param description a textual description of the changes described by this
   *          instance
   * @param n the node on which change is to be carried out
   * @param ns additional nodes, defining the scope of this action.
   */
  public Suggestion(final String description, final ASTNode n, final ASTNode... ns) {
    this(range(n, ns));
    lineNumber = ((CompilationUnit) AncestorSearch.forClass(CompilationUnit.class).from(n)).getLineNumber(from);
  }
  public final Change createChange() throws OperationCanceledException {
    return new CompositeChange("" + this, changes.toArray(new Change[changes.size()]));
  }
  /** @return current description */
  public String description() {
    return description;
  }
  /**
   * @return a quick fix for this instance
   */
  public IMarkerResolution getFix() {
    return getFix("" + this);
  }
  /**
   * @param s Spartanization's name
   * @return a quickfix which automatically performs the spartanization
   */
  public IMarkerResolution getFix(final String s) {
    /**
     * a quickfix which automatically performs the spartanization
     *
     * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code>
     * @since 2013/07/01
     */
    return new IMarkerResolution() {
      @Override public String getLabel() {
        return "Spartanize!";
      }
      @Override public void run(final IMarker m) {
        try {
          fix(new NullProgressMonitor(), m);
        } catch (final CoreException e) {
          e.printStackTrace();
        }
      }
    };
  }
  /**
   * @return a quick fix with a preview for this instance.
   */
  public IMarkerResolution getFixWithPreview() {
    return getFixWithPreview("" + this);
  }
  /**
   * Convert the rewrite into changes on an {@link ASTRewrite}
   *
   * @param r where to place the changes
   * @param g to be associated with these changes
   */
  public void go(ASTRewrite r, TextEditGroup g) {
  }
  /** @return current lineNumber */
  public int lineNumber() {
    return lineNumber;
  }
  public Suggestion of(final Range ¢) {
    set(¢);
    return self();
  }
  /** sets this instance description */
  public void of(String s) {
    description = s;
  }
  public Suggestion of(Wring<?> ¢) {
    wring.set(¢);
    return self();
  }
  /**
   * Performs the current Spartanization on the provided compilation unit
   *
   * @param cu the compilation to spartanize
   * @param progressMonitor progress monitor for long operations (could be
   *          {@link NullProgressMonitor} for light operations)
   * @return whether any changes have been made to the compilation unit
   * @throws CoreException exception from the <code>pm</code>
   */
  public boolean performRule() throws CoreException {
    progressMonitor().beginTask("Creating change for a single compilation unit...", 2);
    final TextFileChange textChange = textChange(cu);
    Source.set(cu.getPath(), textChange.getCurrentDocument(null).get());
    textChange.setTextType("java");
    final SubProgressMonitor spm = new SubProgressMonitor(progressMonitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
    textChange.setEdit(starting.vrom((CompilationUnit) Make.COMPILIATION_UNIT.parser(cu).createAST(spm)).with(spm).rewriteAST());
    boolean $ = false;
    if (textChange.getEdit().getLength() != 0) {
      textChange.perform(progressMonitor());
      $ = true;
    }
    progressMonitor().done();
    return $;
  }
  @Override public Suggestion self() {
    return this;
  }
  public void set(final Range r) {
    range.set(r);
  }
  /** @return a textual description of this instance */
  @Override public String toString() {
    return description != null ? description : getClass().getSimpleName();
  }
  public Wring<?> wring() {
    return wring.get();
  }
  ASTNode node() {
    return node.get();
  }
  /**
   * Performs the current Spartanization on the provided compilation unit
   *
   * @param cu the compilation to spartanize
   * @return whether any changes have been made to the compilation unit
   * @throws CoreException exception from the <code>pm</code>
   */
  boolean performRule(final ICompilationUnit cu) {
    progressMonitor().beginTask("Creating change for a single compilation unit...", UNKNOWN);
    final TextFileChange textChange = textChange(cu);
    Source.set(cu.getPath(), textChange.getCurrentDocument(null).get());
    textChange.setTextType("java");
    final SubProgressMonitor spm = new SubProgressMonitor(progressMonitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
    textChange.setEdit(inContext().set((CompilationUnit) Make.COMPILIATION_UNIT.parser(cu).createAST(spm)).set(spm).rewriteAST());
    boolean $ = false;
    if (textChange.getEdit().getLength() != 0) {
      textChange.perform(progressMonitor());
      $ = true;
    }
    progressMonitor().done();
    return $;
  }
  void set(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  private String description;
  private int lineNumber;
  private final Edible<ASTNode> node = new Ingredient<ASTNode>();
  private final Edible<Range> range = new Recipe<Range>(new Supplier<Range>() {
    @Override public Range get() {
      return range(node());
    }
  });
  private final Edible<Wring<?>> wring = new Ingredient<Wring<?>>();
  List<Change> changes;
}
