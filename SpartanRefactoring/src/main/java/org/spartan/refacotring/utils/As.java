package org.spartan.refacotring.utils;

import static org.eclipse.jdt.core.dom.PrefixExpression.Operator.NOT;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;

/**
 * A an empty <code><b>enum</b></code> for fluent programming. The name says it
 * all: The name, followed by a dot, followed by a method name, should read like
 * a word phrase.
 *
 * @author Yossi Gil
 * @since 2015-07-16
 *
 */
public enum As {
  COMPILIATION_UNIT(ASTParser.K_COMPILATION_UNIT) {
    @Override public CompilationUnit ast(final File f) {
      return ast(string(f));
    }
    @Override public CompilationUnit ast(final String s) {
      return (CompilationUnit) makeParser(s).createAST(null);
    }
    @Override public CompilationUnit ast(final IMarker m, final SubProgressMonitor pm) {
      return (CompilationUnit) Make.COMPILIATION_UNIT.parser(m).createAST(pm);
    }
    @Override public CompilationUnit ast(final IFile f) {
      return (CompilationUnit) Make.COMPILIATION_UNIT.parser(f).createAST(null);
    }
  },
  EXPRESSION(ASTParser.K_EXPRESSION) {
    @Override public Expression ast(final File f) {
      return ast(string(f));
    }
    @Override public Expression ast(final String s) {
      return (Expression) makeParser(s).createAST(null);
    }
    @Override public Expression ast(final IMarker m, final SubProgressMonitor pm) {
      return (Expression) Make.EXPRESSION.parser(m).createAST(pm);
    }
    @Override public Expression ast(final IFile f) {
      return (Expression) Make.EXPRESSION.parser(f).createAST(null);
    }
  },
  STATEMENTS(ASTParser.K_STATEMENTS), CLASS_BODY_DECLARATIONS(ASTParser.K_CLASS_BODY_DECLARATIONS);
  final int kind;

  private As(final int kind) {
    this.kind = kind;
  }
  public ASTNode ast(final File f) {
    return ast(string(f));
  }
  public ASTNode ast(final String s) {
    return makeParser(s).createAST(null);
  }
  public static ICompilationUnit iCompilationUnit(final IMarker m) {
    return iCompilationUnit((IFile) m.getResource());
  }
  public static ICompilationUnit iCompilationUnit(final IFile f) {
    return JavaCore.createCompilationUnitFrom(f);
  }
  public ASTNode ast(final IMarker m, final SubProgressMonitor pm) {
    return Make.of(this).parser(m).createAST(pm);
  }
  public ASTNode ast(final IFile f) {
    return Make.of(this).parser(f).createAST(null);
  }
  /**
   * Convert file contents into a {@link String}
   *
   * @param f
   *          a file
   * @return the entire contents of this file, as one string
   */
  public static String string(final File f) {
    try (final BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
      final StringBuilder $ = new StringBuilder();
      for (String line = r.readLine(); line != null; line = r.readLine())
        $.append(line).append(System.lineSeparator());
      return $.toString();
    } catch (final IOException e) {
      fail(e.toString());
      return null;
    }
  }
  /**
   * Creates a StringBuilder object out of a file object.
   */
  public static StringBuilder stringBuilder(final File f) {
    try (final Scanner $ = new Scanner(f)) {
      return new StringBuilder($.useDelimiter("\\Z").next());
    } catch (final Exception e) {
      return new StringBuilder("");
    }
  }
  /**
   * Creates a no-binding parser for a given text
   *
   * @param text
   *          what to parse
   * @return a newly created parser for the parameter
   */
  public ASTParser makeParser(final String text) {
    return makeParser(text.toCharArray());
  }
  /**
   * Creates a no-binding parser for a given text
   *
   * @param text
   *          what to parse
   * @return a newly created parser for the parameter
   */
  public ASTParser makeParser(final char[] text) {
    final ASTParser $ = makeParser();
    $.setSource(text);
    return $;
  }
  /**
   * Creates a no-binding parser for a given compilation unit
   *
   * @param u
   *          what to parse
   * @return a newly created parser for the parameter
   */
  public ASTParser makeParser(final ICompilationUnit u) {
    final ASTParser $ = makeParser();
    $.setSource(u);
    return $;
  }
  private ASTParser makeParser() {
    final ASTParser $ = ASTParser.newParser(AST.JLS8);
    $.setKind(kind);
    $.setResolveBindings(false);
    return $;
  }
  private static ReturnStatement asReturn(final Block b) {
    return b.statements().size() != 1 ? null : asReturn((Statement) b.statements().get(0));
  }
  /**
   * @param s
   *          The node from which to return statement.
   * @return null if it is not possible to extract the return statement.
   */
  public static ReturnStatement asReturn(final ASTNode s) {
    if (s == null)
      return null;
    switch (s.getNodeType()) {
      case ASTNode.BLOCK:
        return asReturn((Block) s);
      case ASTNode.RETURN_STATEMENT:
        return (ReturnStatement) s;
      default:
        return null;
    }
  }
  /**
   * @param s
   *          a statement or a block to extract the expression statement from
   * @return the expression statement if n is a block or an expression statement
   *         or null if it not an expression statement or if the block contains
   *         more than one statement
   */
  public static ExpressionStatement asExpressionStatement(final Statement s) {
    if (s == null)
      return null;
    final ASTNode $ = !Is.block(s) ? s : Funcs.getBlockSingleStmnt(s);
    return !Is.expressionStatement($) ? null : (ExpressionStatement) $;
  }
  /**
   * Convert, is possible, an {@link ASTNode} to a {@link Block}
   *
   * @param n
   *          what to convert
   * @return the argument, but downcasted to a {@link Block}, or
   *         <code><b>null</b></code> otherwise.
   */
  public static Block asBlock(final ASTNode n) {
    return !(n instanceof Block) ? null : (Block) n;
  }
  public static PrefixExpression not(final PrefixExpression e) {
    return NOT.equals(e.getOperator()) ? e : null;
  }
  public static PrefixExpression not(final Expression e) {
    return !(e instanceof PrefixExpression) ? null : As.not((PrefixExpression) e);
  }
  public static InfixExpression infixExpression(final Expression e) {
    return !(e instanceof InfixExpression) ? null : (InfixExpression) e;
  }
}
