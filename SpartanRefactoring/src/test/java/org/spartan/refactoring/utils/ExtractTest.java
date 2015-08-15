package org.spartan.refactoring.utils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jdt.core.dom.Statement;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Test;
import org.junit.Test;
import org.junit.Test;

public class ExtractTest {
  @Test public void core() {
    final Statement s = null;
    assertThat(Extract.core(s), nullValue());
  }
}
