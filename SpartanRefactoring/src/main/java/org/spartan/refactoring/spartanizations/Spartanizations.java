package org.spartan.refactoring.spartanizations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.spartan.refactoring.preferences.PreferencesFile;
import static org.spartan.refactoring.spartanizations.Wrings.*;

/**
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code> (original /
 *         30.05.2014) (v3)
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> (original /
 *         30.05.2014) (v3)
 * @since 2013/07/01
 */
@SuppressWarnings("javadoc") //
public enum Spartanizations {
<<<<<<< HEAD
  ComparisonWithBoolean(new ComparisonWithBoolean()), //
  ComparisonWithSpecific(new ComparisonWithSpecific()), //
  ForwardDeclaration(new ForwardDeclaration()), //
  InlineSingleUse(new InlineSingleUse()), //
  RenameReturnVariableToDollar(new RenameReturnVariableToDollar()), //
  ShortestBranchFirst(new ShortestBranchFirst()), //
  Ternarize(new Ternarize()), //
  ShortestOperand(new ShortestOperand()), //
  // SimplifyTernary(new AsSpartanization(Wrings.TERNARY_BOOLEAN_LITERAL.inner,
  // "Ternary", "Simplify complex ternary boolean expression")), //
  // Tautologies(new AsSpartanization(Wrings.ANDOR_TRUE.inner, "&&/|| true",
  // "Eliminate true")), //
  // PushDownNot(new AsSpartanization(Wrings.PUSHDOWN_NOT.inner, "Pushdown not",
  // "Simplify not expression")), //
=======
  // ComparisonWithBoolean(new ComparisonWithBoolean()), //
  // ComparisonWithSpecific(new ComparisonWithSpecific()), //
  // ForwardDeclaration(new ForwardDeclaration()), //
  // InlineSingleUse(new InlineSingleUse()), //
  // RenameReturnVariableToDollar(new RenameReturnVariableToDollar()), //
  // ShortestBranchFirst(new ShortestBranchFirst()), //
  Ternarize(new Ternarize()), //
  // AndTrue(new AsSpartanization(AND_TRUE.inner, "and true", "remove trues from
  // expression")), //
  // OrFalse(new AsSpartanization(OR_FALSE.inner, "or false", "remove falses
  // from expression")), //
  // ShortestOperand(new ShortestOperand()), //
  SimplifyTernary(new AsSpartanization(TERNARY_BOOLEAN_LITERAL.inner, "Ternary", "Simplify complex ternary boolean expression")), //
  // Tautologies(new AsSpartanization(Wrings.ANDOR_TRUE.inner, "&&/|| true",
  // "Eliminate true")), //
<<<<<<< HEAD
  // PushDownNot(new AsSpartanization(PUSHDOWN_NOT.inner, "Pushdown not",
  // "Simplify not expression")), //
=======
  PushDownNot(new AsSpartanization(PUSHDOWN_NOT.inner, "Pushdown not", "Simplify not expression")), //
>>>>>>> 9bc365fe0e6ed8f876dc17a2c1f03433880a7574
>>>>>>> 13211baf453325d9cf6da4c0a681cdb1502fe138
  // TODO break that simply returns
  // TODO Change Javadoc to one line /**... */ style when possible
  // TODO Check for mentions of arguments in JavaDoc
  // TODO Clever chaining in 2 to 3 selected classes
  // TODO more clever forward/inline. do not propose if components of expression
  // are used in between
  // TODO Add safe operations as source menu item for safe operations.
  // TODO Use one letter name for local variables and parameters
  ;
  private final Spartanization value;
  private Spartanizations(final Spartanization value) {
    this.value = value;
  }
  /**
   * @return Spartanization class rule instance
   */
  public Spartanization value() {
    return value;
  }
  /**
   * @param c Spartanization rule
   * @return Spartanization class rule instance
   */
  @SuppressWarnings("unchecked") //
  public static <T extends Spartanization> T findInstance(final Class<? extends T> c) {
    for (final Spartanizations a : Spartanizations.values()) {
      final Spartanization $ = a.value();
      if ($.getClass().equals(c))
        return (T) $;
    }
    return null;
  }
  /**
   * @return Iteration over all Spartanization class instances
   */
  public static Iterable<Spartanization> allAvailableSpartanizations() {
    return new Iterable<Spartanization>() {
      @Override public Iterator<Spartanization> iterator() {
        return new Iterator<Spartanization>() {
          int next;
          @Override public boolean hasNext() {
            return next < values().length;
          }
          @Override public Spartanization next() {
            return values()[next++].value();
          }
          @Override public final void remove() {
            throw new IllegalArgumentException();
          }
        };
      }
    };
  }
  private static String ignoreRuleStr = "false";
  private static final Map<String, Spartanization> all = new HashMap<>();
  private static void put(final Spartanization s) {
    all.put(s.toString(), s);
  }
  private static boolean ignored(final String sparta) {
    return sparta.indexOf(ignoreRuleStr) >= 0;
  }
  /**
   * Resets the enumeration with the current values from the preferences file.
   * Letting the rules notification decisions be updated without restarting
   * eclipse.
   */
  public static void reset() {
    all.clear();
    final int offset = PreferencesFile.getSpartanTitle().length;
    final String[] str = PreferencesFile.parsePrefFile();
    final boolean useAll = str == null;
    int i = 0;
    for (final Spartanization rule : allAvailableSpartanizations()) {
      if (useAll || str != null && str.length >= i + offset && !ignored(str[i + offset]))
        put(rule);
      i++;
    }
    put(new SimplifyLogicalNegation());
  }
  /**
   * @param name the name of the spartanization
   * @return an instance of the spartanization
   */
  public static Spartanization get(final String name) {
    assert name != null;
    return all.get(name);
  }
  /**
   * @return all the registered spartanization refactoring objects
   */
  public static Iterable<Spartanization> all() {
    return all.values();
  }
  /**
   * @return all the registered spartanization refactoring objects names
   */
  public static List<String> allRulesNames() {
    final List<String> $ = new ArrayList<>();
    for (final Spartanization rule : allAvailableSpartanizations())
      if (rule != null)
        $.add(rule.getName());
    return $;
  }
}
