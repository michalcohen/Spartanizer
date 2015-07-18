package org.spartan.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author Yossi Gil <code><yossi.gil [at] gmail.com></code>
 * @since 2013/07/01
 *
 */
public enum Utils {
  ;
  /**
   * Impose an ordering on type <code><b>boolean</b></code> by which
   * <code><b>true</b></code> is greater than <code><b>false</b></code>.
   *
   * @param b1
   *          JD
   * @param b2
   *          JD
   *
   * @return an integer that is negative, zero or positive depending on whether
   *         the first argument is less than, equal to or greater than the
   *         second.
   *
   * @see Comparable
   * @see Comparator
   */
  public static int compare(final boolean b1, final boolean b2) {
    return b1 == b2 ? 0 : b1 ? 1 : -1;
  }
  public static String removeSuffix(final String s, final String postfix) {
    for (String $ = s;; $ = $.substring(0, $.length() - postfix.length()))
      if (!$.endsWith(postfix))
        return $;
  }
  public static String removePrefix(final String s, final String prefix) {
    for (String $ = s;; $ = $.substring(prefix.length()))
      if (!$.startsWith(prefix))
        return $;
  }
  /**
   * Convert variadic list into an array
   *
   * @param os
   *          _
   * @return the parameter, as an array.
   */
  public static Object[] objects(final Object... os) {
    return os;
  }
  /**
   * determine whether there is a null in a sequence of object
   *
   * @param os
   *          an unknown number of objects
   * @return true if one of the objects is a null or false otherwise
   */
  public static boolean hasNull(final Object... os) {
    for (final Object o : os)
      if (o == null)
        return true;
    return false;
  }
  /**
   * Appends an element to an array, by reallocating an array whose size is
   * greater by one and placing the element at the last position.
   *
   * @param ts
   *          an arbitrary array
   * @param t
   *          an element
   * @return the newly created array
   */
  public static <T> T[] append(final T[] ts, final T t) {
    final T[] $ = Arrays.copyOf(ts, 1 + ts.length);
    $[ts.length] = t;
    return $;
  }
  /**
   * Deletes a specified element from an array, by reallocating an array whose
   * size is smaller by one and shifting the other elements down.
   *
   * @param ts
   *          an arbitrary array
   * @param i
   *          position of element to be deleted
   * @return the newly created array
   */
  public static <T> T[] delete(final T[] ts, final int i) {
    final T[] $ = Arrays.copyOf(ts, ts.length - 1);
    System.arraycopy(ts, i + 1, $, i, $.length - i);
    return $;
  }
  /**
   * Sorts an array
   *
   * @param is
   *          what to sort
   * @return the given array with elements in sorted order
   */
  public static int[] sort(final int[] is) {
    Arrays.sort(is);
    return is;
  }
  /**
   * Convert multiple arguments into an array
   *
   * @param ts
   *          a sequence of arguments of the same type
   * @return an array representation of the parameter
   */
  @SafeVarargs public static <T> T[] asArray(final T... ts) {
    return ts;
  }
  /**
   * Determine if an integer can be found in a list of values
   *
   * @param candidate
   *          what to search for
   * @param is
   *          where to search
   * @return true if the the item is found in the list
   */
  @SafeVarargs public static boolean intIsIn(final int candidate, final int... is) {
    for (final int i : is)
      if (i == candidate)
        return true;
    return false;
  }
  /**
   * Determine if an item can be found in a list of values
   *
   * @param candidate
   *          what to search for
   * @param ts
   *          where to search
   * @return true if the the item is found in the list
   */
  @SafeVarargs public static <T> boolean in(final T candidate, final T... ts) {
    for (final T t : ts)
      if (t != null && t.equals(candidate))
        return true;
    return false;
  }
  /**
   * Determine whether an integer is a valid list index
   *
   * @param i
   *          some integer
   * @param ts
   *          a list of things
   * @return <code><b>true</b></code> <i>iff</i> the index is valid index into
   *         the list. and it is the last one in it.
   */
  public static <T> boolean inRange(final int i, final List<T> ts) {
    return i >= 0 && i < ts.size();
  }
  @SafeVarargs public static <T> Collection<T> asList(final T... ts) {
    final Collection<T> $ = new ArrayList<>(ts.length);
    for (final T t : ts)
      $.add(t);
    return $;
  }
}
