package org.spartan.refactoring;

/**
 * @author Yossi Gil
 * @since 2015/07/04
 */
abstract class SpartanizationOfInfixExpression extends Spartanization {
  public SpartanizationOfInfixExpression(final String name, final String message) {
    super(name, message);
  }

  public SpartanizationOfInfixExpression(final String name) {
    super(name);
  }
}
