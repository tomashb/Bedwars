package com.example.bedwars.ops;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.NamespacedKey;

/**
 * Centralised PersistentDataContainer keys used by the plugin.
 */
public final class Keys {
  private static NamespacedKey key(String s){ return new NamespacedKey(BedwarsPlugin.get(), s); }

  public static final NamespacedKey ARENA_ID    = key("bw_arena");
  public static final NamespacedKey NPC_KIND    = key("bw_npc");         // "item" / "upgrade"
  public static final NamespacedKey GEN_MARKER  = key("bw_gen_marker");  // markers setup
  public static final NamespacedKey GEN_KIND    = key("bw_gen");         // "IRON"/"GOLD"/...
  public static final NamespacedKey HOLO_KIND   = key("bw_holo");        // "diamond"/"emerald"

  private Keys() {}
}
