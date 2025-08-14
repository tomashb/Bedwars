package com.example.bedwars.shop;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.arena.TeamData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Menu listing all available traps to purchase.
 */
public final class TrapsCatalogueMenu {
  private final BedwarsPlugin plugin;
  public static final int SLOT_CLOSE = 26;

  public TrapsCatalogueMenu(BedwarsPlugin plugin) { this.plugin = plugin; }

  public void open(Player p, String arenaId, TeamColor team) {
    String title = plugin.messages().get("menu.traps_title");
    Inventory inv = Bukkit.createInventory(new Holder(arenaId, team), 27, title);
    TeamData td = plugin.arenas().get(arenaId).map(a->a.team(team)).orElse(null);
    TeamUpgradesState st = td != null ? td.upgrades() : new TeamUpgradesState();

    ItemStack filler = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
    ItemMeta fm = filler.getItemMeta();
    if (fm != null) { fm.setDisplayName(" "); filler.setItemMeta(fm); }
    for (int i = 0; i < 27; i++) inv.setItem(i, filler);

    int slot = 10;
    for (TrapType t : TrapType.values()) {
      UpgradeService.TrapDef def = plugin.upgrades().trapDef(t);
      if (def == null) continue;
      ItemStack it = new ItemStack(def.icon);
      ItemMeta im = it.getItemMeta();
      if (im != null) {
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', def.name));
        im.setLore(java.util.List.of(ChatColor.GRAY + "Coût : " + def.cost + "◆"));
        it.setItemMeta(im);
      }
      inv.setItem(slot, it);
      slot += 2;
    }

    ItemStack close = new ItemStack(Material.BARRIER);
    ItemMeta cm = close.getItemMeta();
    if (cm != null) { cm.setDisplayName(ChatColor.RED + plugin.messages().get("generic.reloaded")); close.setItemMeta(cm); }
    inv.setItem(SLOT_CLOSE, close);

    p.openInventory(inv);
  }

  static final class Holder implements InventoryHolder {
    final String arenaId; final TeamColor team;
    Holder(String arenaId, TeamColor team){ this.arenaId = arenaId; this.team = team; }
    @Override public Inventory getInventory(){ return null; }
  }
}
