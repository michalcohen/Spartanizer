package il.org.spartan.spartanizer.engine;

import org.eclipse.jdt.core.dom.*;

import il.org.spartan.spartanizer.ast.navigate.*;
import il.org.spartan.spartanizer.tipping.*;
import il.org.spartan.utils.*;

public class ShortTip extends Range {
  public static int lineFromNode(final ASTNode n) {
    return ((CompilationUnit) searchAncestors.forClass(CompilationUnit.class).from(n)).getLineNumber(n.getStartPosition());
  }

  /** A factory function that converts a sequence of ASTNodes into a
   * {@link Range}
   * @param n arbitrary
   * @param ns */
  protected static Range range(final ASTNode n, final ASTNode... ns) {
    return range(singleNodeRange(n), ns);
  }

  static ShortTip shortenTip(Tip t) {
    return new ShortTip(t, t.description, t.lineNumber, t.tipperClass);
  }
  static Range range(final Range r, final ASTNode... ns) {
    Range $ = r;
    for (final ASTNode ¢ : ns)
      $ = $.merge(singleNodeRange(¢));
    return $;
  }

  static Range singleNodeRange(final ASTNode n) {
    final int from = n.getStartPosition();
    return new Range(from, from + n.getLength());
  }

  /** A textual description of the action to be performed **/
  public final String description;

  /** The line number of the first character to be rewritten **/
  public final int lineNumber;

  /** The tipper class that supplied that tip */
  public final Class<? extends Tipper<?>> tipperClass;

  /** Instantiates this class
   * @param description a textual description of the changes described by this
   *        instance
   * @param n the node on which change is to be carried out
   * @param ns additional nodes, defining the scope of this action. */
  public ShortTip(final String description, final Class<? extends Tipper<?>> tipperClass, final ASTNode n, final ASTNode... ns) {
    this(range(n, ns), description, lineFromNode(n), tipperClass);
  }

  ShortTip(final Range other, final String description, int lineNumber, final Class<? extends Tipper<?>> tipperClass) {
    super(other);
    this.description = description;
    this.lineNumber = lineNumber;
    this.tipperClass = tipperClass;
  }
}