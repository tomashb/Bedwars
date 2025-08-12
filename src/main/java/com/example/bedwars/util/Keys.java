package com.example.bedwars.util;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.NamespacedKey;

/** Utility holder for plugin-wide {@link NamespacedKey} instances. */
public final class Keys {
    /** Tag for shop NPC villagers. */
    public static final NamespacedKey NPC = new NamespacedKey(BedwarsPlugin.get(), "bw_npc");
    /** Tag for generator marker armor stands. */
    public static final NamespacedKey GEN = new NamespacedKey(BedwarsPlugin.get(), "bw_gen");
    private Keys() {}
}
