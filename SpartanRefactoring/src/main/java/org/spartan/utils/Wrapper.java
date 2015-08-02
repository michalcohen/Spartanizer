package org.spartan.utils;

/**
 * A generic wrapper classes which can store and retrieve values of any type.
 * Main use is in
 * 
 * @author Yossi Gil
 * @since 2015-08-02
 * @param <T> JD
 */
public class Wrapper<T> {
  private T t = null;
  /**
   * Set the value wrapped in this object.
   * 
   * @param t JD
   */
  public void set(T t) {
    this.t = t;
  }
  /**
   * @return the value wrapped in this object.
   */
  public T get() {
    return t;
  }
}
