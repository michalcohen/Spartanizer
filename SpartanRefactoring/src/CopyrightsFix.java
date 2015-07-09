import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.fix.TextEditFix;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.text.edits.TextEdit;

public class CopyrightsFix {
  public static ICleanUpFix createCleanUp(final CompilationUnit u, final boolean enabled) {
    final TextEdit edit = null;
    final String changeDescription = null;
    return new TextEditFix(edit, null, changeDescription);
  };
}
