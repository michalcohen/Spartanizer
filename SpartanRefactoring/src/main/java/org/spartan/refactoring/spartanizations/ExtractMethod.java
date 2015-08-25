package org.spartan.refactoring.spartanizations;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.ltk.core.refactoring.Change;
import org.spartan.refactoring.utils.UnifiedGroup;
import org.spartan.utils.Range;

/**
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code> (original /
 *         24.06.2015)
 * @since 2015/06/24
 */
@SuppressWarnings("restriction") // "internal use only" for
// "ExtractMethodRefactoring" import,
// which is supposed to be fine due to the
// fact that extract method API is internal.
public class ExtractMethod extends Spartanization {
  /** Instantiates this class */
  public ExtractMethod() {
    super("Split method", "Extract a new method from the current one based on Spartan hyuristics");
  }
  CompilationUnit oldCu;
  @Override protected ASTVisitor collectOpportunities(final List<Range> opportunities) {
    // TODO Ofir: No opportunities for now, if it's 2016 and not added yet,
    // blame
    return new ASTVisitor() {
      @Override public boolean visit(final MethodDeclaration n) {
        opportunities.add(new Range(n));
        return true;
      }
    };
  }
  final int MinimumGroupSizeForExtraction = 3;
  final int MaximunGroupRelativeToMethodSize = 3;
  @Override protected final void fillRewrite(@SuppressWarnings("unused") final ASTRewrite r, final AST t, final CompilationUnit cu, @SuppressWarnings("unused") final IMarker m) {
    cu.accept(new ASTVisitor() {
      @SuppressWarnings("boxing") @Override public boolean visit(final MethodDeclaration md) {
        final Block b = md.getBody();
        final UnifiedGroup ug = new UnifiedGroup(40);
        b.accept(new ASTVisitor() {
          @Override public boolean visit(final SimpleName sn) {
            // if (sn.isDeclaration()) return true; // could be added
            final int line = cu.getLineNumber(sn.getStartPosition());
            ug.add(sn.toString(), line); // Add all simple names to the queue
            return true;
          }
        });
        final List<LinkedList<Integer>> groups = new LinkedList<>();
        for (final LinkedList<Integer> group : ug)
          if (group.size() >= MinimumGroupSizeForExtraction && group.size() <= md.getBody().statements().size() - MaximunGroupRelativeToMethodSize)
            groups.add(0, group);
        // TODO Ofir: random method name for now - will be changed later on
        for (final LinkedList<Integer> group : groups) {
          extract(cu.getPosition(group.getFirst(), 0), cu.getPosition(group.getLast() + 1, 0));
          break; // TODO Ofir: support multiple groups future, Only the first
                 // group for now,
        }
        return true;
        // TODO Note: there is a known bug right now - that simple name also
        // includes class names
      }
      private void extract(final int begin, final int end) {
        // final Random rand = new Random();
        final CompilationUnit newCu = cu; // This line does nothing right now
        // - but it will perform deep clone
        // of the computation unit in order
        // to preserve its initial state
        final ExtractMethodRefactoring tempR = new ExtractMethodRefactoring(newCu, begin, end - begin + 1);
        try {
          tempR.setMethodName("newMethod" /* + rand.nextInt(50) */);
          final NullProgressMonitor pm = new NullProgressMonitor();
          tempR.checkAllConditions(pm);
          final Change change = tempR.createChange(pm);
          change.perform(pm);
        } catch (final Exception e) {
          e.printStackTrace();
        }
      }
    });
  }
}
