package com.example.bedwars.lobby;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.arena.TeamData;
import com.example.bedwars.game.PlayerContextService;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Handles lobby items interactions. */
public final class LobbyListener implements Listener {
  private final BedwarsPlugin plugin;
  private final LobbyItemsService items;
  private final PlayerContextService ctx;

  public LobbyListener(BedwarsPlugin plugin, LobbyItemsService items, PlayerContextService ctx) {
    this.plugin = plugin; this.items = items; this.ctx = ctx;
  }

  @EventHandler(ignoreCancelled = true)
  public void onInteract(PlayerInteractEvent e) {
    ItemStack it = e.getItem();
    if (it == null) return;
    if (items.isTeamSelector(it)) {
      openTeamSelectMenu(e.getPlayer());
      e.setCancelled(true);
    } else if (items.isLeaveItem(it)) {
      plugin.game().leave(e.getPlayer(), true);
      e.setCancelled(true);
    }
  }

  private void openTeamSelectMenu(Player p) {
    String arenaId = ctx.getArena(p);
    if (arenaId == null) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null) return;
    Inventory inv = Bukkit.createInventory(null, 9, plugin.messages().get("game.choose-team-title"));
    int i = 0;
    for (TeamColor tc : TeamColor.values()) {
      ItemStack icon;
      if (!a.enabledTeams().contains(tc)) {
        icon = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
      } else {
        TeamData td = a.team(tc);
        int count = ctx.countPlayers(arenaId, tc);
        if (count >= td.maxPlayers()) {
          icon = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        } else {
          icon = new ItemStack(tc.wool);
        }
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
          meta.setDisplayName(tc.color + tc.display);
          List<String> lore = new ArrayList<>();
          lore.add("§7" + count + "/" + td.maxPlayers() + " joueurs");
          meta.setLore(lore);
          icon.setItemMeta(meta);
        }
      }
      inv.setItem(i++, icon);
    }
    p.openInventory(inv);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player p)) return;
    if (!plugin.messages().get("game.choose-team-title").equals(e.getView().getTitle())) return;
    e.setCancelled(true);
    ItemStack clicked = e.getCurrentItem();
    if (clicked == null) return;
    String arenaId = ctx.getArena(p);
    if (arenaId == null) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null) return;
    for (TeamColor tc : TeamColor.values()) {
      if (clicked.getType() == tc.wool) {
        TeamData td = a.team(tc);
        int count = ctx.countPlayers(arenaId, tc);
        if (count >= td.maxPlayers()) return; // full
        ctx.setTeam(p, tc);
        p.sendMessage(plugin.messages().get("prefix") +
            plugin.messages().format("game.assign-team", java.util.Map.of("team", tc.display)));
        p.closeInventory();
        return;
      }
    }
  }
}
