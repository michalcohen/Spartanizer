package il.ac.technion.cs.ssdl.spartan.refactoring;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

public class ShortestBranchContribution extends RefactoringContribution {
  @SuppressWarnings("rawtypes") @Override public RefactoringDescriptor createDescriptor(String id, String project,
      String description, String comment, Map arguments, int flags) {
    return new ShortestBranchDescriptor(project, description, comment, arguments);
  }
  
  @SuppressWarnings("rawtypes") @Override public Map retrieveArgumentMap(RefactoringDescriptor descriptor) {
    if (descriptor instanceof ShortestBranchDescriptor)
      return ((ShortestBranchDescriptor) descriptor).getArguments();
    return super.retrieveArgumentMap(descriptor);
  }
}