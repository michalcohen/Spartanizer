package il.ac.technion.cs.ssdl.spartan.builder;

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
  public static final String NATURE_ID = "il.ac.technion.cs.ssdl.spartan.spartaNature";
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
    final ICommand c = d.newCommand();
    c.setBuilderName(SpartaBuilder.BUILDER_ID);
    d.setBuildSpec(Utils.append(cs, c));
    project.setDescription(d, null);
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
        description.setBuildSpec(Utils.delete(cs, i));
        project.setDescription(description, null);
        return;
      }
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
