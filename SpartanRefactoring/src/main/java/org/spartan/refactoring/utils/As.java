package org.spartan.refactoring.utils;

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
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;

/**
 * An empty <code><b>enum</b></code> for fluent programming. The name should say
 * it all: The name, followed by a dot, followed by a method name, should read
 * like a word phrase.
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
   * Creates a {@link StringBuilder} object out of a file object.
   *
   * @param f
   *          JD
   * @return {@link StringBuilder} whose content is the same as the contents of
   *         the parameter.
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
}
