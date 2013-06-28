package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.BaseRefactoring;
import il.ac.technion.cs.ssdl.spartan.refactoring.ChangeReturnToDollarRefactoring;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ui.handlers.HandlerUtil;

public class ChangeReturnToDollarHandler extends BaseSpartanizationHandler {
	private BaseRefactoring refactoring = new ChangeReturnToDollarRefactoring();

	@Override
	protected String getDialogTitle() {
		return "Change Return Variable to $";
	}

	@Override
	protected BaseRefactoring getRefactoring() {
		return refactoring;
	}
}
