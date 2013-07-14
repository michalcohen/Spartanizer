package il.ac.technion.cs.ssdl.spartan.refactoring;

import static org.junit.Assert.assertEquals;
import il.ac.technion.cs.ssdl.spartan.builder.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.junit.Test;

/**
 * @author Boris van Sosin
 * 
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class SpartanizationTests {
  @Test public void testConvertToTernary1() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ConvertToTernaryRefactoring(), "ConvertToTernary1");
  }
  
  @Test public void testConvertToTernary2() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ConvertToTernaryRefactoring(), "ConvertToTernary2");
  }
  
  @Test public void testConvertToTernary3() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ConvertToTernaryRefactoring(), "ConvertToTernary3");
  }
  
  @Test public void testConvertToTernary4() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ConvertToTernaryRefactoring(), "ConvertToTernary4");
  }
  
  @Test public void testConvertToTernary5() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCaseNoChange(new ConvertToTernaryRefactoring(), "ConvertToTernary5");
  }
  
  @Test public void testConvertToTernary6() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ConvertToTernaryRefactoring(), "ConvertToTernary6");
  }
  
  @Test public void testConvertToTernary7() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCaseNoChange(new ConvertToTernaryRefactoring(), "ConvertToTernary7");
  }
  
  @Test public void testConvertToTernary8() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ConvertToTernaryRefactoring(), "ConvertToTernary8");
  }
  
  @Test public void testConvertToTernary9() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ConvertToTernaryRefactoring(), "ConvertToTernary9");
  }
  
  @Test public void testConvertToTernary10() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCaseNoChange(new ConvertToTernaryRefactoring(), "ConvertToTernary10");
  }
  
  @Test public void testConvertToTernary11() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ConvertToTernaryRefactoring(), "ConvertToTernary11");
  }
  
  @Test public void testConvertToTernary12() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCaseNoChange(new ConvertToTernaryRefactoring(), "ConvertToTernary12");
  }
  
  @Test public void testForwardDeclaration1() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ForwardDeclarationRefactoring(), "ForwardDeclaration1");
  }
  
  @Test public void testForwardDeclaration2() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ForwardDeclarationRefactoring(), "ForwardDeclaration2");
  }
  
  @Test public void testForwardDeclaration3() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ForwardDeclarationRefactoring(), "ForwardDeclaration3");
  }
  
  @Test public void testForwardDeclaration4() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ForwardDeclarationRefactoring(), "ForwardDeclaration4");
  }
  
  @Test public void testForwardDeclaration5() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ForwardDeclarationRefactoring(), "ForwardDeclaration5");
  }
  
  @Test public void testForwardDeclaration6() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ForwardDeclarationRefactoring(), "ForwardDeclaration6");
  }
  
  @Test public void testForwardDeclaration7() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ForwardDeclarationRefactoring(), "ForwardDeclaration7");
  }
  
  @Test public void testInlineSingleUse1() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new InlineSingleUseRefactoring(), "InlineSingleUse1");
  }
  
  @Test public void testInlineSingleUse2() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new InlineSingleUseRefactoring(), "InlineSingleUse2");
  }
  
  @Test public void testInlineSingleUse3() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new InlineSingleUseRefactoring(), "InlineSingleUse3");
  }
  
  @Test public void testInlineSingleUse4() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new InlineSingleUseRefactoring(), "InlineSingleUse4");
  }
  
  @Test public void testInlineSingleUse5() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new InlineSingleUseRefactoring(), "InlineSingleUse5");
  }
  
  @Test public void testInlineSingleUse6() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCaseNoChange(new InlineSingleUseRefactoring(), "InlineSingleUse6");
  }
  
  @Test public void testInlineSingleUse7() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCaseNoChange(new InlineSingleUseRefactoring(), "InlineSingleUse7");
  }
  
  @Test public void testInlineSingleUse8() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCaseNoChange(new InlineSingleUseRefactoring(), "InlineSingleUse8");
  }
  
  @Test public void testRedundantEquality1() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new RedundantEqualityRefactoring(), "RedundantEquality1");
  }
  
  @Test public void testRedundantEquality2() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new RedundantEqualityRefactoring(), "RedundantEquality2");
  }
  
  @Test public void testRedundantEquality3() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new RedundantEqualityRefactoring(), "RedundantEquality3");
  }
  
  @Test public void testShortestBranchFirst1() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ShortestBranchRefactoring(), "ShortestBranchFirst1");
  }
  
  @Test public void testShortestBranchFirst2() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ShortestBranchRefactoring(), "ShortestBranchFirst2");
  }
  
  @Test public void testShortestBranchFirst3() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ShortestBranchRefactoring(), "ShortestBranchFirst3");
  }
  
  @Test public void testShortestBranchFirst4() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ShortestBranchRefactoring(), "ShortestBranchFirst4");
  }
  
  @Test public void testShortestBranchFirst5() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ShortestBranchRefactoring(), "ShortestBranchFirst5");
  }
  
  @Test public void testShortestBranchFirst6() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ShortestBranchRefactoring(), "ShortestBranchFirst6");
  }
  
  @Test public void testShortestBranchFirst7() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ShortestBranchRefactoring(), "ShortestBranchFirst7");
  }
  
  @Test public void testShortestBranchFirst8() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ShortestBranchRefactoring(), "ShortestBranchFirst8");
  }
  
  @Test public void testShortestBranchFirst9() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ShortestBranchRefactoring(), "ShortestBranchFirst9");
  }
  
  @Test public void testChangeReturnVariableToDollar1() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ChangeReturnToDollarRefactoring(), "ChangeReturnVarToDollar1");
  }
  
  @Test public void testChangeReturnVariableToDollar2() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ChangeReturnToDollarRefactoring(), "ChangeReturnVarToDollar2");
  }
  
  @Test public void testChangeReturnVariableToDollar3() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ChangeReturnToDollarRefactoring(), "ChangeReturnVarToDollar3");
  }
  
  @Test public void testChangeReturnVariableToDollar4() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCaseNoChange(new ChangeReturnToDollarRefactoring(), "ChangeReturnVarToDollar4");
  }
  
  @Test public void testChangeReturnVariableToDollar5() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ChangeReturnToDollarRefactoring(), "ChangeReturnVarToDollar5");
  }
  
  @Test public void testChangeReturnVariableToDollar6() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ChangeReturnToDollarRefactoring(), "ChangeReturnVarToDollar6");
  }
  
  @Test public void testChangeReturnVariableToDollar7() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ChangeReturnToDollarRefactoring(), "ChangeReturnVarToDollar7");
  }
  
  @Test public void testChangeReturnVariableToDollar8() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ChangeReturnToDollarRefactoring(), "ChangeReturnVarToDollar8");
  }
  
  @Test public void testChangeReturnVariableToDollar9() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCaseNoChange(new ChangeReturnToDollarRefactoring(), "ChangeReturnVarToDollar9");
  }
  
  private static void runTestCase(final SpartanRefactoring s, final String testCaseName) throws MalformedTreeException,
      IllegalArgumentException, BadLocationException, IOException {
    final String text = readFile("TestResources" + File.separator + testCaseName + ".before");
    final ASTParser parser = Utils.makeParser(text);
    final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
    assertEquals(1, s.findOpportunities(cu).size());
    final Document doc = new Document(text);
    s.createRewrite(cu, null).rewriteAST(doc, null).apply(doc);
    assertEquals(readFile("TestResources" + File.separator + testCaseName + ".after"), doc.get());
  }
  
  private static void runTestCaseNoChange(final SpartanRefactoring s, final String testCaseName) throws MalformedTreeException,
      IllegalArgumentException, BadLocationException, IOException {
    final String text = readFile("TestResources" + File.separator + testCaseName + ".before");
    final ASTParser p = Utils.makeParser(text);
    final CompilationUnit cu = (CompilationUnit) p.createAST(null);
    assertEquals(0, s.findOpportunities(cu).size());
    final Document d = new Document(text);
    s.createRewrite(cu, null).rewriteAST(d, null).apply(d);
    assertEquals(readFile("TestResources" + File.separator + testCaseName + ".before"), d.get());
  }
  
  private static String readFile(final String filename) throws IOException {
    final BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
    String line;
    final StringBuilder $ = new StringBuilder();
    while ((line = r.readLine()) != null)
      $.append(line).append(System.lineSeparator());
    r.close();
    return $.toString();
  }
}
