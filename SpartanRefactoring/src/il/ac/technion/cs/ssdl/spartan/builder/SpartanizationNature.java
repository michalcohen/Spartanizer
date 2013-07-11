package il.ac.technion.cs.ssdl.spartan.builder;

import java.util.Arrays;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Eclipse (auto-generated)
 * @since 2013/07/01
 */
public class SpartanizationNature implements IProjectNature {
  /**
   * ID of this project nature
   */
  public static final String NATURE_ID = "il.ac.technion.cs.ssdl.spartan.SpartanizationNature";
  private IProject project;
  
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.resources.IProjectNature#configure()
   */
  @Override public void configure() throws CoreException {
    final IProjectDescription d = project.getDescription();
    final ICommand[] cs = d.getBuildSpec();
    for (final ICommand c : cs)
      if (c.getBuilderName().equals(SpartaBuilder.BUILDER_ID))
        return;
    d.setBuildSpec(make(d, cs));
    project.setDescription(d, null);
  }
  
  private static ICommand[] make(final IProjectDescription d, final ICommand[] cs) {
    final ICommand[] $ = Arrays.copyOf(cs, cs.length + 1);
    ($[$.length - 1] = d.newCommand()).setBuilderName(SpartaBuilder.BUILDER_ID);
    return $;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.resources.IProjectNature#deconfigure()
   */
  @Override public void deconfigure() throws CoreException {
    final IProjectDescription description = getProject().getDescription();
    final ICommand[] cs = description.getBuildSpec();
    for (int i = 0; i < cs.length; ++i)
      if (cs[i].getBuilderName().equals(SpartaBuilder.BUILDER_ID)) {
        description.setBuildSpec(make(cs, i));
        project.setDescription(description, null);
        return;
      }
  }
  
  private static ICommand[] make(final ICommand[] cs, final int i) {
    final ICommand[] $ = new ICommand[cs.length - 1];
    System.arraycopy(cs, 0, $, 0, i);
    System.arraycopy(cs, i + 1, $, i, cs.length - i - 1);
    return $;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.resources.IProjectNature#getProject()
   */
  @Override public IProject getProject() {
    return project;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources
   * .IProject)
   */
  @Override public void setProject(final IProject project) {
    this.project = project;
  }
}
