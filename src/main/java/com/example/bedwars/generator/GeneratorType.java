package com.example.bedwars.generator;

import org.bukkit.Material;

/**
 * Types of resource generators supported by the plugin.
 */
public enum GeneratorType {
    IRON(Material.IRON_INGOT),
    GOLD(Material.GOLD_INGOT),
    DIAMOND(Material.DIAMOND),
    EMERALD(Material.EMERALD);

    private final Material drop;

    GeneratorType(Material drop) {
        this.drop = drop;
    }

    public Material getDrop() {
        return drop;
    }
}
