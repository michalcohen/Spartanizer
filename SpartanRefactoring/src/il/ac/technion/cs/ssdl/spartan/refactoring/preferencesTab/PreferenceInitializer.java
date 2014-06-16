package il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab;

import il.ac.technion.cs.ssdl.spartan.builder.Plugin;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code>
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code> @since
 *          2014/6/16
 */
/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		// TODO: Convert this into a loop or something.
		final IPreferenceStore store = Plugin.getDefault().getPreferenceStore();
		store.setDefault("Comparison With Boolean", "Apply");
		store.setDefault("Forward Declaration", "Apply");
		store.setDefault("Inline Single Use", "Apply");
		store.setDefault("Rename Return Variable to $", "Apply");
		store.setDefault("Shortest Branch First", "Apply");
		store.setDefault("Shortest Operand First", "Apply");
		store.setDefault("Ternarize", "Apply");
	}
}
