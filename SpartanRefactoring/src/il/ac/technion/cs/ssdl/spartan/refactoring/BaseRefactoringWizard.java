package il.ac.technion.cs.ssdl.spartan.refactoring;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class BaseRefactoringWizard extends RefactoringWizard {
  public BaseRefactoringWizard(Refactoring refactoring) {
    super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
  }
  
  @Override protected void addUserInputPages() {
    // None of our refactorings require dialog pages, therefore we use this
    // class for all the actions.
  }
}