package org.spartan.refactoring.utils;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace;
import static org.spartan.hamcrest.CoreMatchers.is;
import static org.spartan.hamcrest.MatcherAssert.assertThat;
import static org.spartan.hamcrest.OrderingComparison.greaterThanOrEqualTo;

import org.junit.Test;
import org.spartan.refactoring.utils.LiteralParser.Kinds;

@SuppressWarnings({ "static-method", "javadoc" }) public class LiteralParserTest {
  @Test public void hasConstructor() {
    assertThat(new LiteralParser("a"), notNullValue());
  }
  @Test public void hasKind() {
    assertThat(new LiteralParser("2F").kind(), greaterThanOrEqualTo(0));
  }
  @Test public void hasVisibleValue() {
    final String literal = "2F";
    assertThat(new LiteralParser(literal).literal, equalToIgnoringWhiteSpace(literal));
  }
  @Test public void kindCharacter() {
    assertThat(new LiteralParser("'l'").kind(), is(Kinds.CHARACTER.ordinal()));
  }
  @Test public void kindFloatLower() {
    assertThat(new LiteralParser("2f").kind(), is(Kinds.FLOAT.ordinal()));
  }
  @Test public void kindFloatUpper() {
    assertThat(new LiteralParser("2F").kind(), is(Kinds.FLOAT.ordinal()));
  }
  @Test public void kindDoubleLower() {
    assertThat(new LiteralParser("2d").kind(), is(Kinds.DOUBLE.ordinal()));
  }
  @Test public void kindDoublePlain() {
    assertThat(new LiteralParser("0.5").kind(), is(Kinds.DOUBLE.ordinal()));
  }
  @Test public void kindDoubleNoFraction() {
    assertThat(new LiteralParser("0.").kind(), is(Kinds.DOUBLE.ordinal()));
  }
  @Test public void kindDoubleNoDoublePart() {
    assertThat(new LiteralParser("0.4").kind(), is(Kinds.DOUBLE.ordinal()));
  }
  @Test public void kindDoubleUpper() {
    assertThat(new LiteralParser("2D").kind(), is(Kinds.DOUBLE.ordinal()));
  }
  @Test public void kindDoubleWithExponetUpper() {
    assertThat(new LiteralParser("0E1").kind(), is(Kinds.DOUBLE.ordinal()));
  }
  @Test public void kindDoubleWithExponetLower() {
    assertThat(new LiteralParser("0E1").kind(), is(Kinds.DOUBLE.ordinal()));
  }
  @Test public void kindInteger() {
    assertThat(new LiteralParser("22").kind(), is(Kinds.INTEGER.ordinal()));
  }
  @Test public void kindLongLower() {
    assertThat(new LiteralParser("22l").kind(), is(Kinds.LONG.ordinal()));
  }
  @Test public void kindLongUpper() {
    assertThat(new LiteralParser("22L").kind(), is(Kinds.LONG.ordinal()));
  }
  @Test public void floatLiteralsFromSpecification() {
    assertThat(new LiteralParser("1e1f").kind(), is(Kinds.FLOAT.ordinal()));
    assertThat(new LiteralParser("2.f").kind(), is(Kinds.FLOAT.ordinal()));
    assertThat(new LiteralParser(".3f").kind(), is(Kinds.FLOAT.ordinal()));
    assertThat(new LiteralParser("0f").kind(), is(Kinds.FLOAT.ordinal()));
    assertThat(new LiteralParser("3.14f").kind(), is(Kinds.FLOAT.ordinal()));
    assertThat(new LiteralParser("6.022137e+23f").kind(), is(Kinds.FLOAT.ordinal()));
  }
  @Test public void doubleLiteralsFromSpecification() {
    assertThat(new LiteralParser("1e1").kind(), is(Kinds.DOUBLE.ordinal()));
    assertThat(new LiteralParser("2.").kind(), is(Kinds.DOUBLE.ordinal()));
    assertThat(new LiteralParser(".3").kind(), is(Kinds.DOUBLE.ordinal()));
    assertThat(new LiteralParser("0.0").kind(), is(Kinds.DOUBLE.ordinal()));
    assertThat(new LiteralParser("3.14").kind(), is(Kinds.DOUBLE.ordinal()));
    assertThat(new LiteralParser("1e-9d").kind(), is(Kinds.DOUBLE.ordinal()));
    assertThat(new LiteralParser("1e137").kind(), is(Kinds.DOUBLE.ordinal()));
  }
  @Test public void doubleBinaryLiterals() {
    assertThat(new LiteralParser("0x1.fffffffffffffP+1023").kind(), is(Kinds.DOUBLE.ordinal()));
    assertThat(new LiteralParser("0x1.0p-1022").kind(), is(Kinds.DOUBLE.ordinal()));
  }
}