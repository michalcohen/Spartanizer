package il.ac.technion.cs.ssdl.spartan.builder;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Artium Nihamkin
 * 
 * @since 2013/01/01
 */
public class Plugin extends AbstractUIPlugin {
	private static Plugin plugin;

	/**
	 * an empty c'tor. creates an instance of the plugin.
	 */
	public Plugin() {
		plugin = this;
	}

	@Override public void start(final BundleContext context) throws Exception {
		super.start(context);
	}

	@Override public void stop(final BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * @return the (single) instance of the plugin
	 */
	public static Plugin getDefault() {
		return plugin;
	}

	/**
	 * logs an error in the plugin
	 * 
	 * @param t
	 *            an error
	 */
	public static void log(final Throwable t) {
		getDefault().getLog().log(
				new Status(IStatus.ERROR, "il.ac.technion.cs.ssdl.spartan.refactoring", 0, t.getMessage(), t));
	}
}