package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.ArenaManager;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.shop.ShopManager;
import com.example.bedwars.util.C;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerListener implements Listener {
    private final BedwarsPlugin plugin; private final ArenaManager arenas; private final ShopManager shops;
    public PlayerListener(BedwarsPlugin plugin, ArenaManager arenas, ShopManager shops){ this.plugin=plugin; this.arenas=arenas; this.shops=shops; }

    @EventHandler public void onJoin(PlayerJoinEvent e){
        e.setJoinMessage(null);
        Player p = e.getPlayer();
        if (p.getWorld().getName().equals(plugin.getConfig().getString("lobby-world","world"))){
            p.getInventory().clear();
            ItemStack compass = new ItemStack(Material.COMPASS);
            org.bukkit.inventory.meta.ItemMeta meta = compass.getItemMeta(); meta.setDisplayName("§bMenu BedWars"); compass.setItemMeta(meta);
            p.getInventory().setItem(0, compass);
        }
    }

    @EventHandler public void onQuit(PlayerQuitEvent e){ e.setQuitMessage(null); UUID id=e.getPlayer().getUniqueId(); arenas.all().forEach(a->a.removePlayer(id)); }

    @EventHandler public void onInteract(PlayerInteractEvent e){
        if (e.getItem()!=null && e.getItem().getType()==Material.COMPASS){ e.setCancelled(true); BedwarsPlugin.get().menus().openMain(e.getPlayer()); }
    }

    @EventHandler public void onDeath(PlayerDeathEvent e){
        Player p = e.getEntity(); e.setDeathMessage(null);
        Arena a = arenas.all().stream().filter(ar->ar.getAllPlayers().contains(p.getUniqueId())).findFirst().orElse(null); if (a==null) return;
        TeamColor team = a.getTeamOf(p.getUniqueId()); if (team==null) return;
        e.getDrops().clear(); e.setDroppedExp(0);
        if (a.isBedAlive(team)){
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, ()->{
                p.spigot().respawn();
                p.getInventory().clear();
                p.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD,1));
                // give team-colored wool
                p.getInventory().addItem(new ItemStack(team.wool(),16));
                p.teleport(a.getSpawn(team));
            }, 20L);
        } else {
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, ()->{ p.spigot().respawn(); p.setAllowFlight(true); p.setFlying(true); p.sendMessage(C.color("&cVous êtes éliminé.")); }, 20L);
        }
    }
}
