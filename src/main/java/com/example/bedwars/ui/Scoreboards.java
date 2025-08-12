package com.example.bedwars.ui;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class Scoreboards {
    private final BedwarsPlugin plugin; private final com.example.bedwars.arena.ArenaManager arenas;
    public Scoreboards(BedwarsPlugin plugin, com.example.bedwars.arena.ArenaManager arenas){ this.plugin=plugin; this.arenas=arenas; }
    public void applyTo(Player p, Arena a){
        Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective o = sb.registerNewObjective("bw","dummy", ChatColor.GOLD+""+ChatColor.BOLD+"BedWars");
        o.setDisplaySlot(DisplaySlot.SIDEBAR);
        int s=8; o.getScore(ChatColor.WHITE+"Arène: "+a.getName()).setScore(s--);
        // only display teams that are enabled for this arena
        for (TeamColor t : a.getEnabledTeams()){
            String status = a.isBedAlive(t)? "§a✔":"§c✘"; o.getScore(t.chat()+t.display()+ChatColor.WHITE+": "+status).setScore(s--);
        }
        p.setScoreboard(sb);
    }
}
