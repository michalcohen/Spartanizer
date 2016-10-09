package il.org.spartan.spartanizer.dispatch;

import java.util.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;

import il.org.spartan.spartanizer.engine.*;

/** This class represents a worker, which, given a {@link Toolbox} and
 * {@link ASTNode}, generate an <i>advice</i>, which is list of *
 * non-conflicting rewrites.
 * <p>
 * An instance of this class fires by invoking function
 * {@link #fire(ASTRewrite)}. Prior to firing, the instance may be configured by
 * any of the configuration elements found in {@Link AdviceGenerator.Config}.
 * <p>
 * Output is received through {@link AdviceGenerator.Result} methods, some of
 * which are computed lazily
 * @author Yossi Gil
 * @since 2016 */
public abstract class AdviceGenerator {
  public final Config config = new Config();
  private List<Tip> tips;

  public int countTips() {
    return 0; 
  }

  public void setICompilationUnit(final ICompilationUnit $) {
    // TODO Auto-generated method stub
  }


  public List<Tip> tips() {
    // TODO Matteo: this is REAP I think --yg
    return tips = (tips == null? generateTips() : tips); 
  }

  @Override public String toString() {
    return "AdviceGenerator [name=" + config.name() + "]";
  }

  /** To be repeatedly inlined */
  public ASTRewrite weaveRewriter(final ASTNode u) {
    return config.with(u).weaveRewriter();
  }

  /** To be repeatedly inlined */
  public ASTRewrite weaveRewriter(final ASTRewrite $, final ASTNode n) {
    return config.with($).weaveRewriter(n);
  }

  protected abstract List<Tip> generateTips() ;


  /**
   * @return
   */
  private ASTRewrite weaveRewriter() {
    // TODO Auto-generated method stub
    return null;
  }

  /** Listeners, fluent-API, and design stuff
   * @author Yossi Gil
   * @since 2016 */
  @SuppressWarnings("hiding") public class Config {
    /** The {@link AST} on on whch the advice */
    protected AST ast;
    /** Where the advice is stored */
    protected ASTRewrite astRewrite;
    /** The point in the tree from which firing starts */
    protected ASTNode from;
    /** The name by which this instance is known */
    protected String name;
    /** The {@link Toolbox} used in making the advice */
    protected Toolbox toolbox;
    /** A possibly empty list of {@link Listener} objects that might be used for
     * logging, performance management, dash-boards, runtime statistics of
     * quality assurance, progress monitoring, machine learning or whatever;
     * defaults to a do nothing listener */
    private final List<Listener> listeners = Listener.S.empty();
        
    public ASTRewrite astRewrite() {
      return astRewrite;
    }

    public String defaultName() {
      return getClass().getCanonicalName();
    }

    public List<Listener> listeners() {
      return listeners;
    }

    public String name() {
      // TODO Marco: Note the nano...create an issue
      return name = name == null ? defaultName() : null;
    }

    public Toolbox toolbox() {
      return toolbox = toolbox == null ? defaultToolbox() : null;
    }

    public AdviceGenerator with(final AST ast) {
      this.ast = ast;
      return with(ASTRewrite.create(ast));
    }

    public AdviceGenerator with(final ASTNode from) {
      return with((this.from = from).getAST());
    }

    public AdviceGenerator with(final ASTRewrite astRewrite) {
      this.astRewrite = astRewrite;
      return containingInstance();
    }

    public AdviceGenerator with(final String name) {
      this.name = name;
      return containingInstance();
    }

    private AdviceGenerator containingInstance() {
      return AdviceGenerator.this;
    }

    private Toolbox defaultToolbox() {
      return Toolbox.defaultInstance;
    }
  }
}
