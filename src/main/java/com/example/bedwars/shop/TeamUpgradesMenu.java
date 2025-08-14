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

import com.example.bedwars.shop.TeamUpgradesState;
import com.example.bedwars.shop.UpgradeType;
import com.example.bedwars.shop.TrapType;

/**
 * GUI for team upgrades purchased with diamonds.
 */
public final class TeamUpgradesMenu {
  private final BedwarsPlugin plugin;

  public static final int SLOT_SHARP = 10;
  public static final int SLOT_PROT = 12;
  public static final int SLOT_HASTE = 14;
  public static final int SLOT_TRAP_SHORTCUT = 16;
  public static final int SLOT_FORGE = 19;
  public static final int SLOT_HEAL = 21;
  public static final int SLOT_TRAP_OPEN = 18;
  public static final int SLOT_TRAP1 = 20;
  public static final int SLOT_TRAP2 = 22;
  public static final int SLOT_TRAP3 = 24;
  public static final int SLOT_CLOSE = 26;

  public TeamUpgradesMenu(BedwarsPlugin plugin){ this.plugin = plugin; }

  public void open(Player p, String arenaId, TeamColor team) {
    String title = plugin.messages().get("menu.upgrades_title");
    Inventory inv = Bukkit.createInventory(new Holder(arenaId, team), 27, title);
    TeamData td = plugin.arenas().get(arenaId).map(a->a.team(team)).orElse(null);
    TeamUpgradesState st = td != null ? td.upgrades() : new TeamUpgradesState();

    ItemStack filler = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
    ItemMeta fm = filler.getItemMeta();
    if (fm != null) { fm.setDisplayName(" "); filler.setItemMeta(fm); }
    for (int i = 0; i < 9; i++) inv.setItem(i, filler);

    inv.setItem(SLOT_SHARP, icon(Material.IRON_SWORD, UpgradeType.SHARPNESS, st.sharpness()?1:0, p));
    inv.setItem(SLOT_PROT, icon(Material.IRON_CHESTPLATE, UpgradeType.PROTECTION, st.protection(), p));
    inv.setItem(SLOT_HASTE, icon(Material.GOLDEN_PICKAXE, UpgradeType.MANIC_MINER, st.manicMiner(), p));
    inv.setItem(SLOT_TRAP_SHORTCUT, trapShortcutIcon(p));

    inv.setItem(SLOT_FORGE, icon(Material.FURNACE, UpgradeType.FORGE, st.forge(), p));
    inv.setItem(SLOT_HEAL, icon(Material.BEACON, UpgradeType.HEAL_POOL, st.healPool()?1:0, p));

    inv.setItem(SLOT_TRAP_OPEN, trapOpenIcon(st, p));
    TrapType[] q = st.trapQueue().toArray(new TrapType[0]);
    inv.setItem(SLOT_TRAP1, trapQueueIcon(q, 0));
    inv.setItem(SLOT_TRAP2, trapQueueIcon(q, 1));
    inv.setItem(SLOT_TRAP3, trapQueueIcon(q, 2));
    inv.setItem(SLOT_CLOSE, closeIcon());

    p.openInventory(inv);
  }

  private ItemStack icon(Material mat, UpgradeType type, int level, Player p) {
    UpgradeService.UpgradeDef def = plugin.upgrades().def(type);
    ItemStack it = new ItemStack(mat);
    ItemMeta im = it.getItemMeta();
    if (im != null && def != null) {
      im.setDisplayName(ChatColor.AQUA + type.name() + " " + level + "/" + def.maxLevel());
      if (level < def.maxLevel()) {
        int cost = def.costForLevel(level + 1);
        int have = plugin.upgrades().countDiamonds(p);
        ChatColor col = have >= cost ? ChatColor.GRAY : ChatColor.RED;
        im.setLore(java.util.List.of(col + "Coût : " + cost + "◆"));
      } else {
        im.setLore(java.util.List.of(ChatColor.GRAY + plugin.messages().get("shop.maxed")));
      }
      it.setItemMeta(im);
    }
    return it;
  }

  private ItemStack trapOpenIcon(TeamUpgradesState st, Player p) {
    ItemStack it = new ItemStack(Material.TRIPWIRE_HOOK);
    ItemMeta im = it.getItemMeta();
    if (im != null) {
      im.setDisplayName(ChatColor.LIGHT_PURPLE + plugin.messages().get("menu.traps_title"));
      int cost = plugin.upgrades().trapCost(st.trapQueue().size());
      String line1 = ChatColor.GRAY + plugin.messages().get("shop.trap-added")
          .replace("{count}", String.valueOf(st.trapQueue().size()))
          .replace("{max}", String.valueOf(plugin.upgrades().trapSlots()));
      String line2 = ChatColor.DARK_GRAY + plugin.messages().format("traps.next_cost", java.util.Map.of("cost", cost));
      im.setLore(java.util.List.of(line1, line2));
      it.setItemMeta(im);
    }
    return it;
  }

  private ItemStack trapShortcutIcon(Player p) {
    ItemStack it = new ItemStack(Material.REDSTONE_TORCH);
    ItemMeta im = it.getItemMeta();
    if (im != null) {
      im.setDisplayName(ChatColor.LIGHT_PURPLE + plugin.messages().get("menu.traps_title"));
      it.setItemMeta(im);
    }
    return it;
  }

  private ItemStack trapQueueIcon(TrapType[] q, int idx) {
    if (idx < q.length) {
      TrapType t = q[idx];
      UpgradeService.TrapDef def = plugin.upgrades().trapDef(t);
      Material mat = def != null ? def.icon : Material.PAPER;
      ItemStack it = new ItemStack(mat);
      ItemMeta im = it.getItemMeta();
      if (im != null && def != null) { im.setDisplayName(ChatColor.WHITE + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', def.name))); it.setItemMeta(im); }
      return it;
    }
    ItemStack empty = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
    ItemMeta em = empty.getItemMeta();
    if (em != null) { em.setDisplayName(" "); empty.setItemMeta(em); }
    return empty;
  }

  private ItemStack closeIcon() {
    ItemStack it = new ItemStack(Material.BARRIER);
    ItemMeta im = it.getItemMeta();
    if (im != null) { im.setDisplayName(ChatColor.RED + plugin.messages().get("generic.reloaded")); it.setItemMeta(im); }
    return it;
  }

  static final class Holder implements InventoryHolder {
    final String arenaId; final TeamColor team;
    Holder(String arenaId, TeamColor team){ this.arenaId = arenaId; this.team = team; }
    @Override public Inventory getInventory(){ return null; }
  }
}
