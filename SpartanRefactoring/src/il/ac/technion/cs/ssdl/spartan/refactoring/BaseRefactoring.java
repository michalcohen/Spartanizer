package il.ac.technion.cs.ssdl.spartan.refactoring;

import il.ac.technion.cs.ssdl.spartan.refactoring.BasicSpartanization.SpartanizationRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;

public abstract class BaseRefactoring extends Refactoring {
  ITextSelection selection = null;
  ICompilationUnit compilationUnit = null;
  IMarker marker = null;
  final Collection<TextFileChange> changes = new ArrayList<TextFileChange>();
  
  @Override public abstract String getName();
  
  protected abstract ASTRewrite innerCreateRewrite(final CompilationUnit cu, final SubProgressMonitor pm, final IMarker m);
  
  public final ASTRewrite createRewrite(final CompilationUnit cu, final SubProgressMonitor pm) {
    return innerCreateRewrite(cu, pm, null);
  }
  
  public final ASTRewrite createRewriteForMarker(final SubProgressMonitor pm, final IMarker m) {
    final ASTParser parser = ASTParser.newParser(AST.JLS4);
    parser.setResolveBindings(false);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setSource(getCompilationUnitFromMarker(m));
    return innerCreateRewrite((CompilationUnit) parser.createAST(null), pm, m);
  }
  
  protected boolean isTextSelected() {
    return selection != null && !selection.isEmpty() && selection.getLength() != 0;
  }
  
  /**
   * Determines if the node is outside of the selected text.
   * 
   * @return true if the node is not inside selection. If there is no selection
   *         at all will return false.
   */
  protected boolean isNodeOutsideSelection(final ASTNode node) {
    return isTextSelected()
        && (node.getStartPosition() > selection.getOffset() + selection.getLength() || node.getStartPosition() < selection
            .getOffset());
  }
  
  protected static boolean isNodeOutsideMarker(final ASTNode node, final IMarker m) {
    try {
      return node.getStartPosition() < ((Integer) m.getAttribute(IMarker.CHAR_START)).intValue()
          || node.getStartPosition() + node.getLength() > ((Integer) m.getAttribute(IMarker.CHAR_END)).intValue();
    } catch (final CoreException e) {
      return true;
    }
  }
  
  @Override public RefactoringStatus checkInitialConditions(final IProgressMonitor pm) {
    final RefactoringStatus $ = new RefactoringStatus();
    if (compilationUnit == null && marker == null)
      $.merge(RefactoringStatus.createFatalErrorStatus("Nothing to refactor."));
    return $;
  }
  
  public void setMarker(final IMarker m) {
    marker = m;
  }
  
  @Override public RefactoringStatus checkFinalConditions(final IProgressMonitor m) throws CoreException,
      OperationCanceledException {
    final RefactoringStatus $ = new RefactoringStatus();
    changes.clear();
    // TODO: Catch exceptions and change status accordingly
    if (marker != null) {
      innerRunAsMarkerFix(m, marker, true);
      marker = null; // consume marker
    } else
      runAsManualCall(m);
    m.done();
    return $;
  }
  
  private void runAsManualCall(final IProgressMonitor pm) throws JavaModelException, CoreException {
    ArrayList<ICompilationUnit> units;
    pm.beginTask("Checking preconditions...", 2);
    if (isTextSelected()) {
      units = new ArrayList<ICompilationUnit>();
      units.add(compilationUnit);
    } else
      units = getAllProjectCompilationUnits(new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
    scanCompilationUnits(units, new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
  }
  
  public RefactoringStatus runAsMarkerFix(final IProgressMonitor pm, final IMarker m) throws CoreException {
    return innerRunAsMarkerFix(pm, m, false);
  }
  
  private RefactoringStatus innerRunAsMarkerFix(final IProgressMonitor pm, final IMarker m, final boolean preview)
      throws CoreException {
    final RefactoringStatus $ = new RefactoringStatus();
    marker = m;
    pm.beginTask("Running refactoring...", 2);
    scanCompilationUnitForMarkerFix(m, pm, preview);
    marker = null;
    pm.done();
    return $;
  }
  
  /**
   * Creates a change from each compilation unit and stores it in the changes
   * array
   * 
   * @throws IllegalArgumentException
   * @throws CoreException
   */
  protected void scanCompilationUnits(final ArrayList<ICompilationUnit> units, final IProgressMonitor pm)
      throws IllegalArgumentException, CoreException {
    pm.beginTask("Iterating over gathered compilation units...", units.size());
    for (final ICompilationUnit u : units)
      scanCompilationUnit(u, new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
    pm.done();
  }
  
  /**
   * @param u
   * @throws CoreException
   */
  protected void scanCompilationUnit(final ICompilationUnit u, final IProgressMonitor pm) throws CoreException {
    pm.beginTask("Creating change for a single compilation unit...", 2);
    final ASTParser parser = ASTParser.newParser(AST.JLS4);
    parser.setResolveBindings(false);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setSource(u);
    final CompilationUnit cu = (CompilationUnit) parser.createAST(new SubProgressMonitor(pm, 1,
        SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
    final TextFileChange textChange = new TextFileChange(u.getElementName(), (IFile) u.getResource());
    textChange.setTextType("java");
    textChange.setEdit(createRewrite(cu, new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)).rewriteAST());
    if (textChange.getEdit().getLength() != 0)
      changes.add(textChange);
    pm.done();
  }
  
  protected void scanCompilationUnitForMarkerFix(final IMarker m, final IProgressMonitor pm, final boolean preview)
      throws CoreException {
    pm.beginTask("Creating change for a single compilation unit...", 2);
    final ICompilationUnit u = getCompilationUnitFromMarker(m);
    final TextFileChange textChange = new TextFileChange(u.getElementName(), (IFile) u.getResource());
    textChange.setTextType("java");
    textChange.setEdit(createRewriteForMarker(new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL), m)
        .rewriteAST());
    if (textChange.getEdit().getLength() != 0)
      if (preview)
        changes.add(textChange);
      else
        textChange.perform(pm);
    pm.done();
  }
  
  public static ICompilationUnit getCompilationUnitFromMarker(final IMarker m) {
    return JavaCore.createCompilationUnitFrom((IFile) m.getResource());
  }
  
  /**
   * @param units
   * @throws JavaModelException
   */
  protected ArrayList<ICompilationUnit> getAllProjectCompilationUnits(final IProgressMonitor pm) throws JavaModelException {
    pm.beginTask("Gathering project information...", 1);
    final ArrayList<ICompilationUnit> $ = new ArrayList<ICompilationUnit>();
    final IJavaProject proj = compilationUnit.getJavaProject();
    for (final IPackageFragmentRoot r : proj.getPackageFragmentRoots())
      if (r.getKind() == IPackageFragmentRoot.K_SOURCE)
        for (final IJavaElement e : r.getChildren())
          if (e.getElementType() == IJavaElement.PACKAGE_FRAGMENT)
            $.addAll(Arrays.asList(((IPackageFragment) e).getCompilationUnits()));
    pm.done();
    return $;
  }
  
  public abstract Collection<SpartanizationRange> checkForSpartanization(CompilationUnit cu);
  
  @Override public Change createChange(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
    return new CompositeChange(getName(), changes.toArray(new Change[changes.size()]));
  }
}
