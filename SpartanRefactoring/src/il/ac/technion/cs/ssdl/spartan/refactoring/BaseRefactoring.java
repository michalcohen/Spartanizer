package il.ac.technion.cs.ssdl.spartan.refactoring;

import il.ac.technion.cs.ssdl.spartan.refactoring.BasicSpartanization.SpartanizationRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin (v2) the base class for all Spartanization
 *         Refactoring classes. contains common functionality
 */
public abstract class BaseRefactoring extends Refactoring {
  private ITextSelection selection = null;
  private ICompilationUnit compilationUnit = null;
  IMarker marker = null;
  final Collection<TextFileChange> changes = new ArrayList<TextFileChange>();
  
  @Override public abstract String getName();
  
  /**
   * creates an ASTRewrite which contains the changes
   * 
   * @param cu
   *          the Compilation Unit (outermost ASTNode in the Java Grammar)
   * @param pm
   *          a progress monitor in which to display the progress of the
   *          refactoring
   * @return an ASTRewrite which contains the changes
   */
  public final ASTRewrite createRewrite(final CompilationUnit cu, final SubProgressMonitor pm) {
    return createRewrite(pm, cu.getAST(), cu, (IMarker) null);
  }
  
  /**
   * creates an ASTRewrite, under the context of a text marker, which contains
   * the changes
   * 
   * @param pm
   *          a progress monitor in which to display the progress of the
   *          refactoring
   * @param m
   *          the marker
   * @return an ASTRewrite which contains the changes
   */
  private final ASTRewrite createRewrite(final SubProgressMonitor pm, final IMarker m) {
    final ASTParser p = ASTParser.newParser(AST.JLS4);
    p.setResolveBindings(false);
    p.setKind(ASTParser.K_COMPILATION_UNIT);
    p.setSource(getCompilationUnitFromMarker(m));
    return createRewrite(pm, (CompilationUnit) p.createAST(null), m);
  }
  
  private ASTRewrite createRewrite(SubProgressMonitor pm, CompilationUnit cu, IMarker m) {
    return createRewrite(pm, cu.getAST(), cu, m);
  }
  
  private ASTRewrite createRewrite(SubProgressMonitor pm, AST t, CompilationUnit cu, IMarker m) {
    if (pm != null)
      pm.beginTask("Creating rewrite operation...", 1);
    final ASTRewrite $ = createRewrite(t, cu, m);
    if (pm != null)
      pm.done();
    return $;
  }
  
  private ASTRewrite createRewrite(AST ast, CompilationUnit cu, IMarker m) {
    ASTRewrite $ = ASTRewrite.create(ast);
    fillRewrite($, ast, cu, m);
    return $;
  }
  
  protected abstract void fillRewrite(ASTRewrite r, AST t, CompilationUnit cu, IMarker m);
  
  private final boolean isTextSelected() {
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
  
  @Override public RefactoringStatus checkInitialConditions(@SuppressWarnings("unused") final IProgressMonitor pm) {
    final RefactoringStatus $ = new RefactoringStatus();
    if (compilationUnit == null && marker == null)
      $.merge(RefactoringStatus.createFatalErrorStatus("Nothing to refactor."));
    return $;
  }
  
  /**
   * @param m
   *          the marker to set for the refactoring
   */
  public final void setMarker(final IMarker m) {
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
    pm.beginTask("Checking preconditions...", 2);
    scanCompilationUnits(getUnits(pm), new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
  }
  
  private List<ICompilationUnit> getUnits(final IProgressMonitor pm) throws JavaModelException {
    if (!isTextSelected())
      return getAllProjectCompilationUnits(new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
    List<ICompilationUnit> $ = new ArrayList<ICompilationUnit>();
    $.add(compilationUnit);
    return $;
  }
  
  /**
   * @param pm
   *          a progress monitor in which to display the progress of the
   *          refactoring
   * @param m
   *          the marker for which the refactoring needs to run
   * @return a RefactoringStatus
   * @throws CoreException
   *           the JDT core throws it
   */
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
  protected void scanCompilationUnits(final List<ICompilationUnit> cus, final IProgressMonitor pm) throws IllegalArgumentException,
      CoreException {
    pm.beginTask("Iterating over gathered compilation units...", cus.size());
    for (final ICompilationUnit cu : cus)
      scanCompilationUnit(cu, new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
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
    textChange.setEdit(createRewrite(new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL), m).rewriteAST());
    if (textChange.getEdit().getLength() != 0)
      if (preview)
        changes.add(textChange);
      else
        textChange.perform(pm);
    pm.done();
  }
  
  private static ICompilationUnit getCompilationUnitFromMarker(final IMarker m) {
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
  
  /**
   * Checks a Compilation Unit (outermost ASTNode in the Java Grammar) for
   * spartanization suggestions
   * 
   * @param cu
   *          the Compilation Unit
   * @return a collection of SpartanizationRange's, each containing a
   *         spartanization suggestion
   */
  public abstract Collection<SpartanizationRange> checkForSpartanization(CompilationUnit cu);
  
  @Override public Change createChange(@SuppressWarnings("unused") final IProgressMonitor pm) throws OperationCanceledException {
    return new CompositeChange(getName(), changes.toArray(new Change[changes.size()]));
  }
  
  /**
   * @return the selection
   */
  public ITextSelection getSelection() {
    return selection;
  }
  
  /**
   * @param selection
   *          the selection to set
   */
  public void setSelection(ITextSelection selection) {
    this.selection = selection;
  }
  
  /**
   * @return the compilationUnit
   */
  public ICompilationUnit getCompilationUnit() {
    return compilationUnit;
  }
  
  /**
   * @param compilationUnit
   *          the compilationUnit to set
   */
  public void setCompilationUnit(ICompilationUnit compilationUnit) {
    this.compilationUnit = compilationUnit;
  }
  
  protected final boolean inRange(final IMarker m, final ASTNode n) {
    if (m == null && isNodeOutsideSelection(n))
      return false;
    if (m != null && isNodeOutsideMarker(n, m))
      return false;
    return true;
  }
}
