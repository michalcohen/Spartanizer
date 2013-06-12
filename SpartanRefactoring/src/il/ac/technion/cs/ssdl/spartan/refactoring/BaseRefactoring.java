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
  
  public final ASTRewrite createRewrite(final CompilationUnit cu, SubProgressMonitor pm) {
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
  protected boolean isNodeOutsideSelection(ASTNode node) {
    return isTextSelected() && (node.getStartPosition() > selection.getOffset() + selection.getLength() || node.getStartPosition() < selection
        .getOffset());
  }
  
  protected static boolean isNodeOutsideMarker(ASTNode node, IMarker m) {
    try {
      return (node.getStartPosition() < ((Integer) m.getAttribute(IMarker.CHAR_START)).intValue() ||
    		  node.getStartPosition() + node.getLength() > ((Integer) m.getAttribute(IMarker.CHAR_END)).intValue());
    } catch (CoreException e) {
      return true;
    }
  }
  
  @Override public RefactoringStatus checkInitialConditions(IProgressMonitor pm) {
    RefactoringStatus $ = new RefactoringStatus();
    if (compilationUnit == null && marker == null)
      $.merge(RefactoringStatus.createFatalErrorStatus("Nothing to refactor."));
    return $;
  }
  
  public void setMarker(final IMarker m) {
    marker = m;
  }
  
  @Override public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
    final RefactoringStatus $ = new RefactoringStatus();
    changes.clear();
    // TODO: Catch exceptions and change status accordingly
    if (marker != null) {
      innerRunAsMarkerFix(pm, marker, true);
      marker = null; // consume marker
    } else {
      runAsManualCall(pm);
    }
    pm.done();
    return $;
  }
  
  private void runAsManualCall(IProgressMonitor pm) throws JavaModelException, CoreException {
    ArrayList<ICompilationUnit> units;
    pm.beginTask("Checking preconditions...", 2);
    if (isTextSelected()) {
      units = new ArrayList<ICompilationUnit>();
      units.add(compilationUnit);
    } else {
      units = getAllProjectCompilationUnits(new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
    }
    scanCompilationUnits(units, new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
  }
  
  public RefactoringStatus runAsMarkerFix(IProgressMonitor pm, IMarker m) throws CoreException {
    return innerRunAsMarkerFix(pm, m, false);
  }
  
  private RefactoringStatus innerRunAsMarkerFix(IProgressMonitor pm, IMarker m, boolean preview) throws CoreException {
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
  protected void scanCompilationUnits(ArrayList<ICompilationUnit> units, IProgressMonitor pm) throws IllegalArgumentException,
      CoreException {
    pm.beginTask("Iterating over gathered compilation units...", units.size());
    for (ICompilationUnit u : units) {
      scanCompilationUnit(u, new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
    }
    pm.done();
  }
  
  /**
   * @param u
   * @throws CoreException
   */
  protected void scanCompilationUnit(ICompilationUnit u, IProgressMonitor pm) throws CoreException {
    pm.beginTask("Creating change for a single compilation unit...", 2);
    ASTParser parser = ASTParser.newParser(AST.JLS4);
    parser.setResolveBindings(false);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setSource(u);
    CompilationUnit cu = (CompilationUnit) parser
        .createAST(new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
    TextFileChange textChange = new TextFileChange(u.getElementName(), (IFile) u.getResource());
    textChange.setTextType("java");
    textChange.setEdit(createRewrite(cu, new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)).rewriteAST());
    if (textChange.getEdit().getLength() != 0)
      changes.add(textChange);
    pm.done();
  }
  
  protected void scanCompilationUnitForMarkerFix(IMarker m, IProgressMonitor pm, boolean preview) throws CoreException {
    pm.beginTask("Creating change for a single compilation unit...", 2);
    final ICompilationUnit u = getCompilationUnitFromMarker(m);
    TextFileChange textChange = new TextFileChange(u.getElementName(), (IFile) u.getResource());
    textChange.setTextType("java");
    textChange.setEdit(createRewriteForMarker(new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL), m)
        .rewriteAST());
    if (textChange.getEdit().getLength() != 0) {
      if (preview)
        changes.add(textChange);
      else
        textChange.perform(pm);
    }
    pm.done();
  }
  
  public static ICompilationUnit getCompilationUnitFromMarker(IMarker m) {
    return JavaCore.createCompilationUnitFrom((IFile) m.getResource());
  }
  
  /**
   * @param units
   * @throws JavaModelException
   */
  protected ArrayList<ICompilationUnit> getAllProjectCompilationUnits(IProgressMonitor pm) throws JavaModelException {
    pm.beginTask("Gathering project information...", 1);
    ArrayList<ICompilationUnit> $ = new ArrayList<ICompilationUnit>();
    IJavaProject proj = compilationUnit.getJavaProject();
    for (IPackageFragmentRoot r : proj.getPackageFragmentRoots()) {
      if (r.getKind() == IPackageFragmentRoot.K_SOURCE) {
        for (IJavaElement e : r.getChildren()) {
          if (e.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
            $.addAll(Arrays.asList(((IPackageFragment) e).getCompilationUnits()));
          }
        }
      }
    }
    pm.done();
    return $;
  }
  
  public abstract Collection<SpartanizationRange> checkForSpartanization(CompilationUnit cu);
  
  @Override public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
    return new CompositeChange(getName(), changes.toArray(new Change[changes.size()]));
  }
}
