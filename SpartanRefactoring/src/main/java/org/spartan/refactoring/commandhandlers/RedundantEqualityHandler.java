package org.spartan.refactoring.commandhandlers;

import org.spartan.refactoring.ComparisonWithBoolean;

/**
 * a handler for {@link ComparisonWithBoolean}
 * 
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code>
 * @author Yossi Gil <code><yossi.gil [at] gmail.com></code> (major refactoring
 *         2013/07/11)
 * @since 2013/07/01
 */
public class RedundantEqualityHandler extends BaseHandler {
  /** Instantiates this class */
  public RedundantEqualityHandler() {
    super(new ComparisonWithBoolean());
  }
}
