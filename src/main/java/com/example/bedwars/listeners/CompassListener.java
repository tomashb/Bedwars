package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.game.PlayerContextService;
import com.example.bedwars.gui.RulesMenu;
import com.example.bedwars.gui.TeamSelectMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/** Handles compass interactions for rules/team selection menus. */
public final class CompassListener implements Listener {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;
  private final TeamSelectMenu teamMenu;
  private final RulesMenu rulesMenu;

  public CompassListener(BedwarsPlugin plugin, PlayerContextService ctx, TeamSelectMenu teamMenu) {
    this.plugin = plugin; this.ctx = ctx; this.teamMenu = teamMenu; this.rulesMenu = new RulesMenu(plugin);
  }

  @EventHandler(ignoreCancelled = true)
  public void onCompassUse(PlayerInteractEvent e) {
    if (e.getHand() != EquipmentSlot.HAND) return;
    ItemStack it = e.getItem();
    if (it == null || !it.hasItemMeta()) return;
    var pdc = it.getItemMeta().getPersistentDataContainer();
    String kind = pdc.get(plugin.keys().BW_ITEM(), PersistentDataType.STRING);
    if (!"compass".equals(kind)) return;
    Action action = e.getAction();
    if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
    Player p = e.getPlayer();
    if (plugin.getConfig().getBoolean("compass.require_permission", false)
        && !p.hasPermission("bedwars.menu.rules")) return;
    String mode = plugin.getConfig().getString("compass.opens", "rules");
    if ("team_selector".equalsIgnoreCase(mode)) {
      String arenaId = ctx.getArena(p);
      if (arenaId != null) {
        Arena a = plugin.arenas().get(arenaId).orElse(null);
        if (a != null) teamMenu.open(p, a);
      }
    } else {
      rulesMenu.open(p);
    }
    e.setCancelled(true);
  }
}
