package il.org.spartan.spartanizer.engine;

import org.eclipse.jdt.core.dom.*;

import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.text.edits.*;

import il.org.spartan.spartanizer.ast.navigate.*;
import il.org.spartan.spartanizer.tipping.*;
import il.org.spartan.utils.*;

/** A function object representing a sequence of operations on an
 * {@link ASTRewrite} object.
 * @author Yossi Gil
 * @since 2015-08-28 */
public abstract class Tip extends ShortTip {

  public Tip(Range other, String description, int lineNumber, Class<? extends Tipper<?>> tipperClass) {
    super(other, description, lineNumber, tipperClass);
  }

  /**
   * @param description
   * @param tipperClass
   * @param n
   * @param ns
   */
  public Tip(String description, Class<? extends Tipper<?>> tipperClass, ASTNode n, ASTNode... ns) {
    super(description, tipperClass, n, ns);
  }

  /** Convert the rewrite into changes on an {@link ASTRewrite}
   * @param r where to place the changes
   * @param g to be associated with these changes */
  public abstract void go(ASTRewrite r, TextEditGroup g);
}
