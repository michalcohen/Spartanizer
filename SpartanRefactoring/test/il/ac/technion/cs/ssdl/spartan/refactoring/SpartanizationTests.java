package il.ac.technion.cs.ssdl.spartan.refactoring;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.junit.Test;

public class SpartanizationTests {
	
	@Test
	public void testConvertToTernary1() throws IOException, JavaModelException, IllegalArgumentException, MalformedTreeException, BadLocationException {
		runTestCase(new ConvertToTernaryRefactoring(),"ConvertToTernary1");
	}

	@Test
	public void testConvertToTernary2() throws IOException, JavaModelException, IllegalArgumentException, MalformedTreeException, BadLocationException {
		runTestCase(new ConvertToTernaryRefactoring(),"ConvertToTernary2");
	}

	@Test
	public void testConvertToTernary3() throws IOException, JavaModelException, IllegalArgumentException, MalformedTreeException, BadLocationException {
		runTestCase(new ConvertToTernaryRefactoring(),"ConvertToTernary3");
	}

	@Test
	public void testConvertToTernary4() throws IOException, JavaModelException, IllegalArgumentException, MalformedTreeException, BadLocationException {
		runTestCase(new ConvertToTernaryRefactoring(),"ConvertToTernary4");
	}

	@Test
	public void testConvertToTernary5() throws IOException, JavaModelException, IllegalArgumentException, MalformedTreeException, BadLocationException {
		runTestCaseNoChange(new ConvertToTernaryRefactoring(), "ConvertToTernary5");
	}
	
	@Test
	public void testForwardDeclaration1() throws IOException, JavaModelException, IllegalArgumentException, MalformedTreeException, BadLocationException {
		runTestCase(new ForwardDeclarationRefactoring(), "ForwardDeclaration1");
	}

	@Test
	public void testForwardDeclaration2() throws IOException, JavaModelException, IllegalArgumentException, MalformedTreeException, BadLocationException {
		runTestCase(new ForwardDeclarationRefactoring(), "ForwardDeclaration2");
	}
	
	@Test
	public void testForwardDeclaration3() throws IOException, JavaModelException, IllegalArgumentException, MalformedTreeException, BadLocationException {
		runTestCase(new ForwardDeclarationRefactoring(), "ForwardDeclaration3");
	}
	
	@Test
	public void testForwardDeclaration4() throws IOException, JavaModelException, IllegalArgumentException, MalformedTreeException, BadLocationException {
		runTestCase(new ForwardDeclarationRefactoring(), "ForwardDeclaration4");
	}
	
	@Test
	public void testForwardDeclaration5() throws IOException, JavaModelException, IllegalArgumentException, MalformedTreeException, BadLocationException {
		runTestCase(new ForwardDeclarationRefactoring(), "ForwardDeclaration5");
	}

	@Test
	public void testForwardDeclaration6() throws IOException, JavaModelException, IllegalArgumentException, MalformedTreeException, BadLocationException {
		runTestCase(new ForwardDeclarationRefactoring(), "ForwardDeclaration6");
	}
	
	private void runTestCase(final BaseRefactoring s, final String testCaseName) throws MalformedTreeException, IllegalArgumentException, BadLocationException, IOException {
		final ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		final String file = readFile("TestResources" + File.separator + testCaseName + ".before");
		parser.setSource(file.toCharArray());
		parser.setResolveBindings(false);
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		assertEquals(1, s.checkForSpartanization(cu).size());
		final ASTRewrite rw = s.createRewrite(cu, null);
		final Document doc = new Document(file);
		rw.rewriteAST(doc, null).apply(doc);
		assertEquals(readFile("TestResources" + File.separator + testCaseName + ".after"), doc.get());		
	}
	
	private void runTestCaseNoChange(final BaseRefactoring s, final String testCaseName) throws MalformedTreeException, IllegalArgumentException, BadLocationException, IOException {
		final ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		final String file = readFile("TestResources" + File.separator + testCaseName + ".before");
		parser.setSource(file.toCharArray());
		parser.setResolveBindings(false);
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		assertEquals(0, s.checkForSpartanization(cu).size());
		final ASTRewrite rw = s.createRewrite(cu, null);
		final Document doc = new Document(file);
		rw.rewriteAST(doc, null).apply(doc);
		assertEquals(readFile("TestResources" + File.separator + testCaseName + ".before"), doc.get());		
	}

	private static String readFile(final String filename) throws IOException {
		final FileInputStream is = new FileInputStream(filename);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		final StringBuilder res = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			res.append(line);
			res.append(System.lineSeparator());
		}
		reader.close();
		return res.toString();
	}
}
