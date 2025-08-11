package com.example.bedwars.arena;
import org.bukkit.ChatColor;
public enum TeamColor {
    RED(ChatColor.RED, "Rouge"),
    BLUE(ChatColor.BLUE, "Bleu"),
    GREEN(ChatColor.GREEN, "Vert"),
    YELLOW(ChatColor.YELLOW, "Jaune"),
    AQUA(ChatColor.AQUA, "Aqua"),
    WHITE(ChatColor.WHITE, "Blanc"),
    PINK(ChatColor.LIGHT_PURPLE, "Rose"),
    GRAY(ChatColor.GRAY, "Gris");
    private final ChatColor color; private final String display;
    TeamColor(ChatColor c, String d){ this.color=c; this.display=d; }
    public ChatColor chat(){ return color; } public String display(){ return display; }
}
