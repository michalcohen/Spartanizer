package org.spartan.refactoring.utils;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;

import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.junit.*;

@SuppressWarnings({ "static-method", "javadoc" }) public class MethodExplorerTest {
  @Test public void returnStatementsExists() {
    final MethodDeclaration d = Into.d("int f() { return a; }");
    final List<ReturnStatement> a = new MethodExplorer(d).returnStatements();
    assertThat(a, notNullValue());
    assertThat(a.size(), is(1));
  }
  @Test public void returnStatementsTwoReturns() {
    final MethodDeclaration d = Into.d("int f() { if (b) ; else return c; return a; }");
    final List<ReturnStatement> a = new MethodExplorer(d).returnStatements();
    assertThat(a, notNullValue());
    assertThat(a.size(), is(2));
  }
  @Test public void returnStatementsExistsNestedType() {
    final MethodDeclaration d = Into.d("int f() { class B {}; return a; }");
    final List<ReturnStatement> a = new MethodExplorer(d).returnStatements();
    assertThat(a, notNullValue());
    assertThat(a.size(), is(1));
  }
  @Test public void returnStatementsExistsNestedTypeWithReturn() {
    final MethodDeclaration d = Into.d("int f() { class B {int g() { return c; } }; return a; }");
    final List<ReturnStatement> a = new MethodExplorer(d).returnStatements();
    assertThat(a, notNullValue());
    assertThat(a.size(), is(1));
  }
  @Test public void returnStatementsExistsNestedTypeWithReturn1() {
    final MethodDeclaration d = Into.d("  boolean f() {\n" + //
        "    if (f())\n" + //
        "      return f();\n" + //
        "    class B {\n" + //
        "      boolean g() {\n" + //
        "        return g();\n" + //
        "      }\n" + //
        "    }\n" + //
        "    return new B().g();\n" + //
        "  }"); //
    final List<ReturnStatement> a = new MethodExplorer(d).returnStatements();
    assertThat(a, notNullValue());
    assertThat(a.size(), is(2));
  }
  @Test public void returnStatementsExistsNestedTypeAnnotation() {
    final MethodDeclaration d = Into.d("  boolean f() {\n" + //
        "    @interface C{static class X{boolean f(){return f();}}}" + //
        "    if (f())\n" + //
        "      return f();\n" + //
        "    return new B().g();\n" + //
        "  }"); //
    final List<ReturnStatement> a = new MethodExplorer(d).returnStatements();
    assertThat(a, notNullValue());
    assertThat(a.size(), is(2));
  }
  @Test public void returnStatementsWithNestedEnum() {
    final MethodDeclaration d = Into.d("  int f() {\n" + //
        "    return new Object() {\n" + //
        "      @Override public boolean equals(Object obj) {\n" + //
        "        return super.equals(obj);\n" + //
        "      }\n" + //
        "      @Override public int hashCode() {\n" + //
        "        return super.hashCode();\n" + //
        "      }\n" + //
        "    }.hashCode();\n" + //
        "  }"); //
    final List<ReturnStatement> a = new MethodExplorer(d).returnStatements();
    assertThat(a.size(), is(1));
  }
  @Test public void localVariablesNone() {
    final MethodDeclaration d = Into.d("  int f() {\n" + //
        "    return new Object() {\n" + //
        "      @Override public boolean equals(Object obj) {\n" + //
        "        return super.equals(obj);\n" + //
        "      }\n" + //
        "      @Override public int hashCode() {\n" + //
        "        return super.hashCode();\n" + //
        "      }\n" + //
        "    }.hashCode();\n" + //
        "  }"); //
    final List<SimpleName> a = new MethodExplorer(d).localVariables();
    assertThat(a.size(), is(0));
  }
  @Test public void localVariablesVanilla() {
    final MethodDeclaration d = Into.d("  int f() {\n" + //
        "int a;\n" + //
        "  }"); //
    final List<SimpleName> a = new MethodExplorer(d).localVariables();
    assertThat(a.size(), is(1));
  }
  @Test public void localVariablesMultipleFragments() {
    final MethodDeclaration d = Into.d("  int f() {\n" + //
        "int a,b;\n" + //
        "  }"); //
    final List<SimpleName> a = new MethodExplorer(d).localVariables();
    assertThat(a.size(), is(2));
  }
  @Test public void localVariablesMultipleNestedFragments() {
    final MethodDeclaration d = Into.d("  int f() {\n" + //
        "int a,b;\n" + //
        "  {int c, d;}}"); //
    final List<SimpleName> a = new MethodExplorer(d).localVariables();
    assertThat(a.size(), is(4));
  }
  @Test public void localVariablesRepeatedNestedFragments() {
    final MethodDeclaration d = Into.d("  int f() {\n" + //
        "int a,b,c,d;\n" + //
        "  {int i, j;} {int i,j; int k;}"); //
    final List<SimpleName> a = new MethodExplorer(d).localVariables();
    assertThat(a.size(), is(9));
  }
  @Test public void localVariablesForLoopNoVariable() {
    final MethodDeclaration d = Into.d("  int f() {\n" + //
        "  for (f(); i*j <10; i += j++);  }"); //
    final List<SimpleName> a = new MethodExplorer(d).localVariables();
    assertThat(a.size(), is(0));
  }
  @Test public void localVariablesForLoopOneVariable() {
    final MethodDeclaration d = Into.d("  int f() {\n" + //
        "  for (int i = 0; i*j <10; i += j++);  }"); //
    final List<SimpleName> a = new MethodExplorer(d).localVariables();
    assertThat(a.size(), is(1));
  }
  @Test public void localVariablesForLoopTwoVariables() {
    final MethodDeclaration d = Into.d("  int f() {\n" + //
        "  for (int i = 0, j = 2; i*j <10; i += j++);  }"); //
    final List<SimpleName> a = new MethodExplorer(d).localVariables();
    assertThat(a.size(), is(2));
  }
  @Test public void localVariablesExtendedForLoop() {
    final MethodDeclaration d = Into.d("" + //
        "  int sum(final int is[]) {\n" + //
        "    int $ = 0;\n" + //
        "    for (final int i : is)\n" + //
        "      $ += i;\n" + //
        "    return $;\n" + //
        "  } "); //
    assertThat(new MethodExplorer(d).localVariables().size(), is(2));
  }
  @Test public void localVariablesCatchExpression() {
    final MethodDeclaration d = Into.d("" + //
        "  void f() {\n" + //
        "    try {\n" + //
        "      f();\n" + //
        "    } catch (final Exception|RuntimeException e) {\n" + //
        "      f();\n" + //
        "    }\n" + //
        "  }");
    assertThat(new MethodExplorer(d).localVariables().size(), is(1));
  }
  @Test public void localVariablesTryClause() {
    final MethodDeclaration d = Into.d("" + //
        "  void f() {\n" + //
        "    final File f = new File(\"f\");\n" + //
        "    try (final InputStream s = new FileInputStream(f); final InputStreamReader is = new InputStreamReader(s)) {\n" + //
        "      f();\n" + //
        "    } catch (final FileNotFoundException e) {\n" + //
        "      e.printStackTrace();\n" + //
        "    } catch (final IOException e) {\n" + //
        "      e.printStackTrace();\n" + //
        "    } finally {\n" + //
        "      f();\n" + //
        "    }\n" + //
        "  }\n");
    assertThat(new MethodExplorer(d).localVariables().size(), is(5));
  }
}
