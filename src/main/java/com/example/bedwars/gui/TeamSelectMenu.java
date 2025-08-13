package com.example.bedwars.gui;

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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** GUI for selecting a team before the game starts. */
public final class TeamSelectMenu implements Listener {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;

  public TeamSelectMenu(BedwarsPlugin plugin, PlayerContextService ctx) {
    this.plugin = plugin;
    this.ctx = ctx;
  }

  /** Opens the team selection menu for a player and arena. */
  public void open(Player p, Arena a) {
    if (a == null) return;
    Inventory inv = Bukkit.createInventory(null, 9, plugin.messages().get("game.choose-team-title"));
    int i = 0;
    for (TeamColor tc : a.activeTeams()) {
      int count = ctx.countPlayers(a.id(), tc);
      ItemStack icon;
      if (count >= a.maxTeamSize()) {
        icon = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
      } else {
        icon = new ItemStack(tc.wool);
      }
      ItemMeta meta = icon.getItemMeta();
      if (meta != null) {
        meta.setDisplayName(tc.color + tc.display);
        List<String> lore = new ArrayList<>();
        lore.add("§7Joueurs: " + count + "/" + a.maxTeamSize());
        meta.setLore(lore);
        icon.setItemMeta(meta);
      }
      inv.setItem(i++, icon);
    }
    p.openInventory(inv);
  }

  @EventHandler(ignoreCancelled = true)
  public void onClick(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player p)) return;
    if (!plugin.messages().get("game.choose-team-title").equals(e.getView().getTitle())) return;
    e.setCancelled(true);
    ItemStack clicked = e.getCurrentItem();
    if (clicked == null) return;
    String arenaId = ctx.getArena(p);
    if (arenaId == null) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null) return;
    for (TeamColor tc : a.activeTeams()) {
      if (clicked.getType() == tc.wool) {
        int count = ctx.countPlayers(arenaId, tc);
        if (count >= a.maxTeamSize()) {
          p.sendMessage(plugin.messages().format("errors.team_full", java.util.Map.of("count", count, "max", a.maxTeamSize())));
          return;
        }
        ctx.setTeam(p, tc);
        p.sendMessage(plugin.messages().get("prefix") +
            plugin.messages().format("team.chosen", java.util.Map.of("team", tc.display)));
        p.closeInventory();
        return;
      }
    }
  }
}
