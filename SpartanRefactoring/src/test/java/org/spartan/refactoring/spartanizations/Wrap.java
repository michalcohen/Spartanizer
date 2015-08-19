package org.spartan.refactoring.spartanizations;

import static org.spartan.utils.Utils.removePrefix;
import static org.spartan.utils.Utils.removeSuffix;

/**
 * An empty <code><b>enum</b></code> for fluent programming. The name should say
 * it all: The name, followed by a dot, followed by a method name, should read
 * like a sentence phrase.
 *
 * @author Yossi Gil
 * @since 2015-07-16
 */
public enum Wrap {
  /** Algorithm for wrapping/unwrapping an expression */
  Expression {
    @Override public String on(final String s) {
      return PRE_EXPRESSION + s + POST_EXPRESSION;
    }
    @Override public String off(final String s) {
      return removeSuffix(removePrefix(s, PRE_EXPRESSION), POST_EXPRESSION);
    }
  },
  /** Algorithm for wrapping/unwrapping a statement */
  Statement {
    @Override public String on(final String s) {
      return PRE_STATEMENT + s + POST_STATEMENT;
    }
    @Override public String off(final String s) {
      return removeSuffix(removePrefix(s, PRE_STATEMENT), POST_STATEMENT);
    }
  };
  private static final String PRE_STATEMENT = //
  "package p;public class SpongeBob {\n" + //
      "public boolean squarePants(){\n" + //
      "";
  private static final String POST_STATEMENT = //
  "" + //
      "} // END OF METHO\n" + //
      "} // END OF PACKAGE\n" + //
      "";
  private static final String PRE_EXPRESSION = PRE_STATEMENT + "   return ";
  private static final String POST_EXPRESSION = ";\n" + POST_STATEMENT;
  /**
   * Place a wrap around a phrase
   *
   * @param s some program phrase
   * @return the wrapped phrase
   */
  public abstract String on(final String s);
  /**
   * Remove a wrap from around a phrase
   *
   * @param s a wrapped program phrase
   * @return the unwrapped phrase
   */
  public abstract String off(final String s);
}
