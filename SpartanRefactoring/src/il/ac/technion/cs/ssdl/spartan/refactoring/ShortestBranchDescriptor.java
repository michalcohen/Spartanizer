package il.ac.technion.cs.ssdl.spartan.refactoring;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class ShortestBranchDescriptor extends RefactoringDescriptor {
  public static final String REFACTORING_ID = "il.ac.technion.cs.ssdl.spartan.refactoring.shortest.branch"; // TODO:
                                                                                                            // What
                                                                                                            // is
                                                                                                            // this??
  @SuppressWarnings("rawtypes") private final Map fArguments;
  
  @SuppressWarnings("rawtypes") public ShortestBranchDescriptor(String project, String description, String comment, Map arguments) {
    super(REFACTORING_ID, project, description, comment, RefactoringDescriptor.NONE);
    fArguments = arguments;
  }
  
  @Override public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
    ShortestBranchRefactoring refactoring = new ShortestBranchRefactoring();
    status.merge(refactoring.initialize(fArguments));
    return refactoring;
  }
  
  @SuppressWarnings("rawtypes") public Map getArguments() {
    return fArguments;
  }
}