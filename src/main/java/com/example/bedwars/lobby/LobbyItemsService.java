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
  private final int teamSelectorSlot;
  private final ItemStack leaveItem;
  private final int leaveItemSlot;
  private final boolean leaveLocked;

  public LobbyItemsService(BedwarsPlugin plugin) {
    this.plugin = plugin;
    this.teamSelector = createItem("items.team_selector");
    this.teamSelectorSlot = plugin.getConfig().getInt("items.team_selector.slot", 0);
    this.leaveItem = createLeaveItem();
    this.leaveItemSlot = plugin.getConfig().getInt("items.leave.slot", 8);
    this.leaveLocked = plugin.getConfig().getBoolean("items.leave.lock", true);
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

  private ItemStack createLeaveItem() {
    String mat = plugin.getConfig().getString("items.leave.material", "DARK_OAK_DOOR");
    Material m = Material.matchMaterial(mat);
    if (m == null) m = Material.DARK_OAK_DOOR;
    String name = ChatColor.translateAlternateColorCodes('&',
        plugin.getConfig().getString("items.leave.name", ""));
    java.util.List<String> lore = plugin.getConfig().getStringList("items.leave.lore");
    ItemStack it = new ItemStack(m);
    ItemMeta meta = it.getItemMeta();
    if (meta != null) {
      meta.setDisplayName(name);
      if (!lore.isEmpty()) meta.setLore(lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).toList());
      meta.getPersistentDataContainer().set(plugin.keys().BW_ITEM(), org.bukkit.persistence.PersistentDataType.STRING, "leave");
      it.setItemMeta(meta);
    }
    return it;
  }

  public void giveLobbyItems(Player p) {
    p.getInventory().clear();
    p.getInventory().setItem(teamSelectorSlot, teamSelector.clone());
    p.getInventory().setItem(leaveItemSlot, leaveItem.clone());
  }

  public boolean isTeamSelector(ItemStack it) {
    return it != null && it.isSimilar(teamSelector);
  }

  public boolean isLeaveItem(ItemStack it) {
    if (it == null) return false;
    ItemMeta meta = it.getItemMeta();
    if (meta == null) return false;
    var pdc = meta.getPersistentDataContainer();
    var key = plugin.keys().BW_ITEM();
    return pdc.has(key, org.bukkit.persistence.PersistentDataType.STRING)
        && "leave".equals(pdc.get(key, org.bukkit.persistence.PersistentDataType.STRING));
  }

  public boolean leaveLocked() { return leaveLocked; }

  public ItemStack teamSelectorItem() { return teamSelector; }
  public ItemStack leaveItem() { return leaveItem; }
}
