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
  /**
   * Instantiates this class
   *
   * @param typeName the Java type name to parse
   */
  public JavaTypeNameParser(final String typeName) {
    this.typeName = typeName;
  }
  /**
   * Returns whether a variable name is a generic variable of the type name
   *
   * @param variableName the name of the variable
   * @return true if the variable name is a generic variation of the type name,
   *         false otherwise
   */
  public boolean isGenericVariation(final String variableName) {
    return typeName.equalsIgnoreCase(variableName) || typeName.toLowerCase().contains(variableName.toLowerCase());
  }
  /**
   * Returns the calculated short name for the type
   *
   * @return the type's short name
   */
  public String shortName() {
    return String.valueOf(Character.toLowerCase(lastName().charAt(0)));
  }
  /**
   * Shorthand for n.equals(this.shortName())
   * 
   * @param n JD
   * @return true if the provided name equals the type's short name
   */
  public boolean isShort(final String n) {
    return n.equals(shortName());
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
  // TODO Daniel: Ask Yossi what these two methods are for
  String[] components() {
    return new String[] { "" + hashCode(), toString(), getClass().getSimpleName(), getClass().getCanonicalName() };
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
            // TODO Auto-generated method stub
          }
        };
      }
    };
  }
}