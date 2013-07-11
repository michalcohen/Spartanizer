package il.ac.technion.cs.ssdl.spartan.builder;

import java.util.Arrays;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

/**
 * @author Yossi Gil <yossi.gil@gmail.com>
 * @since 2013/07/01
 * 
 */
public enum Utils {
  ;
  /**
   * Creates a no-binding parser for a given compilation unit
   * 
   * @param cu
   *          what to parse
   * @return a newly created parser for the parameter
   */
  public static ASTParser makeParser(final ICompilationUnit cu) {
    final ASTParser $ = makeParser();
    $.setSource(cu);
    return $;
  }
  
  /**
   * Creates a no-binding parser for a given text
   * 
   * @param text
   *          what to parse
   * @return a newly created parser for the parameter
   */
  public static ASTParser makeParser(final String text) {
    return makeParser(text.toCharArray());
  }
  
  /**
   * Creates a no-binding parser for a given text
   * 
   * @param text
   *          what to parse
   * @return a newly created parser for the parameter
   */
  public static ASTParser makeParser(final char[] text) {
    final ASTParser $ = makeParser();
    $.setSource(text);
    return $;
  }
  
  private static ASTParser makeParser() {
    final ASTParser $ = ASTParser.newParser(AST.JLS4);
    $.setKind(ASTParser.K_COMPILATION_UNIT);
    $.setResolveBindings(false);
    return $;
  }
  
  /**
   * Appends an element to an array, by reallocating an array whose size is
   * greater by one and placing the element at the last position.
   * 
   * @param ts
   *          an arbitrary array
   * @param t
   *          an element
   * @return the newly created array
   */
  public static <T> T[] append(final T[] ts, final T t) {
    final T[] $ = Arrays.copyOf(ts, ts.length + 1);
    $[ts.length] = t;
    return $;
  }
}
