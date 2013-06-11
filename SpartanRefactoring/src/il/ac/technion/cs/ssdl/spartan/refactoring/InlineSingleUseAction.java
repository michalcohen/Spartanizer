package il.ac.technion.cs.ssdl.spartan.refactoring;

public class InlineSingleUseAction extends BaseAction {
  @Override protected String getDialogTitle() {
    return "Inline single use of variable";
  }
  
  @Override protected BaseRefactoring getRefactoring() {
    return new InlineSingleUseRefactoring();
  }
}
