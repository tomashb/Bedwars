package com.example.bedwars.gen;

import java.util.EnumMap;
import java.util.Map;

/**
 * Simple metrics collector for generator events.
 */
public final class GenTelemetry {
  private final Map<GeneratorType, Integer> dropped = new EnumMap<>(GeneratorType.class);
  private final Map<GeneratorType, Integer> picked = new EnumMap<>(GeneratorType.class);

  public void recordDrop(GeneratorType type, int amount) {
    dropped.merge(type, amount, Integer::sum);
  }

  public void recordPickup(GeneratorType type, int amount) {
    picked.merge(type, amount, Integer::sum);
  }

  public Map<GeneratorType, Integer> getDropped() { return dropped; }
  public Map<GeneratorType, Integer> getPicked() { return picked; }
}
