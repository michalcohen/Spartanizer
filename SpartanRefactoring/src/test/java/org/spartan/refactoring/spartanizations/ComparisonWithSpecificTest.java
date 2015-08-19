package org.spartan.refactoring.spartanizations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.spartan.refactoring.spartanizations.ComparisonWithSpecific.applicable;
import static org.spartan.refactoring.spartanizations.ComparisonWithSpecific.withinDomain;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertNoOpportunity;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertNotEvenSimilar;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertOneOpportunity;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertSimilar;
import static org.spartan.refactoring.spartanizations.TESTUtils.rewrite;
import static org.spartan.refactoring.utils.Into.e;
import static org.spartan.refactoring.utils.Into.i;
import static org.spartan.refactoring.wring.TrimmerTest.countOpportunities;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.spartan.refactoring.utils.As;
import org.spartan.utils.Range;

/**
 * * Unit tests for {@link ComparisonWithSpecific}
 *
 * @author Yossi Gil
 * @since 2014-07-10
 */
// @RunWith(PowerMockRunner.class) //
// @PrepareForTest({ Expression.class, /* ASTNode.class */ }) //
// @PrepareForTest({ TEST.AFinalClass.class, TEST.Node.class, ASTNode.class,
// Expression.class, }) //
@FixMethodOrder(MethodSorters.JVM) //
@SuppressWarnings({ "static-method", "javadoc" }) //
public class ComparisonWithSpecificTest {
  private static final String P0 = "package p; public class Blah { public void y() {  if (null == a)  return;}}";
  private static final String P1 = "package p; public class Blah { public void y() {  if (a == null)  return;}}";
  private static final String P2 = "package p; \n" + //
      "public class SpongeBob {\n" + //
      " public boolean squarePants() {\n" + //
      " return a*b*c*d>d*e;\n" + //
      " }"//
      + "";
  @Test public void applicableCompareWithThis() {
    assertFalse(applicable(i("a == this")));
  }
  @Test public void applicableThisCompareWithThis() {
    assertTrue(applicable(i("this ==this")));
  }
  @Test public void breakExpression() {
    final InfixExpression i = i("a == this");
    assertEquals(ASTNode.INFIX_EXPRESSION, i.getNodeType());
  }
  @Test public void getNodeType() {
    assertEquals(ASTNode.THIS_EXPRESSION, e("this").getNodeType());
  }
  @Test public void noOpportunity0() {
    assertNoOpportunity(new ShortestOperand(), P0);
  }
  @Test public void noOpportunity1() {
    assertOneOpportunity(new ShortestOperand(), P2);
  }
  @Test public void noOpportunitySp1() {
    assertNoOpportunity(new ShortestOperand(), P1);
  }
  @Test public void one() {
    assertOneOpportunity(inner(), P0);
  }
  @Test public void one1() {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(P0);
    final Document rewrite = rewrite(inner(), u, new Document(P0));
    assertNotNull(rewrite);
  }
  @Test public void one2false() {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(P0);
    final Document rewrite = rewrite(inner(), u, new Document(P0));
    assertNotNull(rewrite);
    assertNotEvenSimilar(P0, rewrite.get());
  }
  @Test public void one2true() {
    final String expected = P1;
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(P0);
    final Document rewrite = rewrite(inner(), u, new Document(P0));
    assertSimilar(expected, rewrite);
  }
  @Test public void one2true0() {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(P0);
    rewrite(inner(), u, new Document(P0));
    assertNotNull(u);
  }
  @Test public void one2true1() {
    final Document d = new Document(P0);
    assertNotNull(d);
    assertSimilar(P1, rewrite(inner(), (CompilationUnit) As.COMPILIATION_UNIT.ast(P0), d));
  }
  @Test public void one2true2() {
    final Document d = new Document(P0);
    assertNotNull(d);
    assertSimilar(P0, d);
  }
  @Test public void one2true3() {
    final Document d = new Document(P0);
    assertNotNull(d);
    assertEquals(P0, d.get());
  }
  @Test public void one2true4() throws MalformedTreeException, IllegalArgumentException, BadLocationException {
    final Document d = new Document(P0);
    assertNotNull(d);
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(P0);
    final ComparisonWithSpecific s = inner();
    s.createRewrite(u, null).rewriteAST(d, null).apply(d);
    assertSimilar(P1, d);
  }
  @Test public void one2true5() {
    final Document d = new Document(P0);
    assertNotNull(d);
    assertSimilar(P1, rewrite(inner(), (CompilationUnit) As.COMPILIATION_UNIT.ast(P0), d));
  }
  @Test public void one2true6() throws MalformedTreeException, IllegalArgumentException, BadLocationException {
    final Document d = new Document(P0);
    assertNotNull(d);
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(P0);
    final ComparisonWithSpecific s = inner();
    final ASTRewrite r = s.createRewrite(u, null);
    assertNotNull(r);
    r.rewriteAST(d, null).apply(d);
    assertSimilar(P1, d);
  }
  @Test public void one2true7() throws MalformedTreeException, IllegalArgumentException, BadLocationException {
    final String expected = P1;
    final Document d = new Document(P0);
    assertNotNull(d);
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(P0);
    final ComparisonWithSpecific s = inner();
    final ASTRewrite r = s.createRewrite(u, null);
    r.rewriteAST(d, null).apply(d);
    assertSimilar(expected, d);
  }
  @Test public void oneOpportunitySP2() {
    assertOneOpportunity(new ShortestOperand(), P2);
  }
  @Test public void t1() {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(P2);
    assertEquals(u.toString(), 1, countOpportunities(new ShortestOperand(), u));
  }
  @Test public void t2() {
    final CompilationUnit u = (CompilationUnit) As.COMPILIATION_UNIT.ast(P2);
    final ShortestOperand shortestOperand = new ShortestOperand();
    final List<Range> $ = new ArrayList<>();
    u.accept(shortestOperand.collectOpportunities($));
    final List<Range> findOpportunities = $;
    assertNotNull(findOpportunities);
    final int countOpportunities = findOpportunities.size();
    assertEquals(u.toString(), 1, countOpportunities);
  }
  @Test public void withinDomainFalse0() {
    assertFalse(withinDomain(i("13455643294 < 22")));
  }
  @Test public void withinDomainFalse1() {
    assertFalse(withinDomain(i("1 < 102333")));
  }
  @Test public void withinDomainFalse2() {
    assertFalse(withinDomain(i("1 + 2 < 3 & 7 + 4 > 2 + 1 || 6 - 7 < 2 + 1")));
  }
  @Test public void withinDomainFalse3() {
    assertFalse(withinDomain(i("1 + 2 < 3 & 7 + 4 > 2 + 1")));
  }
  @Test public void withinDomainFalse4() {
    assertFalse(withinDomain(i(" 6 - 7 < 2 + 1   ")));
  }
  @Test public void withinDomainFalse5() {
    assertFalse(withinDomain(i("13455643294 < 22")));
  }
  @Test public void withinDomainFalse6() {
    assertFalse(withinDomain(i("1 < 102333")));
  }
  @Test public void withinDomainFalse7() {
    assertFalse(withinDomain(i("1 + 2 < 3 & 7 + 4 > 2 + 1 || 6 - 7 < 2 + 1")));
  }
  @Test public void withinDomainFalse8() {
    assertFalse(withinDomain(i("1 + 2 < 3 & 7 + 4 > 2 + 1")));
  }
  @Test public void withinDomainFalse9() {
    assertFalse(withinDomain(i(" 6 - 7 < 2 + 1   ")));
  }
  @Test public void withinDomainTrue1() {
    assertTrue(withinDomain(i("a == this")));
  }
  @Test public void withinDomainTrue2() {
    assertTrue(withinDomain(i("this == null")));
  }
  @Test public void withinDomainTrue3() {
    assertTrue(withinDomain(i("12 == this")));
  }
  @Test public void withinDomainTrue4() {
    assertTrue(withinDomain(i("a == 11")));
  }
  private ComparisonWithSpecific inner() {
    return new ComparisonWithSpecific();
  }
}
