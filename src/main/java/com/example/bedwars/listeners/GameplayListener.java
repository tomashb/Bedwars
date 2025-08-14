package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.game.PlayerContextService;
import com.example.bedwars.lobby.LobbyItemsService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

/** Misc gameplay restrictions: sword drop and hunger. */
public final class GameplayListener implements Listener {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;
  private final LobbyItemsService items;

  public GameplayListener(BedwarsPlugin plugin, PlayerContextService ctx, LobbyItemsService items) {
    this.plugin = plugin;
    this.ctx = ctx;
    this.items = items;
  }

  private boolean noDropSwords() {
    return plugin.getConfig().getBoolean("anti_drop.swords", true);
  }

  private boolean noDropSelector() {
    return plugin.getConfig().getBoolean("anti_drop.selector_item", true);
  }

  private boolean noDropLeave() {
    return plugin.getConfig().getBoolean("anti_drop.leave_item", true);
  }

  private boolean noHunger() {
    return plugin.getConfig().getBoolean("gameplay.no_hunger", true);
  }

  private static boolean isSword(Material m) {
    return m != null && m.name().endsWith("_SWORD");
  }

  @EventHandler(ignoreCancelled = true)
  public void onDrop(PlayerDropItemEvent e) {
    Player p = e.getPlayer();
    if (ctx.getArena(p) == null) return;
    ItemStack drop = e.getItemDrop().getItemStack();
    Material m = drop.getType();
    if (noDropSwords() && isSword(m)) {
      e.setCancelled(true);
      plugin.messages().send(p, "errors.cannot_drop_sword");
      return;
    }
    if ((noDropSelector() && items.isTeamSelector(drop)) || (noDropLeave() && items.isLeaveItem(drop))) {
      e.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onInvClick(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player p)) return;
    if (ctx.getArena(p) == null) return;
    if (e.getClick() == ClickType.DROP || e.getClick() == ClickType.CONTROL_DROP) {
      ItemStack cur = e.getCurrentItem();
      if (cur == null) return;
      Material m = cur.getType();
      if (noDropSwords() && isSword(m)) {
        e.setCancelled(true);
        plugin.messages().send(p, "errors.cannot_drop_sword");
        return;
      }
      if ((noDropSelector() && items.isTeamSelector(cur)) || (noDropLeave() && items.isLeaveItem(cur))) {
        e.setCancelled(true);
      }
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onFood(FoodLevelChangeEvent e) {
    if (!noHunger()) return;
    if (!(e.getEntity() instanceof Player p)) return;
    if (ctx.getArena(p) == null) return;
    e.setCancelled(true);
    p.setFoodLevel(20);
    p.setSaturation(20f);
  }
}

