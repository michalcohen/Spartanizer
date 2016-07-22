package il.org.spartan.refactoring.wring;

import il.org.spartan.refactoring.utils.*;

import java.util.*;

import org.eclipse.jdt.core.dom.*;

class ExclusionManager {
  void exclude(final ASTNode n) {
    inner.add(n);
  }
  boolean isExcluded(final ASTNode n) {
    for (final ASTNode ancestor : new Ancestors(n))
      if (inner.contains(ancestor))
        return true;
    return false;
  }
  void unExclude(final ASTNode n) {
    inner.remove(n);
  }

  final Set<ASTNode> inner = new HashSet<>();
}