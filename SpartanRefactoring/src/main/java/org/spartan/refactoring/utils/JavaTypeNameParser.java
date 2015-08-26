package org.spartan.refactoring.utils;

import java.util.Iterator;

/**
 * TODO: Fix the documentation for this class
 * <code><pre>  public void execute(HTTPSecureConnection httpSecureConnection) {...}</pre></code>
 * would become:<br>
 * <code><pre>  public void execute(HTTPSecureConnection c) {...}</pre></code>
 *
 * @author Daniel Mittelman <code><mittelmania [at] gmail.com></code>
 * @since 2015-08-25
 */
public class JavaTypeNameParser {
  boolean isGenericVariation(final String variableName) {
    // TODO: Spartanize this code
    if (typeName.equalsIgnoreCase(variableName))
      return true;
    if (typeName.toLowerCase().contains(variableName.toLowerCase()))
      return true;
    // TODO Daniel: Also examine examples from the dataset to find more
    // variations
    return false;
  }
  public final String typeName;
  /**
   * Instantiates this class
   *
   * @param name the Java type name to parse
   */
  public JavaTypeNameParser(final String name) {
    typeName = name;
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
  String shortName() {
    return String.valueOf(Character.toLowerCase(lastName().charAt(0)));
  }
  private boolean isUpper(final int i) {
    return Character.isUpperCase(typeName.charAt(i));
  }
  String[] components() {
    return new String[] { "" + hashCode(), toString(), getClass().getSimpleName(), getClass().getCanonicalName() };
  }
  Iterable<String> suggestions() {
    return new Iterable<String>() {
      @Override public Iterator<String> iterator() {
        return new Iterator<String>() {

          @Override public boolean hasNext() {
            // TODO Auto-generated method stub
            return false;
          }

          @Override public String next() {
            // TODO Auto-generated method stub
            return null;
          }

          @Override public void remove() {
            // TODO Auto-generated method stub

          }
        };
      }
    };
  }
}
