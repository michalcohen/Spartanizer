package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.BaseRefactoring;
import il.ac.technion.cs.ssdl.spartan.refactoring.InlineSingleUseRefactoring;


/**
 * @author Boris van Sosin
 * a BaseSpartanizationHandler configured to return the BaseRefactoring and label
 * relevant for Inline Single Use
 */
public class InlineSinleUseHandler extends BaseSpartanizationHandler {
	private BaseRefactoring refactoring = new InlineSingleUseRefactoring();

	@Override
	protected String getDialogTitle() {
		return "Inline Single Use of Variable";
	}

	@Override
	protected BaseRefactoring getRefactoring() {
		return refactoring;
	}
}
