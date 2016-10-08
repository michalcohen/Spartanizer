package il.org.spartan.spartanizer.dispatch;

import java.util.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;

import il.org.spartan.plugin.*;
import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.tipping.*;
import il.org.spartan.utils.*;

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

  public List<Tip> tips() {
    // TODO Matteo: this is REAP I think --yg
    return tips = (tips == null? generateTips() : tips); 
  }

  protected abstract List<Tip> generateTips() ;


  /** To be repeatedly inlined */
  public ASTRewrite weaveRewriter(final ASTNode u) {
    return config.with(u).weaveRewriter();
  }

  /**
   * @return
   */
  private ASTRewrite weaveRewriter() {
    // TODO Auto-generated method stub
    return null;
  }

  /** To be repeatedly inlined */
  public ASTRewrite weaveRewriter(final ASTRewrite $, final ASTNode n) {
    return config.with($).weaveRewriter(n);
  }

  public void setICompilationUnit(final ICompilationUnit $) {
    // TODO Auto-generated method stub
  }

  @Override public String toString() {
    return "AdviceGenerator [name=" + config.name() + "]";
  }


  /** Listeners, fluent-API, and design stuff
   * @author Yossi Gil
   * @since 2016 */
  @SuppressWarnings("hiding") public class Config {
    /** Where the advice is stored */
    protected ASTRewrite astRewrite;
    /** The name by which this instance is known */
    protected String name;
    /** The {@link Toolbox} used in making the advice */
    protected Toolbox toolbox;
    /** The {@link AST} on on whch the advice */
    protected AST ast;
    /** The point in the tree from which firing starts */
    protected ASTNode from;
    /** A possibly empty list of {@link Listener} objects that might be used for
     * logging, performance management, dash-boards, runtime statistics of
     * quality assurance, progress monitoring, machine learning or whatever;
     * defaults to a do nothing listener */
    private final Listeners listeners = new Listeners();

    public ASTRewrite astRewrite() {
      return astRewrite;
    }

    public String defaultName() {
      return getClass().getCanonicalName();
    }

    public Toolbox toolbox() {
      return toolbox = toolbox == null ? defaultToolbox() : null;
    }

    private Toolbox defaultToolbox() {
      return Toolbox.defaultInstance;
    }

    public Listeners listeners() {
      return listeners;
    }

    public String name() {
      // TODO Marco: Note the nano...create an issue
      return name = name == null ? defaultName() : null;
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
  }

  public static class Listeners extends ArrayList<Listener> implements Listener {
    private static final long serialVersionUID = 1L;

    @Override public void begin(final ASTNode n) {
      for (final Listener l : this)
        l.begin(n);
    }

    @Override public void drop(final ASTNode n, final Tipper<?> t) {
      for (final Listener l : this)
        l.drop(n, t);
    }

    @Override public void end(final ASTNode n) {
      for (final Listener l : this)
        l.end(n);
    }

    @Override public void hit(final ASTNode n) {
      for (final Listener l : this)
        l.hit(n);
    }

    @Override public void select(final ASTNode n, final Tipper<?> t) {
      for (final Listener l : this)
        l.hit(n);
    }
  }

  /** Listeners, fluent-API, and design stuff
   * @author Yossi Gil
   * @since 2016 */
  public interface Listener {
    void begin(final ASTNode n);

    void drop(final ASTNode n, final Tipper<?> t);

    void end(final ASTNode n);

    void hit(final ASTNode n);

    void select(final ASTNode n, final Tipper<?> t);

    public interface Ticker {
      default void begin(final ASTNode n) {
        tick("BEGIN", n);
      }

      default void drop(final ASTNode n, final Tipper<?> t) {
        tick("DROP", n, t);
      }

      default void end(final ASTNode n) {
        tick("END", n);
      }

      default void hit(final ASTNode n) {
        tick("HIT", n);
      }

      default void select(final ASTNode n, final Tipper<?> t) {
        tick("SELECT", n, t);
      }

      void tick(String kind, ASTNode n);

      void tick(String kind, ASTNode n, Tipper<?> t);
    }

    /** Listeners, fluent-API, and design stuff
     * @author Yossi Gil
     * @since 2016 */
    public interface Ignoring extends Ticker {
      @Override default void tick(final String kind, final ASTNode n) {
        ___.unused(kind, n);
      }

      @Override default void tick(final String kind, final ASTNode n, final Tipper<?> t) {
        ___.unused(kind, n, t);
      }
    }

    /** Listeners, fluent-API, and design stuff
     * @author Yossi Gil
     * @since 2016 */
    public class Tracing implements Ticker {
      final StringBuilder $ = new StringBuilder();

      @Override public void tick(final String kind, final ASTNode n) {
        $.append(kind + ": N = + " + n + "\n");
      }

      @Override public void tick(final String kind, final ASTNode n, final Tipper t) {
        $.append(kind + ": N = + " + n + "\n\t T = " + t + "\n");
      }
    }
  }

  public int countTips() {
    return 0; 
  }
}
