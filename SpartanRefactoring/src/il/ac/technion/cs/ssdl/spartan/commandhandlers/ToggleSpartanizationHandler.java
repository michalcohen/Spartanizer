package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import static il.ac.technion.cs.ssdl.spartan.utils.Utils.append;
import static il.ac.technion.cs.ssdl.spartan.utils.Utils.delete;
import il.ac.technion.cs.ssdl.spartan.builder.Builder;
import il.ac.technion.cs.ssdl.spartan.builder.Nature;

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
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code>
 * 
 * @since 2013/07/01
 */
public class ToggleSpartanizationHandler extends AbstractHandler {
	/**
	 * the main method of the command handler, runs when the command is called.
	 */
	@Override public Void execute(final ExecutionEvent e) throws ExecutionException {
		try {
			return execute(e, HandlerUtil.getActivePartIdChecked(e));
		} catch (final CoreException x) {
			throw new ExecutionException(x.getMessage());
		}
	}

	private static Void execute(final ExecutionEvent e, final String partId) throws ExecutionException, CoreException {
		System.out.println(partId);
		return execute(HandlerUtil.getCurrentSelectionChecked(e));
	}

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
		return o instanceof IProject ? (IProject) o : !(o instanceof IAdaptable) ? null : (IProject) ((IAdaptable) o)
				.getAdapter(IProject.class);
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
}
