package org.spartan.refactoring.commandhandlers;

import org.spartan.refactoring.ComparisonWithSpecific;

/**
 * a handler for {@link ComparisonWithSpecific}
 *
 * @author Yossi Gil <code><yossi.gil [at] gmail.com></code>
 * @since 2013/07/01
 */
public class ComparisonWithSpecificHandler extends BaseHandler {
  /** Instantiates this class */
  public ComparisonWithSpecificHandler() {
    super(new ComparisonWithSpecific());
  }
}
