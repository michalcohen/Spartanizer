package il.ac.technion.cs.ssdl.spartan.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

public enum Make {
  COMPILIATION_UNIT {
    /**
     * @see il.ac.technion.cs.ssdl.spartan.utils.As#kind()
     * @see ASTParser#K_COMPILATION_UNIT
     */
    @Override int kind() {
      return ASTParser.K_COMPILATION_UNIT;
    }
  },
  EXPRESSION {
    /**
     * @see il.ac.technion.cs.ssdl.spartan.utils.As#kind()
     * @see ASTParser#K_EXPRESSION
     */
    @Override int kind() {
      return ASTParser.K_EXPRESSION;
    }
  },
  STATEMENTS {
    /**
     * @see il.ac.technion.cs.ssdl.spartan.utils.As#kind()
     * @see ASTParser#K_STATEMENTS
     */
    @Override int kind() {
      return ASTParser.K_STATEMENTS;
    }
  },
  CLASS_BODY_DECLARATIONS {
    /**
     * @see il.ac.technion.cs.ssdl.spartan.utils.As#kind()
     * @see ASTParser#K_CLASS_BODY_DECLARATIONS
     */
    @Override int kind() {
      return ASTParser.K_CLASS_BODY_DECLARATIONS;
    }
  };
  /**
   * Creates a no-binding parser for a given text
   *
   * @param text
   *          what to parse
   * @return a newly created parser for the parameter
   */
  public ASTParser parser(final String text) {
    return parser(text.toCharArray());
  }

  /**
   * Creates a no-binding parser for a given text
   *
   * @param text
   *          what to parse
   * @return a newly created parser for the parameter
   */
  public ASTParser parser(final char[] text) {
    final ASTParser $ = parser();
    $.setSource(text);
    return $;
  }

  public ASTParser parser(final IMarker m) {
    return parser(As.iCompilationUnit(m));
  }

  public ASTParser parser(final IFile f) {
    return parser(JavaCore.createCompilationUnitFrom(f));
  }

  /**
   * Creates a no-binding parser for a given compilation unit
   *
   * @param u
   *          what to parse
   * @return a newly created parser for the parameter
   */
  public ASTParser parser(final ICompilationUnit u) {
    final ASTParser $ = parser();
    $.setSource(u);
    return $;
  }

  private ASTParser parser() {
    final ASTParser $ = ASTParser.newParser(AST.JLS8);
    $.setKind(kind());
    $.setResolveBindings(false);
    return $;
  }

  abstract int kind();

  public static Make of(final As a) {
    switch (a) {
      case STATEMENTS:
        return Make.STATEMENTS;
      case EXPRESSION:
        return Make.EXPRESSION;
      case COMPILIATION_UNIT:
        return Make.COMPILIATION_UNIT;
      case CLASS_BODY_DECLARATIONS:
        return Make.CLASS_BODY_DECLARATIONS;
      default:
        return null;
    }
  }
}
