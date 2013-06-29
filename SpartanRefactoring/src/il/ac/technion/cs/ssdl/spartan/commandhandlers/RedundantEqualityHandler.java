package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.BaseRefactoring;
import il.ac.technion.cs.ssdl.spartan.refactoring.RedundantEqualityRefactoring;


/**
 * @author Boris van Sosin
 * a BaseSpartanizationHandler configured to return the BaseRefactoring and label
 * relevant for Redundant Equality
 */
public class RedundantEqualityHandler extends BaseSpartanizationHandler {
	private BaseRefactoring refactoring = new RedundantEqualityRefactoring();

	@Override
	protected String getDialogTitle() {
		return "Remove Redundant Equality";
	}

	@Override
	protected BaseRefactoring getRefactoring() {
		return refactoring;
	}
}
