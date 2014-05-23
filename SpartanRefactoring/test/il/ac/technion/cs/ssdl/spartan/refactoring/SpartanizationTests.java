package il.ac.technion.cs.ssdl.spartan.refactoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import il.ac.technion.cs.ssdl.spartan.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.junit.Test;

/**
 * @author Boris van Sosin (original)
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code>: test cases
 *         13-17 (2014/05/19)
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code>: test cases
 *         13-17 (2014/05/19)
 * @since 2014/05/19
 */
@SuppressWarnings({ "javadoc", "static-method" }) public class SpartanizationTests {
	@Test public void convertToTernary1() {
		runcase(new Ternarize(), "ConvertToTernary1");
	}

	@Test public void convertToTernary2() {
		runcase(new Ternarize(), "ConvertToTernary2");
	}

	@Test public void convertToTernary3() {
		runcase(new Ternarize(), "ConvertToTernary3");
	}

	@Test public void convertToTernary4() {
		runcase(new Ternarize(), "ConvertToTernary4");
	}

	@Test public void convertToTernary5() {
		runcaseNoChange(new Ternarize(), "ConvertToTernary5");
	}

	@Test public void convertToTernary6() {
		runcase(new Ternarize(), "ConvertToTernary6");
	}

	@Test public void convertToTernary7() {
		runcaseNoChange(new Ternarize(), "ConvertToTernary7");
	}

	@Test public void convertToTernary8() {
		runcase(new Ternarize(), "ConvertToTernary8");
	}

	@Test public void convertToTernary9() {
		runcase(new Ternarize(), "ConvertToTernary9");
	}

	@Test public void convertToTernary10() {
		runcaseNoChange(new Ternarize(), "ConvertToTernary10");
	}

	@Test public void convertToTernary11() {
		runcase(new Ternarize(), "ConvertToTernary11");
	}

	@Test public void convertToTernary12() {
		runcaseNoChange(new Ternarize(), "ConvertToTernary12");
	}

	@Test public void convertToTernary13() {
		runcase(new Ternarize(), "ConvertToTernary13");
	}

	@Test public void convertToTernary14() {
		runcaseNoChange(new Ternarize(), "ConvertToTernary14");
	}

	@Test public void convertToTernary15() {
		runcaseNoChange(new Ternarize(), "ConvertToTernary15");
	}

	@Test public void convertToTernary16() {
		runcase(new Ternarize(), "ConvertToTernary16");
	}

	@Test public void convertToTernary17() {
		runcase(new Ternarize(), "ConvertToTernary17");
	}

	@Test public void forwardDeclaration1() {
		runcase(new ForwardDeclaration(), "ForwardDeclaration1");
	}

	@Test public void forwardDeclaration2() {
		runcase(new ForwardDeclaration(), "ForwardDeclaration2");
	}

	@Test public void forwardDeclaration3() {
		runcase(new ForwardDeclaration(), "ForwardDeclaration3");
	}

	@Test public void forwardDeclaration4()  {
		runcase(new ForwardDeclaration(), "ForwardDeclaration4");
	}

	@Test public void forwardDeclaration5()  {
		runcase(new ForwardDeclaration(), "ForwardDeclaration5");
	}

	@Test public void forwardDeclaration6()  {
		runcase(new ForwardDeclaration(), "ForwardDeclaration6");
	}

	@Test public void forwardDeclaration7()  {
		runcase(new ForwardDeclaration(), "ForwardDeclaration7");
	}

	@Test public void inlineSingleUse1() {
		runcase(new InlineSingleUse(), "InlineSingleUse1");
	}

	@Test public void inlineSingleUse2() {
		runcase(new InlineSingleUse(), "InlineSingleUse2");
	}

	@Test public void inlineSingleUse3() {
		runcase(new InlineSingleUse(), "InlineSingleUse3");
	}

	@Test public void inlineSingleUse4() {
		runcase(new InlineSingleUse(), "InlineSingleUse4");
	}

	@Test public void inlineSingleUse5() {
		runcase(new InlineSingleUse(), "InlineSingleUse5");
	}

	@Test public void inlineSingleUse6() {
		runcaseNoChange(new InlineSingleUse(), "InlineSingleUse6");
	}

	@Test public void inlineSingleUse7() {
		runcaseNoChange(new InlineSingleUse(), "InlineSingleUse7");
	}

	@Test public void inlineSingleUse8() {
		runcaseNoChange(new InlineSingleUse(), "InlineSingleUse8");
	}

	@Test public void redundantEquality1() {
		runcase(new ComparisonWithBoolean(), "RedundantEquality1");
	}

	@Test public void redundantEquality2() {
		runcase(new ComparisonWithBoolean(), "RedundantEquality2");
	}

	@Test public void redundantEquality3() {
		runcase(new ComparisonWithBoolean(), "RedundantEquality3");
	}

	@Test public void shortestBranchFirst1() {
		runcase(new ShortestBranch(), "ShortestBranchFirst1");
	}

	@Test public void shortestBranchFirst2()  {
		runcase(new ShortestBranch(), "ShortestBranchFirst2");
	}

	@Test public void shortestBranchFirst3()  {
		runcase(new ShortestBranch(), "ShortestBranchFirst3");
	}

	@Test public void shortestBranchFirst4()  {
		runcase(new ShortestBranch(), "ShortestBranchFirst4");
	}

	@Test public void shortestBranchFirst5()  {
		runcase(new ShortestBranch(), "ShortestBranchFirst5");
	}

	@Test public void shortestBranchFirst6()  {
		runcase(new ShortestBranch(), "ShortestBranchFirst6");
	}

	@Test public void shortestBranchFirst7()  {
		runcase(new ShortestBranch(), "ShortestBranchFirst7");
	}

	@Test public void shortestBranchFirst8()  {
		runcase(new ShortestBranch(), "ShortestBranchFirst8");
	}

	@Test public void shortestBranchFirst9()  {
		runcase(new ShortestBranch(), "ShortestBranchFirst9");
	}

	@Test public void changeReturnVariableToDollar1()  {
		runcase(new RenameReturnToDollar(), "ChangeReturnVarToDollar1");
	}

	@Test public void changeReturnVariableToDollar2()  {
		runcase(new RenameReturnToDollar(), "ChangeReturnVarToDollar2");
	}

	@Test public void changeReturnVariableToDollar3()  {
		runcase(new RenameReturnToDollar(), "ChangeReturnVarToDollar3");
	}

	@Test public void changeReturnVariableToDollar4()  {
		runcaseNoChange(new RenameReturnToDollar(), "ChangeReturnVarToDollar4");
	}

	@Test public void changeReturnVariableToDollar5() {
		runcase(new RenameReturnToDollar(), "ChangeReturnVarToDollar5");
	}

	@Test public void changeReturnVariableToDollar6() {
		runcase(new RenameReturnToDollar(), "ChangeReturnVarToDollar6");
	}

	@Test public void changeReturnVariableToDollar7() {
		runcase(new RenameReturnToDollar(), "ChangeReturnVarToDollar7");
	}

	@Test public void changeReturnVariableToDollar8()  {
		runcase(new RenameReturnToDollar(), "ChangeReturnVarToDollar8");
	}

	@Test public void changeReturnVariableToDollar9() {
		runcaseNoChange(new RenameReturnToDollar(), "ChangeReturnVarToDollar9");
	}

	private static void runcase(final Spartanization s, final String caseName) {
		final String text = readBefore(caseName);
		final CompilationUnit cu = makeParser(text);
		assertEquals(1, s.findOpportunities(cu).size());
		final Document d = new Document(text);
		createRewrite(s, cu, d);
		assertEquals(readFile("resources" + File.separator + caseName + ".after"), d.get());
	}

	private static CompilationUnit makeParser(final String text) {
		return (CompilationUnit) Utils.makeParser(text).createAST(null);
	}

	private static String readBefore(final String caseName) {
		return readFile("resources" + File.separator + caseName + ".before");
	}

	private static void runcaseNoChange(final Spartanization s, final String caseName) {
		final String text = readBefore(caseName);
		final CompilationUnit cu = makeParser(text);
		assertEquals(0, s.findOpportunities(cu).size());
		final Document d = new Document(text);
		createRewrite(s, cu, d);
		assertEquals(readBefore(caseName), d.get());
	}

	private static void createRewrite(final Spartanization s, final CompilationUnit cu, final Document d) {
		try {
			s.createRewrite(cu, null).rewriteAST(d, null).apply(d);
		} catch (MalformedTreeException e) {
			e.printStackTrace();
			fail();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail();
		} catch (BadLocationException e) {
			e.printStackTrace();
			fail();
		}
	}

	private static String readFile(final String filename) {
		try {
			final BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			String line;
			final StringBuilder $ = new StringBuilder();
			while ((line = r.readLine()) != null)
				$.append(line).append(System.lineSeparator());
			r.close();
			return $.toString();
		} catch (IOException e) {
			fail();
			return null;
		}
	}

}
