package org.spartan.refactoring.spartanizations;
import static org.eclipse.jdt.core.dom.ASTNode.CHARACTER_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.NULL_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.NUMBER_LITERAL;
import static org.eclipse.jdt.core.dom.ASTNode.THIS_EXPRESSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.spartan.refactoring.spartanizations.ComparisonWithSpecific.applicable;
import static org.spartan.refactoring.spartanizations.ComparisonWithSpecific.withinDomain;
import static org.spartan.refactoring.spartanizations.Into.assertNoOpportunity;
import static org.spartan.refactoring.spartanizations.Into.assertOneOpportunity;
import static org.spartan.refactoring.spartanizations.Into.e;
import static org.spartan.refactoring.spartanizations.Into.i;
import static org.spartan.refactoring.spartanizations.TESTUtils.assertSimilar;
import static org.spartan.refactoring.wring.TrimmerTest.countOpportunities;
import static org.spartan.utils.Utils.hasNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jface.text.Document;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.spartan.refactoring.utils.As;
import org.spartan.refactoring.utils.Is;
import org.spartan.utils.Range;
/**
 * * Unit tests for {@link ShortestOperand}
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
public class ShortestOperandTest {
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
    assertEquals(ASTNode.INFIX_EXPRESSION, i("a == this").getNodeType());
  }
  @Test public void callIsSpecificTrue() {
    assertTrue(Is.specific(e("this")));
  }
  @Test public void cisSpecificFalse1() {
    assertFalse(Is.specific(e("a")));
  }
  @Test public void getNodeType() {
    assertEquals(ASTNode.THIS_EXPRESSION, e("this").getNodeType());
  }
  @Test public void isOneOf() {
    assertTrue(Is.oneOf(e("this"), CHARACTER_LITERAL, NUMBER_LITERAL, NULL_LITERAL, THIS_EXPRESSION));
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
  @Test public void t3() {
    assertFalse(ShortestOperand.outOfScope(i("1 + 2 < 3 & 7 + 4 > 2 + 1 || 6 - 7 < 2 + 1")));
  }
  @Test public void t4() {
    assertFalse(ShortestOperand.outOfScope(i("1 + 2 < 3 & 7 + 4 > 2 + 1")));
  }
  @Test public void t5() {
    assertFalse(ShortestOperand.outOfScope(i(" 6 - 7 < 2 + 1")));
  }
  @Test public void t6() {
    final ShortestOperand s = new ShortestOperand();
    final List<Range> opportunities = new ArrayList<>();
    final ASTVisitor a = s.collectOpportunities(opportunities);
    final InfixExpression e = i("1 + 2 < 3 & 7 + 4 > 2 + 1 || 6 - 7 < 2 + 1");
    a.visit(e);
    assertFalse(ShortestOperand.outOfScope(e));
  }
  @Test public void t7() {
    final InfixExpression e = i("1 + 2  + s < 3 ");
    final ShortestOperand s = new ShortestOperand();
    final List<Range> opportunities = new ArrayList<>();
    final ASTVisitor a = s.collectOpportunities(opportunities);
    a.visit(e);
    assertTrue(ShortestOperand.outOfScope(e));
  }
  @Test public void t8() {
    final InfixExpression e = i("1 + 2  + 3 < 3 ");
    assertTrue(ShortestOperand.outOfScope(e));
  }
  @Test public void t9() {
    final InfixExpression e = i("1 + 2  + 3 < 3 -4");
    assertNotNull(e);
    assertFalse(hasNull(e.getLeftOperand(), e.getRightOperand()));
    assertFalse(ShortestOperand.stringReturningMethod(e));
    assertFalse(ShortestOperand.containsStringLiteral(e));
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
}