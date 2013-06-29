package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.BaseRefactoring;
import il.ac.technion.cs.ssdl.spartan.refactoring.ForwardDeclarationRefactoring;


/**
 * @author Boris van Sosin
 * a BaseSpartanizationHandler configured to return the BaseRefactoring and label
 * relevant for Forward Declaration
 */
public class ForwardDeclarationHandler extends BaseSpartanizationHandler {
	private BaseRefactoring refactoring = new ForwardDeclarationRefactoring();

	@Override
	protected String getDialogTitle() {
		return "Forward Declaration of Variable";
	}

	@Override
	protected BaseRefactoring getRefactoring() {
		return refactoring;
	}
}
