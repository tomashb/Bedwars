package com.example.bedwars.ops;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

/**
 * Centralised {@link NamespacedKey} instances used by the plugin.
 *
 * <p>The keys are created per-plugin instance to avoid reliance on
 * static plugin access and allow for easier testing.</p>
 */
public final class Keys {

  private final NamespacedKey ARENA_ID;
  private final NamespacedKey NPC_KIND;
  private final NamespacedKey GEN_MARKER;
  private final NamespacedKey GEN_KIND;
  private final NamespacedKey HOLO_KIND;

  public Keys(Plugin plugin) {
    this.ARENA_ID = new NamespacedKey(plugin, "arena_id");
    this.NPC_KIND = new NamespacedKey(plugin, "npc_kind");
    this.GEN_MARKER = new NamespacedKey(plugin, "gen_marker");
    this.GEN_KIND = new NamespacedKey(plugin, "gen_kind");
    this.HOLO_KIND = new NamespacedKey(plugin, "holo_kind");
  }

  public NamespacedKey ARENA_ID() { return ARENA_ID; }

  public NamespacedKey NPC_KIND() { return NPC_KIND; }

  public NamespacedKey GEN_MARKER() { return GEN_MARKER; }

  public NamespacedKey GEN_KIND() { return GEN_KIND; }

  public NamespacedKey HOLO_KIND() { return HOLO_KIND; }
}

