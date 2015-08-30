package org.spartan.refactoring.utils;

public class LiteralParser {
  final String literal;
  public LiteralParser(final String literal) {
    this.literal = literal;
  }
  public int kind() {
    if (literal.charAt(0) == '\'')
      return Kinds.CHARACTER.ordinal();
    switch (literal.charAt(literal.length() - 1)) {
      case 'l':
      case 'L':
        return Kinds.LONG.ordinal();
      case 'f':
      case 'F':
        return Kinds.FLOAT.ordinal();
      case 'd':
      case 'D':
      case 'p':
      case 'P':
        return Kinds.DOUBLE.ordinal();
      default:
        if (literal.indexOf('.') >= 0)
          return Kinds.DOUBLE.ordinal();
        if (literal.indexOf('E') >= 0 || literal.indexOf('e') >= 0)
          return Kinds.DOUBLE.ordinal();
        return Kinds.INTEGER.ordinal();
    }
  }

  public enum Kinds {
    INTEGER, LONG, CHARACTER, FLOAT, DOUBLE;
  }
}
