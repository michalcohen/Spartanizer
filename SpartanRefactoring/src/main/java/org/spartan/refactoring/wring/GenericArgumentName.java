package org.spartan.refactoring.wring;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.spartan.refactoring.utils.JavaTypeNameParser;

/**
 * A {@link Wring} to convert a method argument's name to its short version
 * where there's a single method argument. <br>
 * For example:
 * <code><pre>  public void execute(HTTPSecureConnection httpSecureConnection) {...}</pre></code>
 * would become<br>
 * <code><pre>  public void execute(HTTPSecureConnection c) {...}</pre></code>
 *
 * @see JavaTypeNameParser
 * @author Daniel Mittelman <code><mittelmania [at] gmail.com></code>
 */
public class GenericArgumentName extends Wring.ReplaceCurrentNode<MethodDeclaration> {
  @Override ASTNode replacement(final MethodDeclaration n) {
    // TODO Check that the method has exactly one argument
    // TODO Check that the argument's name is a generic variation of the type
    // name
    // TODO Calculate the environment in a very coarse way (where there's doubt
    // - assume the identifier is part of the environment)
    // TODO Iterate over the environment, if the short name is not part of it,
    // use it as replacement
    // TODO Expand tests
    return null;
  }
  @Override String description(final MethodDeclaration n) {
    return "Make the variable name " + ((SingleVariableDeclaration) n.parameters().get(0)).getName().getIdentifier() + " generic";
  }
}
