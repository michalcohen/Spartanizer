package il.ac.technion.cs.ssdl.spartan.refactoring;

import static org.junit.Assert.assertEquals;
import il.ac.technion.cs.ssdl.spartan.utils.Utils;

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
    runTestCase(new Ternarize(), "ConvertToTernary1");
  }
  
  @Test public void testConvertToTernary2() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new Ternarize(), "ConvertToTernary2");
  }
  
  @Test public void testConvertToTernary3() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new Ternarize(), "ConvertToTernary3");
  }
  
  @Test public void testConvertToTernary4() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new Ternarize(), "ConvertToTernary4");
  }
  
  @Test public void testConvertToTernary5() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCaseNoChange(new Ternarize(), "ConvertToTernary5");
  }
  
  @Test public void testConvertToTernary6() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new Ternarize(), "ConvertToTernary6");
  }
  
  @Test public void testConvertToTernary7() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCaseNoChange(new Ternarize(), "ConvertToTernary7");
  }
  
  @Test public void testConvertToTernary8() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new Ternarize(), "ConvertToTernary8");
  }
  
  @Test public void testConvertToTernary9() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new Ternarize(), "ConvertToTernary9");
  }
  
  @Test public void testConvertToTernary10() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCaseNoChange(new Ternarize(), "ConvertToTernary10");
  }
  
  @Test public void testConvertToTernary11() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new Ternarize(), "ConvertToTernary11");
  }
  
  @Test public void testConvertToTernary12() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCaseNoChange(new Ternarize(), "ConvertToTernary12");
  }
  
  @Test public void testForwardDeclaration1() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ForwardDeclaration(), "ForwardDeclaration1");
  }
  
  @Test public void testForwardDeclaration2() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ForwardDeclaration(), "ForwardDeclaration2");
  }
  
  @Test public void testForwardDeclaration3() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ForwardDeclaration(), "ForwardDeclaration3");
  }
  
  @Test public void testForwardDeclaration4() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ForwardDeclaration(), "ForwardDeclaration4");
  }
  
  @Test public void testForwardDeclaration5() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ForwardDeclaration(), "ForwardDeclaration5");
  }
  
  @Test public void testForwardDeclaration6() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ForwardDeclaration(), "ForwardDeclaration6");
  }
  
  @Test public void testForwardDeclaration7() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ForwardDeclaration(), "ForwardDeclaration7");
  }
  
  @Test public void testInlineSingleUse1() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new InlineSingleUse(), "InlineSingleUse1");
  }
  
  @Test public void testInlineSingleUse2() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new InlineSingleUse(), "InlineSingleUse2");
  }
  
  @Test public void testInlineSingleUse3() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new InlineSingleUse(), "InlineSingleUse3");
  }
  
  @Test public void testInlineSingleUse4() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new InlineSingleUse(), "InlineSingleUse4");
  }
  
  @Test public void testInlineSingleUse5() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new InlineSingleUse(), "InlineSingleUse5");
  }
  
  @Test public void testInlineSingleUse6() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCaseNoChange(new InlineSingleUse(), "InlineSingleUse6");
  }
  
  @Test public void testInlineSingleUse7() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCaseNoChange(new InlineSingleUse(), "InlineSingleUse7");
  }
  
  @Test public void testInlineSingleUse8() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCaseNoChange(new InlineSingleUse(), "InlineSingleUse8");
  }
  
  @Test public void testRedundantEquality1() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ComparisonWithBoolean(), "RedundantEquality1");
  }
  
  @Test public void testRedundantEquality2() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ComparisonWithBoolean(), "RedundantEquality2");
  }
  
  @Test public void testRedundantEquality3() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ComparisonWithBoolean(), "RedundantEquality3");
  }
  
  @Test public void testShortestBranchFirst1() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ShortestBranch(), "ShortestBranchFirst1");
  }
  
  @Test public void testShortestBranchFirst2() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ShortestBranch(), "ShortestBranchFirst2");
  }
  
  @Test public void testShortestBranchFirst3() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ShortestBranch(), "ShortestBranchFirst3");
  }
  
  @Test public void testShortestBranchFirst4() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ShortestBranch(), "ShortestBranchFirst4");
  }
  
  @Test public void testShortestBranchFirst5() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ShortestBranch(), "ShortestBranchFirst5");
  }
  
  @Test public void testShortestBranchFirst6() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ShortestBranch(), "ShortestBranchFirst6");
  }
  
  @Test public void testShortestBranchFirst7() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ShortestBranch(), "ShortestBranchFirst7");
  }
  
  @Test public void testShortestBranchFirst8() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ShortestBranch(), "ShortestBranchFirst8");
  }
  
  @Test public void testShortestBranchFirst9() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new ShortestBranch(), "ShortestBranchFirst9");
  }
  
  @Test public void testChangeReturnVariableToDollar1() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new RenameReturnToDollar(), "ChangeReturnVarToDollar1");
  }
  
  @Test public void testChangeReturnVariableToDollar2() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new RenameReturnToDollar(), "ChangeReturnVarToDollar2");
  }
  
  @Test public void testChangeReturnVariableToDollar3() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new RenameReturnToDollar(), "ChangeReturnVarToDollar3");
  }
  
  @Test public void testChangeReturnVariableToDollar4() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCaseNoChange(new RenameReturnToDollar(), "ChangeReturnVarToDollar4");
  }
  
  @Test public void testChangeReturnVariableToDollar5() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new RenameReturnToDollar(), "ChangeReturnVarToDollar5");
  }
  
  @Test public void testChangeReturnVariableToDollar6() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new RenameReturnToDollar(), "ChangeReturnVarToDollar6");
  }
  
  @Test public void testChangeReturnVariableToDollar7() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new RenameReturnToDollar(), "ChangeReturnVarToDollar7");
  }
  
  @Test public void testChangeReturnVariableToDollar8() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCase(new RenameReturnToDollar(), "ChangeReturnVarToDollar8");
  }
  
  @Test public void testChangeReturnVariableToDollar9() throws IOException, IllegalArgumentException, MalformedTreeException,
      BadLocationException {
    runTestCaseNoChange(new RenameReturnToDollar(), "ChangeReturnVarToDollar9");
  }
  
  private static void runTestCase(final Spartanization s, final String testCaseName) throws MalformedTreeException,
      IllegalArgumentException, BadLocationException, IOException {
    final String text = readFile("TestResources" + File.separator + testCaseName + ".before");
    final ASTParser parser = Utils.makeParser(text);
    final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
    assertEquals(1, s.findOpportunities(cu).size());
    final Document doc = new Document(text);
    s.createRewrite(cu, null).rewriteAST(doc, null).apply(doc);
    assertEquals(readFile("TestResources" + File.separator + testCaseName + ".after"), doc.get());
  }
  
  private static void runTestCaseNoChange(final Spartanization s, final String testCaseName) throws MalformedTreeException,
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
