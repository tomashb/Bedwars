package com.example.bedwars.arena;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * Enumeration of team colors with associated display materials.
 */
public enum TeamColor {
    RED(ChatColor.RED, Material.RED_WOOL, Material.RED_BED),
    BLUE(ChatColor.BLUE, Material.BLUE_WOOL, Material.BLUE_BED),
    GREEN(ChatColor.GREEN, Material.GREEN_WOOL, Material.GREEN_BED),
    YELLOW(ChatColor.YELLOW, Material.YELLOW_WOOL, Material.YELLOW_BED),
    AQUA(ChatColor.AQUA, Material.CYAN_WOOL, Material.CYAN_BED),
    WHITE(ChatColor.WHITE, Material.WHITE_WOOL, Material.WHITE_BED),
    PINK(ChatColor.LIGHT_PURPLE, Material.PINK_WOOL, Material.PINK_BED),
    GRAY(ChatColor.GRAY, Material.GRAY_WOOL, Material.GRAY_BED);

    private final ChatColor chat;
    private final Material wool;
    private final Material bed;

    TeamColor(ChatColor chat, Material wool, Material bed) {
        this.chat = chat;
        this.wool = wool;
        this.bed = bed;
    }

    public ChatColor chatColor() {
        return chat;
    }

    public Material wool() {
        return wool;
    }

    public Material bed() {
        return bed;
    }
}
