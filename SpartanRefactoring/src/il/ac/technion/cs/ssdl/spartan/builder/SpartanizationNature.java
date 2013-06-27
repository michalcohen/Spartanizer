package il.ac.technion.cs.ssdl.spartan.builder;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

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
    final IProjectDescription desc = project.getDescription();
    final ICommand[] cs = desc.getBuildSpec();
    for (final ICommand c : cs)
      if (c.getBuilderName().equals(SpartaBuilder.BUILDER_ID))
        return;
    desc.setBuildSpec(make(desc, cs));
    project.setDescription(desc, null);
  }
  
  private ICommand[] make(final IProjectDescription desc, final ICommand[] cs) {
    final ICommand[] $ = new ICommand[cs.length + 1];
    System.arraycopy(cs, 0, $, 0, cs.length);
    final ICommand c = $[$.length - 1] = desc.newCommand();
    c.setBuilderName(SpartaBuilder.BUILDER_ID);
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

  private ICommand[] make(final ICommand[] cs, int i) {
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
