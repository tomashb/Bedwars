package com.example.bedwars.commands;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.ArenaManager;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.gen.GeneratorManager;
import com.example.bedwars.shop.ShopManager;
import com.example.bedwars.util.C;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class BwCommand implements CommandExecutor, TabCompleter {
    private final BedwarsPlugin plugin;
    private final ArenaManager arenas;
    private final GeneratorManager gens;
    private final ShopManager shops;
    public BwCommand(BedwarsPlugin plugin, ArenaManager arenas, GeneratorManager gens, ShopManager shops){
        this.plugin=plugin; this.arenas=arenas; this.gens=gens; this.shops=shops;
    }

    @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (args.length==0){
            if (sender instanceof Player p){ plugin.menus().openMain(p); return true; }
            sender.sendMessage(C.color("&e/bw join <arena>, /bw leave, /bw list")); return true;
        }
        switch(args[0].toLowerCase()){
            case "gui": if (sender instanceof Player p) plugin.menus().openMain(p); return true;
            case "list": sender.sendMessage(C.color("&eArènes: &f"+ arenas.all().stream().map(Arena::getName).collect(Collectors.joining(", ")))); return true;
            case "join":
                if (!(sender instanceof Player p)) return true;
                if (args.length<2){ p.sendMessage(C.msg("error.usage","usage","/bw join <arena>")); return true; }
                Arena a = arenas.get(args[1]); if (a==null){ p.sendMessage(C.msg("error.no_arena")); return true; }
                if (a.getState()==GameState.DISABLED){ p.sendMessage(C.color("&cArène désactivée.")); return true; }
                org.bukkit.Location lobby = a.getLobby();
                if (lobby==null || lobby.getWorld()==null){ p.sendMessage(C.color("&cLobby non défini.")); return true; }
                p.teleport(lobby);
                p.sendMessage(C.msg("arena.join","arena",a.getName()));
                plugin.boards().applyTo(p,a);
                if (a.getTeamOf(p.getUniqueId())==null) plugin.menus().openTeamSelect(p, a.getName(), "JOIN_TEAM");
                return true;
            case "start":
                if (args.length<2){ sender.sendMessage(C.msg("error.usage","usage","/bw start <arena>")); return true; }
                Arena a2 = arenas.get(args[1]); if (a2==null){ sender.sendMessage(C.msg("error.no_arena")); return true; }
                arenas.startArena(a2);
                sender.sendMessage(C.color("&eLancement de l'arène &f"+a2.getName()+"&e."));
                return true;
            case "leave":
                if (!(sender instanceof Player p2)) return true;
                plugin.arenas().all().forEach(ar->ar.removePlayer(p2.getUniqueId()));
                p2.getInventory().clear();
                org.bukkit.World w = org.bukkit.Bukkit.getWorld(plugin.getConfig().getString("lobby-world", "world"));
                if (w != null) p2.teleport(w.getSpawnLocation());
                org.bukkit.inventory.ItemStack compass = new org.bukkit.inventory.ItemStack(org.bukkit.Material.COMPASS);
                org.bukkit.inventory.meta.ItemMeta meta = compass.getItemMeta();
                meta.setDisplayName("§bMenu BedWars"); compass.setItemMeta(meta);
                p2.getInventory().setItem(0, compass);
                p2.setScoreboard(org.bukkit.Bukkit.getScoreboardManager().getMainScoreboard());
                p2.sendMessage(C.msg("arena.leave")); return true;
        }
        return true;
    }

    @Override public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a){
        if (a.length==1) return Arrays.asList("gui","list","join","leave","start");
        if (a.length==2 && (a[0].equalsIgnoreCase("join") || a[0].equalsIgnoreCase("start")))
            return arenas.all().stream().map(Arena::getName).collect(Collectors.toList());
        return Collections.emptyList();
    }
}
