package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.BaseRefactoring;
import il.ac.technion.cs.ssdl.spartan.refactoring.ConvertToTernaryRefactoring;


/**
 * @author Boris van Sosin
 * a BaseSpartanizationHandler configured to return the BaseRefactoring and label
 * relevant for Convert to Ternary
 */
public class ConvertToTernaryHandler extends BaseSpartanizationHandler {
	private BaseRefactoring refactoring = new ConvertToTernaryRefactoring();

	@Override
	protected String getDialogTitle() {
		return "Convert Conditional to Ternary";
	}

	@Override
	protected BaseRefactoring getRefactoring() {
		return refactoring;
	}
}
