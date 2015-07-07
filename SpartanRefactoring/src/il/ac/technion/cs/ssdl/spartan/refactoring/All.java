package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab.PreferencesStrings.NullAndBoolAtEnd;
import static il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab.PreferencesStrings.NullAndBoolAtNone;
import static il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab.PreferencesStrings.NullAndBoolAtStart;
import static il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab.PreferencesStrings.doNotRepositionLiterals;
import static il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab.PreferencesStrings.doNotRepositionRightLiterals;
import static il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab.PreferencesStrings.repositionAllLiterals;
import static il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab.PreferencesStrings.repositionRightException;
import static il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab.PreferencesStrings.repositionRightLiterals;
import static il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab.PreferencesStrings.showEverySwap;
import static il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab.PreferencesStrings.showOneSwap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import il.ac.technion.cs.ssdl.spartan.refactoring.ShortestOperand.MessagingOptions;
import il.ac.technion.cs.ssdl.spartan.refactoring.ShortestOperand.RepositionBoolAndNull;
import il.ac.technion.cs.ssdl.spartan.refactoring.ShortestOperand.RepositionLiterals;
import il.ac.technion.cs.ssdl.spartan.refactoring.ShortestOperand.RepositionRightLiteral;
import il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab.PreferencesFile;

/**
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code> (original /
 *         30.05.2014) (v3)
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> (original /
 *         30.05.2014) (v3)
 * @since 2013/07/01
 */
public enum All {
  @SuppressWarnings("javadoc") ComparisonWithBoolean(new ComparisonWithBoolean()), //
  @SuppressWarnings("javadoc") ComparisonWithSpecific(new ComparisonWithSpecific()), //
  @SuppressWarnings("javadoc") ForwardDeclaration(new ForwardDeclaration()), //
  @SuppressWarnings("javadoc") InlineSingleUse(new InlineSingleUse()), //
  @SuppressWarnings("javadoc") RenameReturnVariableToDollar(new RenameReturnVariableToDollar()), //
  @SuppressWarnings("javadoc") ShortestBranchFirst(new ShortestBranchFirst()), //
  @SuppressWarnings("javadoc") Ternarize(new Ternarize()), //
  @SuppressWarnings("javadoc") ShortestOperand(new ShortestOperand()), //
  @SuppressWarnings("javadoc") ComparisonWithLiteral(new ShortestOperand()), //
  // TODO Change Javadoc to one line /**... */ style when possible
  // TODO Check for mentions of arguments in JavaDoc
  // TODO Clever chaining in 2 to 3 selected classes
  // TODO Do not commute in +; it might be strings
  // TODO Literal always on left on multiplication
  // TODO Literal always on right on all comparisons
  // TODO 'this' always on right on all comparisons
  // TODO more clever forward/inline. do not propose if components of expression
  // are used in between
  // TODO List of safe and sane operations: comparison with boolean, literal
  // ordering, simplify negation, ternarize, chain, shortest branch
  // TODO Add safe operations as source menu item for safe operations.
  // TODO Add safe operations as cleanup operations
  // TODO Use one letter name for local variables and parameters
  ;
  private final Spartanization value;

  private All(final Spartanization value) {
    this.value = value;
  }

  /**
   * @return Spartanization class rule instance
   */
  public Spartanization value() {
    return value;
  }

  /**
   * @param c
   *          Spartanization rule
   * @return Spartanization class rule instance
   */
  @SuppressWarnings("unchecked") //
  public static <T extends Spartanization> T findInstance(final Class<? extends T> c) {
    for (final All a : All.values()) {
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

  private static void assignRulesOptions(final String[] lines) {
    final ShortestOperand shortestOperandInstance = (ShortestOperand) ShortestOperand.value;
    if (lines == null || shortestOperandInstance == null)
      return;
    for (final String line : lines) {
      // There must be a way to make it looks good, it's looks similar to
      // the case with o.equals() and the in() function but it's not the
      // same case...
      if (line == null)
        continue;
      if (line.contains(repositionRightLiterals))
        shortestOperandInstance.setRightLiteralRule(RepositionRightLiteral.All);
      if (line.contains(repositionRightException))
        shortestOperandInstance.setRightLiteralRule(RepositionRightLiteral.AllButBooleanAndNull);
      if (line.contains(doNotRepositionRightLiterals))
        shortestOperandInstance.setRightLiteralRule(RepositionRightLiteral.None);
      if (line.contains(repositionAllLiterals))
        shortestOperandInstance.setBothLiteralsRule(RepositionLiterals.All);
      if (line.contains(doNotRepositionLiterals))
        shortestOperandInstance.setBothLiteralsRule(RepositionLiterals.None);
      if (line.contains(NullAndBoolAtStart))
        shortestOperandInstance.setBoolNullLiteralsRule(RepositionBoolAndNull.MoveLeft);
      if (line.contains(NullAndBoolAtEnd))
        shortestOperandInstance.setBoolNullLiteralsRule(RepositionBoolAndNull.MoveRight);
      if (line.contains(NullAndBoolAtNone))
        shortestOperandInstance.setBoolNullLiteralsRule(RepositionBoolAndNull.None);
      if (line.contains(showOneSwap))
        shortestOperandInstance.setMessagingOption(MessagingOptions.Union);
      if (line.contains(showEverySwap))
        shortestOperandInstance.setMessagingOption(MessagingOptions.ShowAll);
    }
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
    assignRulesOptions(str);
    put(new SimplifyLogicalNegation());
  }

  /**
   * @param name
   *          the name of the spartanization
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
