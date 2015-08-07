package org.spartan.refactoring.spartanizations;

import org.spartan.refactoring.wring.Wrings;

/**
 * Stores a list of active cleanups.
 *
 * @author Ofir Elmakias
 * @since 2015-08-02
 */
public enum Cleanups {
  ;
  /** All active cleanups */
  public static AsCleanup[] all = { //
      new AsCleanup(Wrings.ADDITION_SORTER.inner), //
      new AsCleanup(Wrings.AND_TRUE.inner), //
  };
}
