package com.example.bedwars.listeners;

import com.example.bedwars.services.ToolsService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Hooks player lifecycle events for tool persistence and rules.
 */
public final class ToolsListener implements Listener {
  private final ToolsService tools;
  public ToolsListener(ToolsService tools) { this.tools = tools; }

  @EventHandler
  public void onJoin(PlayerJoinEvent e) { tools.load(e.getPlayer()); }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) { tools.save(e.getPlayer()); }

  @EventHandler
  public void onRespawn(PlayerRespawnEvent e) { tools.onRespawn(e.getPlayer()); }

  @EventHandler
  public void onDeath(PlayerDeathEvent e) { tools.onDeath(e.getEntity(), e.getDrops()); }

  @EventHandler(ignoreCancelled = true)
  public void onDrop(PlayerDropItemEvent e) {
    Player p = e.getPlayer();
    ItemStack it = e.getItemDrop().getItemStack();
    if (!tools.canDrop(p, it)) e.setCancelled(true);
  }
}
