package il.ac.technion.cs.ssdl.spartan.refactoring;

public class ConvertToTernaryAction extends BaseAction {
	
	@Override
	protected ConvertToTernaryRefactoring getRefactoring() {
		return new ConvertToTernaryRefactoring();
	}

	@Override
	protected String getDialogTitle() {
		return "Convert Conditional Into a Trenary";
	}

}
