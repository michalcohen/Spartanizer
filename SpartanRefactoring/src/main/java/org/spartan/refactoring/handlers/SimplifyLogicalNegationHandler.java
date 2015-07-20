package org.spartan.refactoring.handlers;

import org.spartan.refactoring.spartanizations.SimplifyLogicalNegation;

/**
 * a BaseSpartanizationHandler configured to return the BaseRefactoring and
 * label relevant for Convert to Ternary
 *
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code>
 * @author Yossi Gil <code><yossi.gil [at] gmail.com></code> (major refactoring
 *         2013/07/11)
 * @since 2013/07/01
 */
public class SimplifyLogicalNegationHandler extends BaseHandler {
  /** Instantiates this class */
  public SimplifyLogicalNegationHandler() {
    super(new SimplifyLogicalNegation());
  }
}
