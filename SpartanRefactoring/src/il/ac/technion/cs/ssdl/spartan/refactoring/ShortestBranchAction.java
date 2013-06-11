package il.ac.technion.cs.ssdl.spartan.refactoring;

public class ShortestBranchAction extends BaseAction {
  @Override protected ShortestBranchRefactoring getRefactoring() {
    return new ShortestBranchRefactoring();
  }
  
  @Override protected String getDialogTitle() {
    return "Shortest Conditional Branch First";
  }
}
