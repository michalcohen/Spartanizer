package il.ac.technion.cs.ssdl.spartan.refactoring;

import static org.junit.Assert.fail;
import il.ac.technion.cs.ssdl.spartan.utils.Utils;

import java.io.File;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;

/**
 * @author Yossi Gil
 * @since 2014/05/24
 */
abstract class AbstractParametrizedTest extends TestSuite {



	static CompilationUnit makeAST(final File f) {
		return (CompilationUnit) Utils.makeParser(readFile(f)).createAST(null);
	}

	static Document rewrite(final Spartanization s, final CompilationUnit cu, final Document d) {
		try {
			s.createRewrite(cu, null).rewriteAST(d, null).apply(d);
			return d;
		} catch (final MalformedTreeException e) {
			fail(e.getMessage());
		} catch (final IllegalArgumentException e) {
			fail(e.getMessage());
		} catch (final BadLocationException e) {
			fail(e.getMessage());
		}
		return null;
	}
}


