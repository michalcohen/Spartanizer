package org.spartan.refactoring.spartanizations;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.spartan.utils.Utils;

/**
 * Unit tests for {@link Wrings#ADDITION_SORTER}.
 *
 * @author Yossi Gil
 * @since 2014-07-13
 *
 */
@RunWith(Parameterized.class) //
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
public class Engine_ADDITION_SORTER_NOCHANGE extends Engine_Common_NOCHANGE {
  public Engine_ADDITION_SORTER_NOCHANGE() {
    super(Wrings.ADDITION_SORTER.inner);
  }
  @Override protected String input() {
    return input;
  }

  /**
   * The name of the specific test for this transformation
   */
  @Parameter(value = 0) public String name;
  /**
   * Where the input text can be found
   */
  @Parameter(value = 1) public String input;

  /**
   * Generate test cases for this parameterized class.
   *
   * @return a collection of cases, where each case is an array of three
   *         objects, the test case name, the input, and the file.
   */
  @Parameters(name = "{index}: {0} {1}") //
  public static Collection<Object[]> cases() {
    final Collection<Object[]> $ = new ArrayList<>(cases.length);
    for (final String[] t : cases)
      $.add(t);
    return $;
  }

  static String[][] cases = Utils.asArray(//
      Utils.asArray("Add 1", "a+1"), //
      Utils.asArray("Add '1'", "a+'1'"), //
      Utils.asArray("Add '\0'", "a+'\0'"), //
      Utils.asArray("Plain addition", "a+b"), //
      Utils.asArray("Literal addition", "2+3") //
  );

  static Document rewrite(final Spartanization s, final CompilationUnit u, final Document d) {
    try {
      s.createRewrite(u, null).rewriteAST(d, null).apply(d);
      return d;
    } catch (final MalformedTreeException e) {
      fail(e.getMessage());
    } catch (final IllegalArgumentException e) {
      fail(e.getMessage());
    } catch (final BadLocationException e) {
      fail(e.getMessage());
    }
    return null;
  }
}