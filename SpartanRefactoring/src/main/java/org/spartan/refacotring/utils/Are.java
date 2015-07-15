package org.spartan.refacotring.utils;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;

public enum Are {
  ;
  public static boolean notString(final List<Expression> es) {
    for (final Expression e : es)
      if (!Is.notString(e))
        return false;
    return true;
  }
}
