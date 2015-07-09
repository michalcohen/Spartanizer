import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.cleanup.CleanUpContext;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class CopyrightUpdaterCleanUp implements ICleanUp {
  private CleanUpOptions fOptions;
  private RefactoringStatus fStatus;

  public CopyrightUpdaterCleanUp() {
  }

  @Override public void setOptions(final CleanUpOptions options) {
    Assert.isLegal(options != null);
    Assert.isTrue(fOptions == null);
    fOptions = options;
  }

  @Override public String[] getStepDescriptions() {
    if (fOptions.isEnabled("cleanup.update_copyrights")) //$NON-NLS-1$
      return new String[] { "Update Copyrights" };//$NON-NLS-1$
    return null;
  }

  @Override public CleanUpRequirements getRequirements() {
    final boolean changedRegionsRequired = false;
    final Map compilerOptions = null;
    final boolean isUpdateCopyrights = fOptions.isEnabled("cleanup.update_copyrights");//$NON-NLS-1$
    return new CleanUpRequirements(isUpdateCopyrights, isUpdateCopyrights, changedRegionsRequired, compilerOptions);
  }

  @Override public RefactoringStatus checkPreConditions(final IJavaProject p, final ICompilationUnit[] us,
      final IProgressMonitor m) {
    if (fOptions.isEnabled("cleanup.update_copyrights"))
      fStatus = new RefactoringStatus();
    return new RefactoringStatus();
  }

  @Override public ICleanUpFix createFix(final CleanUpContext c) throws CoreException {
    final CompilationUnit u = c.getAST();
    if (u == null)
      return null;
    return CopyrightsFix.createCleanUp(u, fOptions.isEnabled("cleanup.update_copyrights"));//$NON-NLS-1$
  }

  @Override public RefactoringStatus checkPostConditions(final IProgressMonitor m) {
    try {
      if (fStatus == null || fStatus.isOK())
        return new RefactoringStatus();
      return fStatus;
    } finally {
      fStatus = null;
    }
  }
}