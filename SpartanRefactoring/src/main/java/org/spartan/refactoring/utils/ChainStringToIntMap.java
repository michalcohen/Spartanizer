package org.spartan.refactoring.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class ChainStringToIntMap {
  public Map<String, Integer> inner = new HashMap<>();

  public boolean containsKey(final String key) {
    return inner.containsKey(key);
  }
  public boolean containsValue(final int value) {
    return inner.containsValue(new Integer(value));
  }
  public Set<Entry<String, Integer>> entrySet() {
    return inner.entrySet();
  }
  public int get(final Object key) {
    return inner.get(key).intValue();
  }
  public boolean isEmpty() {
    return inner.isEmpty();
  }
  public Set<String> keySet() {
    return inner.keySet();
  }
  public ChainStringToIntMap put(final String key, final int value) {
    assert!inner.containsKey(key);
    inner.put(key, new Integer(value));
    return this;
  }
  public ChainStringToIntMap putOn(final int value, final String... keys) {
    for (final String key : keys)
      put(key, value);
    return this;
  }
  public ChainStringToIntMap putAll(final Map<? extends String, ? extends Integer> m) {
    inner.putAll(m);
    return this;
  }
  public ChainStringToIntMap remove(final String key) {
    inner.remove(key);
    return this;
  }
  public int size() {
    return inner.size();
  }
  public Collection<Integer> values() {
    return inner.values();
  }
}