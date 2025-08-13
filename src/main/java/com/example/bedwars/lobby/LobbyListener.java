package com.example.bedwars.lobby;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.gui.TeamSelectMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/** Handles lobby items interactions. */
public final class LobbyListener implements Listener {
  private final BedwarsPlugin plugin;
  private final LobbyItemsService items;
  private final TeamSelectMenu menu;

  public LobbyListener(BedwarsPlugin plugin, LobbyItemsService items, TeamSelectMenu menu) {
    this.plugin = plugin; this.items = items; this.menu = menu;
  }

  @EventHandler(ignoreCancelled = true)
  public void onInteract(PlayerInteractEvent e) {
    ItemStack it = e.getItem();
    if (it == null) return;
    if (items.isTeamSelector(it)) {
      menu.open(e.getPlayer());
      e.setCancelled(true);
    } else if (items.isLeaveItem(it)) {
      plugin.game().leave(e.getPlayer(), true);
      e.setCancelled(true);
    }
  }
}
