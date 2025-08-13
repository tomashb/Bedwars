package com.example.bedwars.util;

import java.util.LinkedHashMap;
import java.util.Map;

/** Utility for building placeholder maps without repeated Map.of calls. */
public final class Args {
  private Args() {}

  public static Map<String, Object> of(Object... kv) {
    if (kv.length % 2 != 0) {
      throw new IllegalArgumentException("pairs required");
    }
    Map<String, Object> m = new LinkedHashMap<>();
    for (int i = 0; i < kv.length; i += 2) {
      m.put(String.valueOf(kv[i]), kv[i + 1]);
    }
    return m;
  }
}
