package org.spartan.refactoring.utils;

import org.eclipse.jdt.core.dom.*;

public class PlantStatement {
  private final Statement inner;
  public PlantStatement(final Statement inner) {
    this.inner = inner;
  }
  public void intoThen(final IfStatement s) {
    final IfStatement plant = Funcs.asIfStatement(inner);
    s.setThenStatement(plant == null || plant.getElseStatement() != null ? inner : blockify());
  }
  Block blockify() {
    final Block $ = inner.getAST().newBlock();
    $.statements().add(inner);
    return $;
  }
}