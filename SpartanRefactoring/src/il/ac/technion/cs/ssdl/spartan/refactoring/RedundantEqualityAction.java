package il.ac.technion.cs.ssdl.spartan.refactoring;

public class RedundantEqualityAction extends BaseAction {
	
	@Override
	protected RedundantEqualityRefactoring getRefactoring() {
		return new RedundantEqualityRefactoring();
	}

	@Override
	protected String getDialogTitle() {
		return "Eliminate Redundant Equalities";
	}

	

}
