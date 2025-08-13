package com.example.bedwars.shop;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.arena.TeamData;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * GUI for team upgrades purchased with diamonds.
 */
public final class TeamUpgradesMenu {
  private final BedwarsPlugin plugin;

  public static final int SLOT_SHARP = 10;
  public static final int SLOT_PROT = 12;
  public static final int SLOT_HASTE = 14;
  public static final int SLOT_HEAL = 16;
  public static final int SLOT_FORGE = 28;
  public static final int SLOT_TRAP = 30;

  public TeamUpgradesMenu(BedwarsPlugin plugin){ this.plugin = plugin; }

  public void open(Player p, String arenaId, TeamColor team) {
    String title = plugin.messages().get("shop.upgrades-title");
    Inventory inv = Bukkit.createInventory(new Holder(arenaId, team), 54, title);
    TeamData td = plugin.arenas().get(arenaId).map(a->a.team(team)).orElse(null);
    TeamUpgradesState st = td != null ? td.upgrades() : new TeamUpgradesState();

    inv.setItem(SLOT_SHARP, icon(Material.DIAMOND_SWORD, UpgradeType.SHARPNESS, st.sharpness()?1:0, p));
    inv.setItem(SLOT_PROT, icon(Material.DIAMOND_BOOTS, UpgradeType.PROTECTION, st.protection(), p));
    inv.setItem(SLOT_HASTE, icon(Material.GOLDEN_PICKAXE, UpgradeType.MANIC_MINER, st.manicMiner(), p));
    inv.setItem(SLOT_HEAL, icon(Material.BEACON, UpgradeType.HEAL_POOL, st.healPool()?1:0, p));
    inv.setItem(SLOT_FORGE, icon(Material.FURNACE, UpgradeType.FORGE, st.forge(), p));
    inv.setItem(SLOT_TRAP, trapIcon(st, p));

    p.openInventory(inv);
  }

  private ItemStack icon(Material mat, UpgradeType type, int level, Player p) {
    ShopConfig.UpgradeDef def = plugin.shopConfig().upgrade(type);
    ItemStack it = new ItemStack(mat);
    ItemMeta im = it.getItemMeta();
    if (im != null && def != null) {
      im.setDisplayName(ChatColor.AQUA + type.name() + " " + level + "/" + def.maxLevel);
      if (level < def.maxLevel) {
        Map<Material,Integer> costMap = def.costs.get(Math.min(level, def.costs.size()-1));
        int cost = costMap.getOrDefault(Material.DIAMOND, 0);
        int have = plugin.upgrades().countDiamonds(p);
        ChatColor col = have >= cost ? ChatColor.GRAY : ChatColor.RED;
        im.setLore(java.util.List.of(col + "Coût : ♦ " + cost + " Diamant(s)"));
      } else {
        im.setLore(java.util.List.of(ChatColor.GRAY + plugin.messages().get("shop.maxed")));
      }
      it.setItemMeta(im);
    }
    return it;
  }

  private ItemStack trapIcon(TeamUpgradesState st, Player p) {
    ShopConfig.TrapDef def = plugin.shopConfig().trap(TrapType.ALARM);
    ItemStack it = new ItemStack(Material.TRIPWIRE_HOOK);
    ItemMeta im = it.getItemMeta();
    if (im != null && def != null) {
      im.setDisplayName(ChatColor.LIGHT_PURPLE + "Traps " + st.trapQueue().size() + "/3");
      int cost = def.cost.getOrDefault(Material.DIAMOND, 0);
      int have = plugin.upgrades().countDiamonds(p);
      ChatColor col = have >= cost ? ChatColor.GRAY : ChatColor.RED;
      im.setLore(java.util.List.of(col + "Coût : ♦ " + cost + " Diamant(s)"));
      it.setItemMeta(im);
    }
    return it;
  }

  static final class Holder implements InventoryHolder {
    final String arenaId; final TeamColor team;
    Holder(String arenaId, TeamColor team){ this.arenaId = arenaId; this.team = team; }
    @Override public Inventory getInventory(){ return null; }
  }
}
