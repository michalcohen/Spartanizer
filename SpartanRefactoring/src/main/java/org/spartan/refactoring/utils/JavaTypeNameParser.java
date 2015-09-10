package org.spartan.refactoring.utils;

import java.util.Iterator;

/**
 * A utility parser that resolves a variable's short name, and determines
 * whether a pre-existing name is a generic variation of the type's name. <br>
 * A variable's short name is a single-character name, determined by the first
 * character in the last word of the type's name.<br>
 * For example:
 * <code><pre>  public void execute(HTTPSecureConnection httpSecureConnection) {...}</pre></code>
 * would become<br>
 * <code><pre>  public void execute(HTTPSecureConnection c) {...}</pre></code>
 *
 * @author Daniel Mittelman <code><mittelmania [at] gmail.com></code>
 * @since 2015-08-25
 */
public class JavaTypeNameParser {
  /** The type name managed by this instance */
  public final String typeName;
  private final boolean isCollection;
  /**
   * Instantiates this class
   *
   * @param typeName the Java type name to parse
   * @param isCollection denotes whether the type is a collection or a varargs
   *          parameter
   */
  public JavaTypeNameParser(final String typeName, final boolean isCollection) {
    this.typeName = typeName;
    this.isCollection = isCollection;
  }
  /**
   * Returns whether a variable name is a generic variable of the type name
   *
   * @param variableName the name of the variable
   * @return true if the variable name is a generic variation of the type name,
   *         false otherwise
   */
  public boolean isGenericVariation(final String variableName) {
    return typeName.equalsIgnoreCase(variableName) || lowerCaseContains(typeName, variableName) || lowerCaseContains(typeName, toSingular(variableName));
  }
  /**
   * Returns the calculated short name for the type
   *
   * @return the type's short name
   */
  public String shortName() {
    final String sn = String.valueOf(Character.toLowerCase(lastName().charAt(0)));
    return sn + (isCollection ? "s" : "");
  }
  @SuppressWarnings("static-method") private String toSingular(final String s) {
    // NOTE: This encompasses 99.9% of the nouns in the English language
    if (s == null)
      return null;
    if (s.endsWith("ies"))
      return s.substring(0, s.length() - 3) + "y";
    if (s.endsWith("es"))
      return s.substring(0, s.length() - 2);
    if (s.endsWith("s"))
      return s.substring(0, s.length() - 1);
    return s;
  }
  /**
   * Shorthand for n.equals(this.shortName())
   *
   * @param n JD
   * @return true if the provided name equals the type's short name
   */
  public boolean isShort(final String s) {
    return s.equals(shortName());
  }
  String lastName() {
    return typeName.substring(lastNameIndex());
  }
  int lastNameIndex() {
    for (int $ = typeName.length() - 1; $ > 0; --$) {
      if (isLower($) && isUpper($ - 1))
        return $ - 1;
      if (isUpper($) && isLower($ - 1))
        return $;
    }
    return 0;
  }
  private boolean isLower(final int i) {
    return Character.isLowerCase(typeName.charAt(i));
  }
  private boolean isUpper(final int i) {
    return Character.isUpperCase(typeName.charAt(i));
  }
  @SuppressWarnings("static-method") private boolean lowerCaseContains(final String string, final String substring) {
    return string.toLowerCase().contains(substring.toLowerCase());
  }
  Iterable<String> suggestions() {
    return new Iterable<String>() {
      @Override public Iterator<String> iterator() {
        return new Iterator<String>() {
          @Override public boolean hasNext() {
            return true;
          }
          @Override public String next() {
            return shortName();
          }
          @Override public void remove() {
            // Redundant
          }
        };
      }
    };
  }
}