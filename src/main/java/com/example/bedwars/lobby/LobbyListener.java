package com.example.bedwars.lobby;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.game.GameService;
import com.example.bedwars.game.PlayerContextService;
import com.example.bedwars.gui.TeamSelectMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/** Handles lobby items interactions. */
public final class LobbyListener implements Listener {
  private final BedwarsPlugin plugin;
  private final LobbyItemsService items;
  private final TeamSelectMenu teamSelectMenu;
  private final PlayerContextService contexts;
  private final GameService gameService;

  public LobbyListener(BedwarsPlugin plugin, LobbyItemsService items, TeamSelectMenu menu,
                       PlayerContextService contexts, GameService gameService) {
    this.plugin = plugin;
    this.items = items;
    this.teamSelectMenu = menu;
    this.contexts = contexts;
    this.gameService = gameService;
  }

  @EventHandler(ignoreCancelled = true)
  public void onCompassUse(PlayerInteractEvent e) {
    ItemStack it = e.getItem();
    if (it == null || !items.isTeamSelector(it)) return;
    if (e.getHand() != EquipmentSlot.HAND) return;
    Action action = e.getAction();
    if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
    Player p = e.getPlayer();
    String arenaId = contexts.getArena(p);
    if (arenaId == null) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null) return;
    teamSelectMenu.open(p, a);
    e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void onLeaveItemUse(PlayerInteractEvent e) {
    ItemStack it = e.getItem();
    if (it == null || !items.isLeaveItem(it)) return;
    if (e.getHand() != EquipmentSlot.HAND) return;
    Action action = e.getAction();
    if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
    Player p = e.getPlayer();
    if (contexts.getArena(p) == null) return;
    gameService.leave(p, true);
    e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void onDrop(PlayerDropItemEvent e) {
    Player p = e.getPlayer();
    String arenaId = contexts.getArena(p);
    if (arenaId == null) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null || (a.state() != GameState.WAITING && a.state() != GameState.STARTING)) return;
    ItemStack it = e.getItemDrop().getItemStack();
    if (items.isTeamSelector(it) || items.isLeaveItem(it)) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player p)) return;
    String arenaId = contexts.getArena(p);
    if (arenaId == null) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null || (a.state() != GameState.WAITING && a.state() != GameState.STARTING)) return;
    ItemStack it = e.getCurrentItem();
    if (items.isTeamSelector(it) || items.isLeaveItem(it)) e.setCancelled(true);
  }
}
