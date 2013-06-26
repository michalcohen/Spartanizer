package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.builder.SpartaBuilder;
import il.ac.technion.cs.ssdl.spartan.builder.SpartanizationNature;

import java.util.Iterator;

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
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ToggleSpartanizationHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public ToggleSpartanizationHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String partId = HandlerUtil.getActivePartIdChecked(event);
		System.out.println(partId);
		final ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		if (selection instanceof IStructuredSelection) {
			for (@SuppressWarnings("rawtypes") final Iterator it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				final Object element = it.next();
				IProject project = null;
				if (element instanceof IProject)
					project = (IProject) element;
				else if (element instanceof IAdaptable)
					project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
				if (project != null)
					toggleNature(project);
			}
		}
		return null;
	}

	private static void toggleNature(final IProject project) {
		try {
			final IProjectDescription description = project.getDescription();
			final String[] natures = description.getNatureIds();
			for (int i = 0; i < natures.length; ++i)
				if (SpartanizationNature.NATURE_ID.equals(natures[i])) {
					// Remove the nature
					final String[] newNatures = new String[natures.length - 1];
					System.arraycopy(natures, 0, newNatures, 0, i);
					System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
					description.setNatureIds(newNatures);
					project.setDescription(description, null);
					project.accept(new IResourceVisitor() {
						@Override public boolean visit(final IResource resource) throws CoreException {
							if (resource instanceof IFile && resource.getName().endsWith(".java"))
								SpartaBuilder.deleteMarkers((IFile) resource);
							return true;
						}
					});
					return;
				}
			// Add the nature
			final String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = SpartanizationNature.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
		} catch (final CoreException e) {
			// we assume that other builder handle cause compilation failure on
			// CoreException
		}
	}
}
