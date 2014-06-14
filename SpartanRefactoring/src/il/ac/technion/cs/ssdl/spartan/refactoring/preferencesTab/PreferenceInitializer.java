package il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab;

import il.ac.technion.cs.ssdl.spartan.builder.Plugin;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = Plugin.getDefault().getPreferenceStore();
		store.setDefault("Comparison With Boolean", "Warning");
		store.setDefault("Forward Declaration", "Warning");
		store.setDefault("Inline Single Use", "Warning");
		store.setDefault("Rename Return Variable to $", "Warning");
		store.setDefault("Shortest Branch First", "Warning");
		store.setDefault("Shortest Operand First", "Warning");
		store.setDefault("Ternarize", "Warning");
	}
}
