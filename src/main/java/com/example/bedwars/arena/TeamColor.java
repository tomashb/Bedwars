package com.example.bedwars.arena;
import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * Represents the eight default BedWars team colors. Besides the ChatColor and
 * display name used in messages, each team exposes the corresponding wool
 * material so items can easily be given in the team's color.
 */
public enum TeamColor {
    RED(ChatColor.RED, "Rouge"),
    BLUE(ChatColor.BLUE, "Bleu"),
    GREEN(ChatColor.GREEN, "Vert"),
    YELLOW(ChatColor.YELLOW, "Jaune"),
    AQUA(ChatColor.AQUA, "Aqua"),
    WHITE(ChatColor.WHITE, "Blanc"),
    PINK(ChatColor.LIGHT_PURPLE, "Rose"),
    GRAY(ChatColor.GRAY, "Gris");

    private final ChatColor color;
    private final String display;

    TeamColor(ChatColor c, String d){ this.color=c; this.display=d; }

    public ChatColor chat(){ return color; }
    public String display(){ return display; }

    /**
     * Returns the wool material matching this team color.
     */
    public Material wool(){
        return switch (this){
            case RED -> Material.RED_WOOL;
            case BLUE -> Material.BLUE_WOOL;
            case GREEN -> Material.GREEN_WOOL;
            case YELLOW -> Material.YELLOW_WOOL;
            case AQUA -> Material.CYAN_WOOL;
            case WHITE -> Material.WHITE_WOOL;
            case PINK -> Material.PINK_WOOL;
            case GRAY -> Material.GRAY_WOOL;
        };
    }
}
