package org.spartan.refactoring.spartanizations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertSimilar;
import static org.spartan.refactoring.spartanizations.TESTUtils.compressSpaces;

import java.util.Collection;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.spartan.refactoring.utils.As;
import org.spartan.refactoring.wring.AbstractTestBase;
import org.spartan.utils.Utils;

/**
 * Unit tests for {@link Ternarize}.
 *
 * @author Yossi Gil
 * @since 2014-07-13
 */
@RunWith(Parameterized.class) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
@SuppressWarnings({ "javadoc" }) //
public class TernarizeTest extends AbstractTestBase {
  final static Spartanization inner = new Ternarize();
  static String[][] cases = Utils.asArray(//
      // Utils.asArray(
      // " String res = s;\n" + //
      // "xif (s.equals(\"yada\")==true)\n" + //
      // " res = s + \" blah\";\n" + //
      // "else\n" + //
      // "res = \"spam\";" + //
      // "",
      // "" + //
      // "String res = (s.equals(\"yada\")==true ? s + \" blah\" : \"spam\");\n"
      // + //
      // "System.out.println(res);"), //
      Utils.asArray("04.test",
          "" + //
              "int res=0;      " + //
              "if (s.equals(xxx)) res+=6;      " + //
              "else res+=9;      " + //
              "return res;", //
              "" + //
              "int res = 0;\n" + //
          "res += (s.equals(xxx) ? 6 : 9);"), //
      null);
  /**
   * Generate test cases for this parameterized class.
   *
   * @return a collection of cases, where each case is an array of three
   *         objects, the test case name, the input, and the file.
   */
  @Parameters(name = "{index}: {0} {1}") //
  public static Collection<Object[]> cases() {
    return collect(cases);
  }
  /** Where the expected output can be found? */
  @Parameter(2) public String output;
  @Test public void peelableOutput() {
    assertEquals(output, Wrap.Statement.off(Wrap.Statement.on(output)));
  }
  @Test public void rewrite() throws MalformedTreeException, IllegalArgumentException, BadLocationException {
    final String s = input;
    final String wrap = Wrap.Statement.on(s);
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(wrap);
    final Document d = new Document(wrap);
    assertNotNull(d);
    final AST t = u.getAST();
    assertNotNull(t);
    final ASTRewrite r = ASTRewrite.create(t);
    inner.fillRewrite(r, t, u, null);
    r.rewriteAST(d, null).apply(d);
    final String unpeeled = d.get();
    if (wrap.equals(unpeeled))
      fail("Nothing done on " + input);
  }
  @Test public void simiplifies() throws MalformedTreeException, IllegalArgumentException, BadLocationException {
    final String s = input;
    final String wrap = Wrap.Statement.on(s);
    final Document d = new Document(wrap);
    assertNotNull(d);
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(wrap);
    final ASTRewrite r = inner.createRewrite(u, null);
    r.rewriteAST(d, null).apply(d);
    final String unpeeled = d.get();
    if (wrap.equals(unpeeled))
      fail("Nothing done on " + input);
    final String peeled = Wrap.Statement.off(unpeeled);
    if (peeled.equals(input))
      assertNotEquals("No similification of " + input, input, peeled);
    if (compressSpaces(peeled).equals(compressSpaces(input)))
      assertNotEquals("Simpification of " + input + " is just reformatting", compressSpaces(peeled), compressSpaces(input));
    assertSimilar(output, peeled);
    final String s1 = output;
    assertSimilar(Wrap.Statement.on(s1), d);
  }
}
