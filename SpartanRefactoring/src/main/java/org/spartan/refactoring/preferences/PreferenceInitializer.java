package org.spartan.refactoring.preferences;

import static org.spartan.refactoring.spartanizations.Spartanizations.*;

import org.eclipse.core.runtime.preferences.*;
import org.eclipse.jface.preference.*;
import org.spartan.refactoring.builder.*;

/**
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> @since
 *         16/06/2014
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code> @since
 *         2014/6/16 Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
  /**
   * Initialize preferences to default values
   */
  @Override public void initializeDefaultPreferences() {
    final IPreferenceStore store = Plugin.getDefault().getPreferenceStore();
    for (final String rule : allRulesNames())
      store.setDefault(rule, true);
  }
}
