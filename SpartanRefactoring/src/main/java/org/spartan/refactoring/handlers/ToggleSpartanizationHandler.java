package org.spartan.refactoring.handlers;

import static org.spartan.utils.Utils.*;

import org.eclipse.core.commands.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.handlers.*;
import org.spartan.refactoring.builder.*;

/**
 * A command handler which toggles the spartanization nature
 *
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code>
 * @since 2013/07/01
 */
public class ToggleSpartanizationHandler extends AbstractHandler {
  private static Void execute(final ISelection s) throws CoreException {
    if (s instanceof IStructuredSelection)
      for (final Object o : ((IStructuredSelection) s).toList()) {
        final IProject p = extractProject(o);
        if (p != null)
          toggleNature(p);
      }
    return null;
  }
  private static IProject extractProject(final Object o) {
    return o instanceof IProject ? (IProject) o : !(o instanceof IAdaptable) ? null : (IProject) ((IAdaptable) o).getAdapter(IProject.class);
  }
  private static void toggleNature(final IProject p) throws CoreException {
    final IProjectDescription description = p.getDescription();
    final String[] natures = description.getNatureIds();
    for (int i = 0; i < natures.length; ++i)
      if (Nature.NATURE_ID.equals(natures[i])) {
        // Remove the nature
        description.setNatureIds(delete(natures, i));
        p.setDescription(description, null);
        p.accept(new IResourceVisitor() {
          @Override public boolean visit(final IResource r) throws CoreException {
            if (r instanceof IFile && r.getName().endsWith(".java"))
              Builder.deleteMarkers((IFile) r);
            return true;
          }
        });
        return;
      }
    // Add the nature
    description.setNatureIds(append(natures, Nature.NATURE_ID));
    p.setDescription(description, null);
  }
  /**
   * the main method of the command handler, runs when the command is called.
   */
  @Override public Void execute(final ExecutionEvent e) throws ExecutionException {
    try {
      return execute(HandlerUtil.getCurrentSelectionChecked(e));
    } catch (final CoreException x) {
      throw new ExecutionException(x.getMessage());
    }
  }
}
