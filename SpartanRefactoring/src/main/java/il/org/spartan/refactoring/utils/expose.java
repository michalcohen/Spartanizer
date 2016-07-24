package il.org.spartan.refactoring.utils;

import java.util.*;

import org.eclipse.jdt.core.dom.*;

/** An empty <code><b>interface</b></code> for fluent programming. The name
 * should say it all: The name, followed by a dot, followed by a method name,
 * should read like a sentence phrase.
 * @author Yossi Gil
 * @since 2015-07-16 */
public interface expose {
  /** Expose the list of arguments in a {@link ClassInstanceCreation}
   * @param c JD
   * @return a reference to the list of arguments in the argument */
  static List<Expression> arguments(final ClassInstanceCreation c) {
    return ast.expressions(c.arguments());
  }
  /** Expose the list of arguments in a {@link MethodInvocation}
   * @param i JD
   * @return a reference to the list of arguments in the argument */
  static List<Expression> arguments(final MethodInvocation i) {
    return ast.expressions(i.arguments());
  }
  /** Expose the list of arguments in a {@link SuperMethodInvocation}
   * @param i JD
   * @return a reference to the list of arguments in the argument */
  static List<Expression> arguments(final SuperMethodInvocation i) {
    return ast.expressions(i.arguments());
  }
  /** Expose the list of extended operands in an {@link InfixExpression}
   * @param e JD
   * @return a reference to the list of extended operands contained in the
   *         parameter */
  static List<Expression> extendedOperands(final InfixExpression e) {
    @SuppressWarnings("unchecked") final List<Expression> $ = e.extendedOperands();
    return $;
  }
  /** Expose the list of fragments in a {@link FieldDeclaration}
   * @param d JD
   * @return a reference to the list of fragments in the argument */
  static List<VariableDeclarationFragment> fragments(final FieldDeclaration d) {
    return ast.fragments(d.fragments());
  }
  /** Expose the list of fragments in a {@link VariableDeclarationExpression}
   * @param e JD
   * @return a reference to the list of fragments in the argument */
  static List<VariableDeclarationFragment> fragments(final VariableDeclarationExpression e) {
    return ast.fragments(e.fragments());
  }
  /** Expose the list of fragments in a {@link VariableDeclarationStatement}
   * @param s JD
   * @return a reference to the list of fragments in the argument */
  static List<VariableDeclarationFragment> fragments(final VariableDeclarationStatement s) {
    return ast.fragments(s.fragments());
  }
  /** Expose the list of parameters in a {@link MethodDeclaration}
   * @param ¢ JD
   * @return result of method {@link MethodDeclaration#parameters} downcasted to
   *         its correct type */
  @SuppressWarnings("unchecked") public static List<SingleVariableDeclaration> parameters(final MethodDeclaration ¢) {
    return ¢.parameters();
  }
  /** Expose the list of statements contained in a {@link Block}
   * @param ¢ JD
   * @return a reference to the list of statements contained in the argument */
  static List<Statement> statements(final Block ¢) {
    return ast.statements(¢.statements());
  }
  /** Expose the list of initializers contained in a {@link ForStatement}
   * @param ¢ JD
   * @return a reference to the list of initializers contained in the argument */
  @SuppressWarnings("unchecked") static List<VariableDeclarationExpression> initializers(final ForStatement ¢) {
    return ¢.initializers();
  }
  /** Expose the list of resources contained in a {@link TryStatement}
   * @param ¢ JD
   * @return a reference to the list of resources contained in the argument */
  @SuppressWarnings("unchecked") static List<VariableDeclarationExpression> resources(final TryStatement ¢) {
    return ¢.resources();
  }
  /** Expose the list of resources contained in a {@link ParameterizedType}
   * @param ¢ JD
   * @return a reference to the list of resources contained in the argument */
  @SuppressWarnings("unchecked") static List<Type> typeArguments(final ParameterizedType ¢) {
    return ¢.typeArguments();
  }
  /** Expose the list of resources contained in a {@link ParameterizedType}
   * @param ¢ JD
   * @return a reference to the list of resources contained in the argument */
  @SuppressWarnings("unchecked") static List<TagElement> tags(final Javadoc ¢) {
    return ¢.tags();
  }
}
