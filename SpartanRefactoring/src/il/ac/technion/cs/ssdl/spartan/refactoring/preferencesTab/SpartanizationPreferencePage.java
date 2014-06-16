package il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab;

import il.ac.technion.cs.ssdl.spartan.builder.Plugin;
import il.ac.technion.cs.ssdl.spartan.refactoring.Spartanization;
import static il.ac.technion.cs.ssdl.spartan.refactoring.All.allRulesNames;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By sub classing {@link FieldEditorPreferencePage}
 * built into {@link org.eclipse.jface} that allows us to create a page that is
 * small and knows how to save, restore and apply itself.
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
public class SpartanizationPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	/**
	 * Instantiates the page and sets its default values
	 */
	public SpartanizationPreferencePage() {
		super(GRID);
		setPreferenceStore(Plugin.getDefault().getPreferenceStore());
		setDescription("Select for each tranformation suggestion if they will be shown\n\n");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		// TODO: There must be a way to make this initialization work from the
		// current list of Spartanization objects.
		for (final String rule : allRulesNames())
			addField(new BooleanFieldEditor(rule, rule + ":", getFieldEditorParent()));
	}

	@Override
	public boolean performOk() {
		// TODO: Convert to StringBuilder
		super.performOk();
		SpartanizationPreferencePage.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		final IPreferenceStore store = Plugin.getDefault().getPreferenceStore();
		String s = "";
		final String[] title = Spartanization.getSpartanTitle();
		for (final String str : title)
			s = s + str + "\n";
		for (final String str : allRulesNames())
			s = s + store.getString(str) + "\n";
		PrintWriter print;
		// TODO: Use the new syntax of "try" with arguments.
		try {
			print = new PrintWriter(Spartanization.getPrefFilePath());
			print.write(s);
			print.close();
		} catch (final FileNotFoundException e) {
			// TODO Treat it like a gentleman
			e.printStackTrace();
		}
		return s != "bb";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(@SuppressWarnings("unused") final IWorkbench workbench) {
		super.initialize();
	}
}
