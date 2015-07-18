package org.spartan.refactoring.utils;

import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.refactoring.spartanizations.TESTUtils.i;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Yossi Gil
 * @since 2015-07-18
 *
 */
@SuppressWarnings({ "static-method", "javadoc" }) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public class AllTest {
  @Test public void operandsCount() {
    final InfixExpression e = i("a+b+c+(d+e)+f");
    final List<Expression> operands = All.operands(e);
    assertThat(operands.size(), is(5));
  }
}
