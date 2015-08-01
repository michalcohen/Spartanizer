package org.spartan.refactoring.spartanizations;

import org.spartan.refactoring.wring.Wring;

/**
 * An adapter which makes it possible to use a single @{link Wring} as a
 * {@link Cleanup}
 *
 * @author Yossi Gil
 * @since 2015/07/25
 */
public class AsCleanup {
  final Wring inner;
  /** Instantiates this class */
  AsCleanup(final Wring inner) {
    this.inner = inner;
  }
}
