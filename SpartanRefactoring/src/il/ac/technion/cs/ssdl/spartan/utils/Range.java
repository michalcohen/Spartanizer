package il.ac.technion.cs.ssdl.spartan.utils;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * a range which contains a spartanization suggestion. used for creating text
 * markers
 *
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code>
 */
public class Range {
  /** the beginning of the range (inclusive) */
  public final int from;
  /** the end of the range (exclusive) */
  public final int to;

  /**
   * Instantiates from beginning and end locations
   *
   * @param from
   *          the beginning of the range (inclusive)
   * @param to
   *          the end of the range (exclusive)
   */
  public Range(final int from, final int to) {
    this.from = from;
    this.to = to;
  }

  /**
   * Instantiates from a single ASTNode
   *
   * @param n
   *          arbitrary
   */
  public Range(final ASTNode n) {
    this(n.getStartPosition(), n.getStartPosition() + n.getLength());
  }

  /**
   * @param r
   *          arbitrary
   * @return <code><b>true</b></code> <i>iff</i> <code><b>this</b></code> is
   *         included in the parameter.
   */
  public boolean includedIn(final Range r) {
    return from >= r.from && to <= r.to;
  }

  /**
   * Find an including range
   *
   * @param rs
   *          some arbitrary {@link Range} objects
   * @return the first {@link Range} object among the parameters that contains
   *         <code><b>true</b></code>, or <code><b>true</b></code> <i>iff</i>
   *         <code><b>null</b></code> if not such object can be vound.
   */
  public Range findIncludedIn(final Iterable<Range> rs) {
    for (final Range $ : rs)
      if (includedIn($))
        return $;
    return null;
  }

  /**
   * Determine whether overlaps in any part another range
   *
   * @param r
   *          arbitrary
   * @return <code><b>true</b></code> <i>iff</i> <code><b>this</b></code>
   *         overlaps the parameter.
   */
  public boolean overlapping(final Range r) {
    return from >= r.from || to <= r.to;
  }

  /**
   * Instantiates from beginning and end ASTNodes
   *
   * @param from
   *          the beginning ASTNode (inclusive)
   * @param to
   *          the end ASTNode (inclusive)
   */
  public Range(final ASTNode from, final ASTNode to) {
    this(from.getStartPosition(), to.getStartPosition() + to.getLength());
  }

  @Override public int hashCode() {
    final int $ = from;
    final int $$ = to;
    // Cantor pairing function
    return (int) ($ + 0.5 * ($ + $$) * ($ + 1 + $$));
  }

  @Override public boolean equals(final Object o) {
    boolean $ = false;
    if (o instanceof Range) {
      final Range r = (Range) o;
      $ = from == r.from && to == r.to;
    }
    return $;
  }
}