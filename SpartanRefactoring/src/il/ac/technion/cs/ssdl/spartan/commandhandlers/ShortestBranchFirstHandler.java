package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.BaseRefactoring;
import il.ac.technion.cs.ssdl.spartan.refactoring.ShortestBranchRefactoring;


/**
 * @author Boris van Sosin
 * a BaseSpartanizationHandler configured to return the BaseRefactoring and label
 * relevant for Shortest Branch First
 */
public class ShortestBranchFirstHandler extends BaseSpartanizationHandler {
	private BaseRefactoring refactoring = new ShortestBranchRefactoring();

	@Override
	protected String getDialogTitle() {
		return "Make Shortest Side of Branch First";
	}

	@Override
	protected BaseRefactoring getRefactoring() {
		return refactoring;
	}
}
