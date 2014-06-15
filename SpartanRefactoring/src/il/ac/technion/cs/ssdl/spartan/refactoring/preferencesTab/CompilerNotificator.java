package il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.ReconcileContext;


/**
 * This class notifies the Java compiler of a warning or an error in the build process
 * according to the preferences set in the spartanization preferences page for each
 * transformation suggestion
 * 
 * @author Tomer Zeltzer (original)
 * @since 10/06/2014
 */
public class CompilerNotificator extends CompilationParticipant {
	@Override public int aboutToBuild(@SuppressWarnings("unused") final IJavaProject project) {
		return 0;
	}
	@Override public boolean isActive(@SuppressWarnings("unused") final IJavaProject project) {
		return true;
	}
	@Override public void reconcile(@SuppressWarnings("unused") final ReconcileContext context) {
		// context.putProblems("test", problems);
	}
}