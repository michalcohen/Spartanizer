package org.spartan.refactoring.utils;

import org.eclipse.jdt.core.dom.*;

/**
 * A class to search in the ancestry line of a given node.
 *
 * @author Yossi Gil
 * @since 2015-08-22
 */
public abstract class AncestorSearch {
  /**
   * Factory method, returning an instance which can search by the integer
   * present on a node.
   *
   * @param type JD
   * @return a newly created instance
   * @see ASTNode#getNodeType()
   */
  public static AncestorSearch forType(final int type) {
    return new ByNodeType(type);
  }
  /**
   * Factory method, returning an instance which can search by a node class
   *
   * @param c JD
   * @return a newly created instance
   * @see ASTNode#getNodeType()
   */
  public static AncestorSearch forClass(final Class<? extends ASTNode> c) {
    return new ByNodeClass(c);
  }
  /**
   * @param n JD
   * @return the closest ancestor whose type matches the given type.
   */
  public abstract ASTNode from(final ASTNode n);

  static class ByNodeClass extends AncestorSearch {
    private final Class<? extends ASTNode> clazz;
    public ByNodeClass(final Class<? extends ASTNode> clazz) {
      this.clazz = clazz;
    }
    @Override public ASTNode from(final ASTNode n) {
      if (n != null)
        for (ASTNode $ = n.getParent(); $ != null; $ = $.getParent())
          if ($.getClass().equals(clazz) || $.getClass().isInstance(clazz))
            return $;
      return null;
    }
  }

  static class ByNodeType extends AncestorSearch {
    public ByNodeType(final int type) {
      this.type = type;
    }
    final int type;
    @Override public ASTNode from(final ASTNode n) {
      if (n != null)
        for (ASTNode $ = n.getParent(); $ != null; $ = $.getParent())
          if (type == $.getNodeType())
            return $;
      return null;
    }
  }
}