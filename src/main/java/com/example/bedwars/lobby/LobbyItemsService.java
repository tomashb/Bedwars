package com.example.bedwars.lobby;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Provides lobby items like team selector and leave item. */
public final class LobbyItemsService {
  private final BedwarsPlugin plugin;
  private final ItemStack teamSelector;
  private final ItemStack leaveItem;

  public LobbyItemsService(BedwarsPlugin plugin) {
    this.plugin = plugin;
    this.teamSelector = createItem("lobby.team_selector");
    this.leaveItem = createItem("lobby.leave_item");
  }

  private ItemStack createItem(String path) {
    String mat = plugin.getConfig().getString(path + ".material", "COMPASS");
    Material m = Material.matchMaterial(mat);
    if (m == null) m = Material.COMPASS;
    String name = ChatColor.translateAlternateColorCodes('&',
        plugin.getConfig().getString(path + ".name", ""));
    ItemStack it = new ItemStack(m);
    ItemMeta meta = it.getItemMeta();
    if (meta != null) { meta.setDisplayName(name); it.setItemMeta(meta); }
    return it;
  }

  public void giveLobbyItems(Player p) {
    p.getInventory().clear();
    p.getInventory().setItem(0, teamSelector.clone());
    p.getInventory().setItem(8, leaveItem.clone());
  }

  public boolean isTeamSelector(ItemStack it) {
    return it != null && it.isSimilar(teamSelector);
  }

  public boolean isLeaveItem(ItemStack it) {
    return it != null && it.isSimilar(leaveItem);
  }
}
