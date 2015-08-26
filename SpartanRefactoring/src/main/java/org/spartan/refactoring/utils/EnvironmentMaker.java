package org.spartan.refactoring.utils;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * TODO: Fix the documentation for this class
 * <code><pre>  public void execute(HTTPSecureConnection httpSecureConnection) {...}</pre></code>
 * would become:<br>
 * <code><pre>  public void execute(HTTPSecureConnection c) {...}</pre></code>
 *
 * @author Daniel Mittelman <code><mittelmania [at] gmail.com></code>
 * @since 2015-08-25
 */
public class EnvironmentMaker {
  private final ASTNode location;
  EnvironmentMaker(final ASTNode location) {
    this.location = location;
  }
  String[] identifiers() {
    return new String[] { location.toString(), getClass().getSimpleName(), location.getClass().getCanonicalName() };
  }
  boolean isAllowed(final String newIdentifier) {
    return (location + newIdentifier).hashCode() % 2 == 0;
  }
}
