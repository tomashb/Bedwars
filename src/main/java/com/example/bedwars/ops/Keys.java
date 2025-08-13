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
  private final NamespacedKey GEN_ID;
  private final NamespacedKey GEN_HOLO;
  private final NamespacedKey GEN_CAP;
  private final NamespacedKey BW_ITEM;
  private final NamespacedKey BW_PLACED;

  public Keys(Plugin plugin) {
    this.ARENA_ID = new NamespacedKey(plugin, "arena_id");
    this.NPC_KIND = new NamespacedKey(plugin, "npc_kind");
    this.GEN_MARKER = new NamespacedKey(plugin, "gen_marker");
    this.GEN_KIND = new NamespacedKey(plugin, "gen_kind");
    this.HOLO_KIND = new NamespacedKey(plugin, "holo_kind");
    this.GEN_ID = new NamespacedKey(plugin, "gen_id");
    this.GEN_HOLO = new NamespacedKey(plugin, "gen_holo");
    this.GEN_CAP = new NamespacedKey(plugin, "gen_cap");
    this.BW_ITEM = new NamespacedKey(plugin, "bw_item");
    this.BW_PLACED = new NamespacedKey(plugin, "bw_placed");
  }

  public NamespacedKey ARENA_ID() { return ARENA_ID; }

  public NamespacedKey NPC_KIND() { return NPC_KIND; }

  public NamespacedKey GEN_MARKER() { return GEN_MARKER; }

  public NamespacedKey GEN_KIND() { return GEN_KIND; }

  public NamespacedKey HOLO_KIND() { return HOLO_KIND; }

  public NamespacedKey GEN_ID() { return GEN_ID; }

  public NamespacedKey GEN_HOLO() { return GEN_HOLO; }

  public NamespacedKey GEN_CAP() { return GEN_CAP; }

  public NamespacedKey BW_ITEM() { return BW_ITEM; }

  public NamespacedKey BW_PLACED() { return BW_PLACED; }
}

