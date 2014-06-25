package il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab;

import static il.ac.technion.cs.ssdl.spartan.refactoring.All.allRulesNames;
import static il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab.PreferencesStrings.BothLiteralsOptions;
import static il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab.PreferencesStrings.NullAndBoolOptions;
import static il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab.PreferencesStrings.rightLiteralOptions;
import static il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab.PreferencesStrings.swapMessageOptions;
import il.ac.technion.cs.ssdl.spartan.builder.Plugin;
import il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab.PreferencesStrings.Strings;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By sub classing {@link FieldEditorPreferencePage} built
 * into {@link org.eclipse.jface} that allows us to create a page that is small
 * and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 * <p>
 * 
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> (original) @since
 *         10/06/2014
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code> @since
 *         2014/6/16 (v2)
 */
public class SpartanizationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	/**
	 * Instantiates the page and sets its default values
	 */
	public SpartanizationPreferencePage() {
		super(GRID);
		setPreferenceStore(Plugin.getDefault().getPreferenceStore());
		setDescription(Strings.description);
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override public void createFieldEditors() {
		for (final String rule : allRulesNames())
			addField(new BooleanFieldEditor(rule, rule + ":", getFieldEditorParent()));
		addField(new RadioGroupFieldEditor("Null And Boolean Positioning", "Null And Boolean Positioning", 1,
				NullAndBoolOptions, getFieldEditorParent()));
		addField(new RadioGroupFieldEditor("Allow Literals Repositioning", "Allow Literals Repositioning", 1,
				BothLiteralsOptions, getFieldEditorParent()));
		addField(new RadioGroupFieldEditor("Behavior On Right Operand Literals", "Behavior On Right Operand Literals",
				1, rightLiteralOptions, getFieldEditorParent()));
		addField(new RadioGroupFieldEditor("Operand Swap Messages", "Operand Swap Messages", 1, swapMessageOptions,
				getFieldEditorParent()));
	}

	@Override public boolean performOk() {
		super.performOk();
		SpartanizationPreferencePage.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		final StringBuilder s = new StringBuilder("");
		for (final String str : PreferencesFile.getSpartanTitle())
			s.append(str + "\n");
		final IPreferenceStore store = Plugin.getDefault().getPreferenceStore();
		for (final String str : allRulesNames())
			s.append(store.getString(str) + "\n");
		s.append(store.getString("Null And Boolean Positioning") + "\n");
		s.append(store.getString("Allow Literals Repositioning") + "\n");
		s.append(store.getString("Numeric Literals Positioning") + "\n");
		s.append(store.getString("Operand Swap Messages") + "\n");
		try (PrintWriter print = new PrintWriter(PreferencesFile.getPrefFilePath())) {
			print.write(s.toString());
		} catch (final FileNotFoundException e) {
			e.printStackTrace(); // Might be permissions problem
		}
		return true;
	}

	/**
	 * Initializes the workbench
	 */
	@Override public void init(@SuppressWarnings("unused") final IWorkbench workbench) {
		super.initialize();
	}
}
