package com.example.bedwars.shop;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.arena.TeamData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import com.example.bedwars.shop.TeamUpgradesState;
import com.example.bedwars.shop.UpgradeType;
import com.example.bedwars.shop.TrapType;
import java.util.Map;

/**
 * GUI for team upgrades purchased with diamonds.
 */
public final class UpgradesGui {
  private final BedwarsPlugin plugin;
  private static final Enchantment GLOW_ENCH =
      Enchantment.getByKey(NamespacedKey.minecraft("power")) != null
          ? Enchantment.getByKey(NamespacedKey.minecraft("power"))
          : Enchantment.DURABILITY;

  // layout slots according to spec
  public static final int SLOT_DIAMOND_COUNTER = 8;
  public static final int SLOT_SHARP = 10;
  public static final int SLOT_PROT = 11;
  public static final int SLOT_HASTE = 12;
  public static final int SLOT_FORGE = 13;
  public static final int SLOT_HEAL = 14;
  public static final int SLOT_TRAP1 = 19;
  public static final int SLOT_TRAP2 = 20;
  public static final int SLOT_TRAP3 = 21;
  public static final int SLOT_TRAP_MF = 22;
  public static final int SLOT_TRAP_BS = 23;
  public static final int SLOT_BACK = 24;

  public UpgradesGui(BedwarsPlugin plugin){ this.plugin = plugin; }

  public void open(Player p, String arenaId, TeamColor team) {
    String title = plugin.messages().get("upgrades.title");
    Inventory inv = Bukkit.createInventory(new Holder(arenaId, team), 27, title);
    TeamData td = plugin.arenas().get(arenaId).map(a->a.team(team)).orElse(null);
    TeamUpgradesState st = td != null ? td.upgrades() : new TeamUpgradesState();
    int teamSize = td != null ? td.maxPlayers() : 2;

    ItemStack filler = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
    ItemMeta fm = filler.getItemMeta();
    if (fm != null) { fm.setDisplayName(" "); filler.setItemMeta(fm); }
    for (int i = 0; i < 27; i++) inv.setItem(i, filler);

    // place diamond counter
    inv.setItem(SLOT_DIAMOND_COUNTER, diamondsIcon(p));

    // upgrades
    inv.setItem(SLOT_SHARP, icon(Material.IRON_SWORD, UpgradeType.SHARPNESS, st.sharpness()?1:0, teamSize, p));
    inv.setItem(SLOT_PROT, icon(Material.IRON_CHESTPLATE, UpgradeType.PROTECTION, st.protection(), teamSize, p));
    inv.setItem(SLOT_HASTE, icon(Material.GOLDEN_PICKAXE, UpgradeType.MANIC_MINER, st.manicMiner(), teamSize, p));
    inv.setItem(SLOT_FORGE, icon(Material.FURNACE, UpgradeType.FORGE, st.forge(), teamSize, p));
    inv.setItem(SLOT_HEAL, icon(Material.BEACON, UpgradeType.HEAL_POOL, st.healPool()?1:0, teamSize, p));

    // traps queue and purchase buttons
    TrapType[] q = st.trapQueue().toArray(new TrapType[0]);
    inv.setItem(SLOT_TRAP1, trapQueueIcon(q, 0));
    inv.setItem(SLOT_TRAP2, trapQueueIcon(q, 1));
    inv.setItem(SLOT_TRAP3, trapQueueIcon(q, 2));
    inv.setItem(SLOT_TRAP_MF, trapButton(TrapType.MINING_FATIGUE, st, p));
    inv.setItem(SLOT_TRAP_BS, trapButton(TrapType.BLIND_SLOW, st, p));

    inv.setItem(SLOT_BACK, backIcon());

    p.openInventory(inv);
  }

  private ItemStack icon(Material mat, UpgradeType type, int level, int teamSize, Player p) {
    UpgradesService.UpgradeDef def = plugin.upgrades().def(type);
    ItemStack it = new ItemStack(mat);
    ItemMeta im = it.getItemMeta();
    if (im != null && def != null) {
      im.setDisplayName(ChatColor.AQUA + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', def.name)));
      if (level < def.maxLevel()) {
        int cost = def.costForLevel(teamSize, level + 1);
        int have = plugin.upgrades().countDiamonds(p);
        String lore = (have >= cost ? ChatColor.GRAY : ChatColor.RED) + plugin.messages().format("upgrades.cost", Map.of("cost", cost));
        im.setLore(java.util.List.of(lore));
      } else {
        im.addEnchant(GLOW_ENCH, 1, true);
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        im.setLore(java.util.List.of(ChatColor.GREEN + plugin.messages().get("upgrades.max")));
      }
      it.setItemMeta(im);
    }
    return it;
  }

  private ItemStack trapButton(TrapType t, TeamUpgradesState st, Player p) {
    UpgradesService.TrapDef def = plugin.upgrades().trapDef(t);
    ItemStack it = new ItemStack(def != null ? def.icon : Material.PAPER);
    ItemMeta im = it.getItemMeta();
    if (im != null && def != null) {
      im.setDisplayName(ChatColor.AQUA + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', def.name)));
      int cost = plugin.upgrades().trapCost(st.trapQueue().size());
      int have = plugin.upgrades().countDiamonds(p);
      String lore = plugin.messages().format("upgrades.queue", Map.of("count", st.trapQueue().size(), "next_cost", cost));
      ChatColor col = have >= cost ? ChatColor.GRAY : ChatColor.RED;
      im.setLore(java.util.List.of(lore, col + plugin.messages().format("upgrades.cost", Map.of("cost", cost))));
      it.setItemMeta(im);
    }
    return it;
  }

  private ItemStack trapQueueIcon(TrapType[] q, int idx) {
    if (idx < q.length) {
      TrapType t = q[idx];
      UpgradesService.TrapDef def = plugin.upgrades().trapDef(t);
      Material mat = def != null ? def.icon : Material.PAPER;
      ItemStack it = new ItemStack(mat);
      ItemMeta im = it.getItemMeta();
      if (im != null && def != null) {
        im.setDisplayName(ChatColor.WHITE + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', def.name)));
        it.setItemMeta(im);
      }
      return it;
    }
    ItemStack empty = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
    ItemMeta em = empty.getItemMeta();
    if (em != null) { em.setDisplayName(" "); empty.setItemMeta(em); }
    return empty;
  }

  private ItemStack backIcon() {
    ItemStack it = new ItemStack(Material.BARRIER);
    ItemMeta im = it.getItemMeta();
    if (im != null) { im.setDisplayName(ChatColor.RED + plugin.messages().get("generic.back")); it.setItemMeta(im); }
    return it;
  }

  private ItemStack diamondsIcon(Player p) {
    ItemStack it = new ItemStack(Material.DIAMOND, Math.max(1, plugin.upgrades().countDiamonds(p)));
    ItemMeta im = it.getItemMeta();
    if (im != null) { im.setDisplayName(ChatColor.AQUA + String.valueOf(plugin.upgrades().countDiamonds(p))); it.setItemMeta(im); }
    return it;
  }

  static final class Holder implements InventoryHolder {
    final String arenaId; final TeamColor team;
    Holder(String arenaId, TeamColor team){ this.arenaId = arenaId; this.team = team; }
    @Override public Inventory getInventory(){ return null; }
  }
}
