package com.example.bedwars.arena;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * Colors and metadata for teams.
 */
public enum TeamColor {
  RED("Rouge", ChatColor.RED, Material.RED_WOOL),
  BLUE("Bleu", ChatColor.BLUE, Material.BLUE_WOOL),
  GREEN("Vert", ChatColor.GREEN, Material.GREEN_WOOL),
  YELLOW("Jaune", ChatColor.YELLOW, Material.YELLOW_WOOL),
  AQUA("Aqua", ChatColor.AQUA, Material.LIGHT_BLUE_WOOL),
  WHITE("Blanc", ChatColor.WHITE, Material.WHITE_WOOL),
  PINK("Rose", ChatColor.LIGHT_PURPLE, Material.PINK_WOOL),
  GRAY("Gris", ChatColor.GRAY, Material.GRAY_WOOL);

  public final String display;
  public final ChatColor color;
  public final Material wool;

  TeamColor(String display, ChatColor color, Material wool) {
    this.display = display; this.color = color; this.wool = wool;
  }
}
