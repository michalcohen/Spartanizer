package org.spartan.refactoring.spartanizations;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refactoring.utils.Occurrences;
import org.spartan.utils.Range;


/**
 * A {@link Spartanization} that shortens the parameter name in methods
 * that have only a single parameter. The short name will be
 * the first character in the last word of the parameter's name. <br><br>
 * For example:<br>
 * <code><pre>  public void execute(HTTPSecureConnection httpSecureConnection) {...}</pre></code>
 * would become:<br>
 * <code><pre>  public void execute(HTTPSecureConnection c) {...}</pre></code>
 * @author Daniel Mittelman <code><mittelmania [at] gmail.com></code>
 * @since 2015-08-25
 *
 */
public class GenericParameterName extends Spartanization {
  protected GenericParameterName() {
    super("Generic Parameter Name", "Shorten parameter names in appropriate cases");
  }
  @Override protected void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final MethodDeclaration node) {
        if(node.parameters().size() != 1)
          return true;
        final SingleVariableDeclaration v = (SingleVariableDeclaration)node.parameters().get(0);
        if(!isGenericVariation(v.getType().toString(), v.getName().getIdentifier()))
          return true;

        // TODO Daniel: Compute the method's environment
        // TODO Daniel: Check against the method environment
        // TODO Daniel: Rename if necessary
        return true;
      }
    });

  }
  @Override protected ASTVisitor collectOpportunities(final List<Range> $) {
    // TODO Daniel: Complete
    return null;
  }
  private static String shortenName(final String n) {
    if(n == null) return null;
    int i;
    for(i = n.length() - 1 ; i > 0 ; --i) {
      if(Character.isLowerCase(n.charAt(i)) && Character.isUpperCase(n.charAt(i-1))) {
        --i;
        break;
      }
      if(Character.isUpperCase(n.charAt(i)) && Character.isLowerCase(n.charAt(i-1)))
        break;
    }
    return String.valueOf(Character.toLowerCase(n.charAt(i)));
  }
  private static boolean isGenericVariation(final String t, final String n) {
    if(t.equalsIgnoreCase(n)) return true;
    if(n.toLowerCase().contains(t.toLowerCase())) return true;
    // TODO Daniel: Also examine examples from the dataset to find more variations
    return false;
  }
  private static List<String> getMethodEnvironment(final MethodDeclaration d) {
    // TODO Daniel: Complete
    // TODO Daniel: getBody may return null if the method has no body
    d.getBody().accept(new ASTVisitor() {
    });
    return null;
  }
}
