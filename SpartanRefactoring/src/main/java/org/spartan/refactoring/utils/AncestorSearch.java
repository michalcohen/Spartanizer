package org.spartan.refactoring.utils;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * A class to search in the ancestry line of a given node.
 *
 * @author Yossi Gil
 * @since 2015-08-22
 */
public class AncestorSearch {
  /**
   * Instantiates this class, to search for a given type.
   *
   * @param type JD
   * @see ASTNode#getNodeType()
   */
  public AncestorSearch(final int type) {
    this.type = type;
  }
  final int type;
  /**
   * @param n JD
   * @return the closest ancestor whose type matches the given type.
   */
  public ASTNode of(final ASTNode n) {
    if (n != null)
      for (ASTNode $ = n.getParent(); $ != null; $ = $.getParent())
        if (type == $.getNodeType())
          return $;
    return null;
  }
}