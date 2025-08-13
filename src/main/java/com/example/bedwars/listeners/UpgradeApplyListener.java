package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamData;
import com.example.bedwars.service.PlayerContextService;
import com.example.bedwars.shop.TeamUpgradesState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

public final class UpgradeApplyListener implements Listener {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;

  public UpgradeApplyListener(BedwarsPlugin plugin, PlayerContextService ctx) {
    this.plugin = plugin;
    this.ctx = ctx;
  }

  @EventHandler
  public void onSwitch(PlayerItemHeldEvent e) {
    Player p = e.getPlayer();
    ctx.get(p).ifPresent(c -> plugin.upgrades().applySharpness(c.arenaId, c.team));
  }

  @EventHandler
  public void onInventory(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player p)) return;
    ctx.get(p).ifPresent(c -> {
      plugin.arenas().get(c.arenaId).ifPresent(arena -> {
        TeamData td = arena.team(c.team);
        TeamUpgradesState st = td.upgrades();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
          plugin.upgrades().applySharpness(c.arenaId, c.team);
          plugin.upgrades().applyProtection(c.arenaId, c.team, st.protection());
        });
      });
    });
  }
}
