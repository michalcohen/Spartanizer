package il.org.spartan.spartanizer.dispatch;

import java.util.*;

import il.org.spartan.utils.*;

/** An abstract listener, that takes event that may have one, two, or three
 * parameters; default implementation is empty, extend to specialize, or use
 * {@link Listener.S}
 * @author Yossi Gil
 * @since 2016 */
public interface Listener {
  default void event(Object event) {
    ___.unused(event);
  }

  default void event(Object event, Object o) {
    ___.unused(event, o);
  }

  default void event(Object event, Object o1, Object o2) {
    ___.unused(event, o1, o2);
  }

  /** A listener that records a long string of the message it got.
   * @author Yossi Gil
   * @since 2016 */
  public class Tracing implements Listener {
    private StringBuilder $ = new StringBuilder();

    public String $() {
      return $ + "";
    }

    @Override public void event(Object event) {
      $.append(event + "\n");
    }

    @Override public void event(Object event, Object o) {
      $.append(event + ": O = + " + trim(o) + "\n");
    }

    private static String trim(Object o) {
      return (o + "").substring(1, 35); 
    }

    @Override public void event(Object event, Object o1, Object o2) {
      $.append(event + ": O1 = " + trim(o1) + " O2 = " + trim(o2)+ "\n");
    }
  }

  public class S extends ArrayList<Listener> implements Listener {
    private static final long serialVersionUID = 1L;

    @Override public void event(Object event) {
      for (final Listener l : this)
        l.event(event);
    }

    @Override public void event(Object event, Object o) {
      for (final Listener l : this)
        l.event(event, o);
    }

    @Override public void event(Object event, Object o1, Object o2) {
      for (final Listener l : this)
        l.event(event, o1, o2);
    }

    public static S empty() {
      return new S();
    }
  }
}