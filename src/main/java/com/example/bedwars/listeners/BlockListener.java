package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.ArenaManager;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.util.C;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashSet;
import java.util.UUID;
import java.util.Set;

public class BlockListener implements Listener {
    private final ArenaManager arenas; private final Set<String> placed = new HashSet<>();
    public BlockListener(ArenaManager arenas){ this.arenas=arenas; }
    private String key(Block b){ return b.getWorld().getName()+":"+b.getX()+":"+b.getY()+":"+b.getZ(); }

    @EventHandler public void onPlace(BlockPlaceEvent e){
        if (!e.canBuild()) return;
        Block b = e.getBlockPlaced();
        Arena a = arenas.all().stream().filter(ar->ar.getWorldName().equals(b.getWorld().getName())).findFirst().orElse(null);
        if (a!=null){
            int y = b.getY();
            int min = BedwarsPlugin.get().getConfig().getInt("build.min-height",0);
            int max = BedwarsPlugin.get().getConfig().getInt("build.max-height",256);
            if (y < min || y > max){
                e.setCancelled(true);
                e.getPlayer().sendMessage(C.color("&cVous ne pouvez pas construire ici."));
                return;
            }
            if (a.getState() != GameState.RUNNING){
                int radius = BedwarsPlugin.get().getConfig().getInt("build.protect-bed-radius",0);
                for (TeamColor t : TeamColor.values()){
                    Location bed = a.getBed(t);
                    if (bed!=null && bed.getWorld().equals(b.getWorld()) && bed.distance(b.getLocation()) <= radius){
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(C.color("&cZone protégée."));
                        return;
                    }
                }
            }
        }
        placed.add(key(b));
    }

    @EventHandler public void onBreak(BlockBreakEvent e){
        Player p = e.getPlayer(); Block b = e.getBlock();
        Arena a = arenas.all().stream().filter(ar->ar.getWorldName().equals(b.getWorld().getName())).findFirst().orElse(null);
        if (a==null || a.getState()!=GameState.RUNNING) return;
        if (b.getY() < BedwarsPlugin.get().getConfig().getInt("build.min-height",0) ||
            b.getY() > BedwarsPlugin.get().getConfig().getInt("build.max-height",256)){
            e.setCancelled(true);
            p.sendMessage(C.color("&cCe bloc n'est pas cassable."));
            return;
        }
        if (b.getType().name().endsWith("_BED")){
            TeamColor victim=null;
            for (TeamColor t:TeamColor.values()){ if (a.getBed(t)!=null && a.getBed(t).getBlock().equals(b)){ victim=t; break; } }
            TeamColor killerTeam = a.getTeamOf(p.getUniqueId());
            if (victim==null || killerTeam==null) return;
            if (victim==killerTeam){
                e.setCancelled(true);
                p.sendMessage(C.color("&cVous ne pouvez pas casser votre propre lit."));
                return;
            }
            a.setBedAlive(victim,false);
            e.setDropItems(false);
            a.broadcast(C.msgRaw("team.bed_destroyed","team",victim.name(),"player",p.getName()));
            for (UUID id : a.getAllPlayers()){
                Player pl = Bukkit.getPlayer(id);
                if (pl != null) BedwarsPlugin.get().boards().applyTo(pl, a);
            }
            return;
        }
        if (b.getType()!=Material.AIR){
            String k=key(b); if (!placed.contains(k)){ e.setCancelled(true); p.sendMessage(C.color("&cCe bloc n'est pas cassable.")); } else placed.remove(k);
        }
    }
}
