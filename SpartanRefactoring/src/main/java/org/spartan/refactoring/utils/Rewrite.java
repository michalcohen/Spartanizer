package org.spartan.refactoring.utils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.utils.Range;

/**
 * A function object representing a sequence of operations on an
 * {@link ASTRewrite} object.
 *
 * @author Yossi Gil
 * @since 2015-08-28
 */
public abstract class Rewrite extends Range {
  /** A textual description of the action to be performed. */
  public final String description;
  /**
   * Instantiates this class
   *
   * @param description a textual description of the changes described by this
   *          instance
   * @param n the node on which change is to be carried out
   * @param ns additional nodes, defining the scope of this action.
   */
  public Rewrite(final String description, final ASTNode n, final ASTNode... ns) {
    this(description, range(n, ns));
  }
  Rewrite(final String description, final Range other) {
    super(other);
    this.description = description;
  }
  /**
   * A factory function that converts a single ASTNode into a {@link Range}
   *
   * @param n arbitrary
   * @param ns
   */
  static Range range(final ASTNode n, final ASTNode... ns) {
    final Range $ = singleNodeRange(n);
    return range($, ns);
  }
  static Range range(final Range r, final ASTNode... ns) {
    for (final ASTNode n : ns)
      r.merge(singleNodeRange(n));
    return r;
  }
  static Range singleNodeRange(final ASTNode n) {
    final int from = n.getStartPosition();
    return new Range(from, from + n.getLength());
  }
  /**
   * Convert the rewrite into changes on an {@link ASTRewrite}
   *
   * @param r where to place the changes
   * @param editGroup to be associated with these changes
   */
  public abstract void go(ASTRewrite r, TextEditGroup editGroup);
}
