package il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab;

import static il.ac.technion.cs.ssdl.spartan.refactoring.All.allRulesNames;
import il.ac.technion.cs.ssdl.spartan.builder.Plugin;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> @since
 *         16/06/2014
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code> @since
 *         2014/6/16
 * 
 *         Class used to initialize default preference values.
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
		final IPreferenceStore store = Plugin.getDefault().getPreferenceStore();
		for (final String rule : allRulesNames())
			store.setDefault(rule, true);
	}
}
