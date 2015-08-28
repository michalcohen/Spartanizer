package org.spartan.utils;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * An immutable integral range, representing all integers between {@link #from},
 * up to, but not including, {@link #to}, i.e.,
 *
 * <pre>
 * {@link #from}, {@link #from}+1, ..., {@link #to}-1
 * </pre>
 *
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code>
 * @since 2012
 */
public class Range {
  /** the beginning of the range (inclusive) */
  public final int from;
  /** the end of the range (exclusive) */
  public final int to;
  /**
   * The number of integers in the range
   *
   * @return a non-negative integer, computed as {@link #to} -{@link #from}
   */
  public int size() {
    return to - from;
  }
  /**
   * Instantiates from beginning and end locations
   *
   * @param from JD
   * @param to JD
   */
  public Range(final int from, final int to) {
    this.from = from;
    this.to = to;
  }
  /**
   * Instantiates using values found in another intance
   *
   * @param other other
   */
  public Range(final Range other) {
    this(other.from, other.to);
  }
  /**
   * @param r arbitrary
   * @return <code><b>true</b></code> <i>iff</i> <code><b>this</b></code> is
   *         included in the parameter.
   */
  public boolean includedIn(final Range r) {
    return from >= r.from && to <= r.to;
  }
  /**
   * Find an including range
   *
   * @param rs some arbitrary {@link Range} objects
   * @return the first {@link Range} object among the parameters that contains
   *         <code><b>true</b></code>, or <code><b>true</b></code> <i>iff</i>
   *         <code><b>null</b></code> if not such object can be found.
   */
  public Range findIncludedIn(final Iterable<? extends Range> rs) {
    for (final Range $ : rs)
      if (includedIn($))
        return $;
    return null;
  }
  /**
   * Prune all ranges in a given list that include this object.
   *
   * @param rs JD
   */
  public void pruneIncluders(final List<? extends Range> rs) {
    for (;;) {
      final Range includesMe = findIncludedIn(rs);
      if (includesMe == null)
        return;
      rs.remove(includesMe);
    }
  }
  /**
   * Determine whether overlaps in any part another range
   *
   * @param r arbitrary
   * @return <code><b>true</b></code> <i>iff</i> <code><b>this</b></code>
   *         overlaps the parameter.
   */
  public boolean overlapping(final Range r) {
    return from >= r.from || to <= r.to;
  }
  /**
   * Instantiates from beginning and end ASTNodes
   *
   * @param from the beginning ASTNode (inclusive)
   * @param to the end ASTNode (inclusive)
   */
  public Range(final ASTNode from, final ASTNode to) {
    this(from.getStartPosition(), to.getStartPosition() + to.getLength());
  }
  @Override public int hashCode() {
    final int $ = from;
    final int $$ = to;
    // Cantor pairing function
    return (int) ($ + 0.5 * ($ + $$) * ($ + $$ + 1));
  }
  @Override public boolean equals(final Object o) {
    boolean $ = false;
    if (o instanceof Range) {
      final Range r = (Range) o;
      $ = from == r.from && to == r.to;
    }
    return $;
  }
  /**
   * Merge with another record
   *
   * @param r JD
   * @return A newly created range representing the merge.
   */
  public Range merge(final Range r) {
    return new Range(Math.min(from, r.from), Math.max(to, r.to));
  }
}