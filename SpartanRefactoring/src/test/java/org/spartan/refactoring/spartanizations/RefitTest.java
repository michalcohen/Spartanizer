package org.spartan.refactoring.spartanizations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.refactoring.spartanizations.TESTUtils.i;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.junit.Test;
import org.spartan.refactoring.utils.All;
import org.spartan.refactoring.utils.Funcs;

/**
 * Test suite for {@link Wrings}
 *
 * @author Yossi Gil
 * @since 2015-07-17
 *
 */
@SuppressWarnings({ "javadoc", "static-method" }) //
public class RefitTest {
  @Test public void refitNotNull() {
    final InfixExpression e = i("1+2+3");
    final List<Expression> operands = All.operands(Funcs.duplicate(e.getAST(), i("a+b+c")));
    assertThat(Wrings.refit(e, operands), notNullValue());
  }
  @Test public void refitIsCorrecct() {
    final InfixExpression e = i("1+2+3");
    final List<Expression> operands = All.operands(Funcs.duplicate(e.getAST(), i("a*b*c")));
    assertThat(Wrings.refit(e, operands).toString(), is("a + b + c"));
  }
  @Test public void refitDoesNotIntroduceList() {
    final InfixExpression e = i("1+2");
    final List<Expression> operands = All.operands(Funcs.duplicate(e.getAST(), i("a*b")));
    assertThat(operands.size(), is(2));
    final InfixExpression refit = Wrings.refit(e, operands);
    assertThat(refit.hasExtendedOperands(), is(false));
    assertThat(refit.toString(), is("a + b"));
  }
}
