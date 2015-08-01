package org.spartan.refactoring.handlers;

import org.spartan.refactoring.spartanizations.ComparisonWithBoolean;

/**
 * a handler for {@link Spartanizations}
 *
 * @author Ofir Elmakias <code><boris.van.sosin [at] gmail.com></code>
 * @since 2015/08/01
 */
public class CleanupHandler extends BaseHandler {
  /** Instantiates this class */
  public CleanupHandler() {
    // super(new AsRefactoring(Wrings.COMPARISON_WITH_BOOLEAN, "", ""));
    super(new ComparisonWithBoolean());
    executeWithoutDialog();
  }
}
