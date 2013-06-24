package il.ac.technion.cs.ssdl.spartan.refactoring;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class RefactoringPlugin extends AbstractUIPlugin {
  private static RefactoringPlugin plugin;
  
  public RefactoringPlugin() {
    plugin = this;
    SpartanizationFactory.initialize();
  }
  
  @Override public void start(final BundleContext context) throws Exception {
    super.start(context);
  }
  
  @Override public void stop(final BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }
  
  public static RefactoringPlugin getDefault() {
    return plugin;
  }
  
  public static void log(final Throwable throwable) {
    getDefault().getLog().log(
        new Status(IStatus.ERROR, "il.ac.technion.cs.ssdl.spartan.refactoring", 0, throwable.getMessage(), throwable));
  }
}