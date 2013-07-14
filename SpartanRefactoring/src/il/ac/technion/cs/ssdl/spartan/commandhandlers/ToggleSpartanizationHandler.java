package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import static il.ac.technion.cs.ssdl.spartan.builder.Utils.append;
import static il.ac.technion.cs.ssdl.spartan.builder.Utils.delete;
import il.ac.technion.cs.ssdl.spartan.builder.SpartaBuilder;
import il.ac.technion.cs.ssdl.spartan.builder.SpartanizationNature;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * A command handler which toggles the spartanization nature
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 * @author Boris van Sosin <boris.van.sosin@gmail.com>
 * 
 * @since 2013/07/01
 */
public class ToggleSpartanizationHandler extends AbstractHandler {
  /**
   * the main method of the command handler, runs when the command is called.
   */
  @Override public Void execute(final ExecutionEvent e) throws ExecutionException {
    return execute(e, HandlerUtil.getActivePartIdChecked(e));
  }
  
  private static Void execute(final ExecutionEvent e, final String partId) throws ExecutionException {
    System.out.println(partId);
    return execute(HandlerUtil.getCurrentSelectionChecked(e));
  }
  
  private static Void execute(final ISelection s) {
    if (s instanceof IStructuredSelection)
      for (final Object o : ((IStructuredSelection) s).toList()) {
        final IProject p = extractProject(o);
        if (p != null)
          toggleNature(p);
      }
    return null;
  }
  
  private static IProject extractProject(final Object o) {
    if (o instanceof IProject)
      return (IProject) o;
    if (o instanceof IAdaptable)
      return (IProject) ((IAdaptable) o).getAdapter(IProject.class);
    return null;
  }
  
  private static void toggleNature(final IProject p) {
    try {
      final IProjectDescription description = p.getDescription();
      final String[] natures = description.getNatureIds();
      for (int i = 0; i < natures.length; ++i)
        if (SpartanizationNature.NATURE_ID.equals(natures[i])) {
          // Remove the nature
          description.setNatureIds(delete(natures, i));
          p.setDescription(description, null);
          p.accept(new IResourceVisitor() {
            @Override public boolean visit(final IResource r) {
              if (r instanceof IFile && r.getName().endsWith(".java"))
                SpartaBuilder.deleteMarkers((IFile) r);
              return true;
            }
          });
          return;
        }
      // Add the nature
      description.setNatureIds(append(natures, SpartanizationNature.NATURE_ID));
      p.setDescription(description, null);
    } catch (final CoreException e) {
      // we assume that other builders handle cause compilation failure on
      // CoreException
    }
  }
}
