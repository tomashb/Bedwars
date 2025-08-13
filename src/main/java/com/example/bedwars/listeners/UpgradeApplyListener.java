package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamData;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.game.PlayerContextService;
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
    String ar = ctx.getArena(p);
    TeamColor tm = ctx.getTeam(p);
    if (ar == null || tm == null) return;
    plugin.arenas().get(ar).ifPresent(arena -> {
      TeamData td = arena.team(tm);
      if (td.upgrades().sharpness()) plugin.upgrades().applySharpness(ar, tm);
    });
  }

  @EventHandler
  public void onInventory(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player p)) return;
    String ar = ctx.getArena(p);
    TeamColor tm = ctx.getTeam(p);
    if (ar == null || tm == null) return;
    plugin.arenas().get(ar).ifPresent(arena -> {
      TeamData td = arena.team(tm);
      TeamUpgradesState st = td.upgrades();
      plugin.getServer().getScheduler().runTask(plugin, () -> {
        if (st.sharpness()) plugin.upgrades().applySharpness(ar, tm);
        plugin.upgrades().applyProtection(ar, tm, st.protection());
      });
    });
  }
}
