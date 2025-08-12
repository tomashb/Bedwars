package com.example.bedwars.scoreboard;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.UUID;

/**
 * Minimal scoreboard that displays player counts per arena.
 */
public class ScoreboardManager {

    private final BedwarsPlugin plugin;

    public ScoreboardManager(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Update the scoreboard for all players currently in the arena.
     * The board simply shows the arena name and number of players.
     */
    public void update(Arena arena) {
        for (UUID uuid : arena.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) {
                continue;
            }
            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective obj = board.registerNewObjective("bedwars", "dummy", ChatColor.GOLD + "BedWars");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            obj.getScore(ChatColor.YELLOW + "Arène:").setScore(2);
            obj.getScore(ChatColor.WHITE + arena.getName()).setScore(1);
            obj.getScore(ChatColor.YELLOW + "Joueurs:").setScore(0);
            obj.getScore(ChatColor.WHITE + String.valueOf(arena.getPlayerCount())).setScore(-1);
            p.setScoreboard(board);
        }
    }
}
