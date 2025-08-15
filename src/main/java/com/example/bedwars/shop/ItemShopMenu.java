package com.example.bedwars.shop;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamColor;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
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
 * GUI for buying consumable/equipment items.
 */
public final class ItemShopMenu {
  private final BedwarsPlugin plugin;
  private static final Map<ShopCategory, Material> ICONS = new EnumMap<>(ShopCategory.class);
  private static final int GRID_ROWS = 4, GRID_COLS = 7, GRID_ORIGIN = 19, ROW_STRIDE = 9;
  private static int slotAt(int r, int c){ return GRID_ORIGIN + r*ROW_STRIDE + c; }
  static {
    ICONS.put(ShopCategory.BLOCKS, Material.CLAY);
    ICONS.put(ShopCategory.MELEE, Material.IRON_SWORD);
    ICONS.put(ShopCategory.ARMOR, Material.IRON_CHESTPLATE);
    ICONS.put(ShopCategory.TOOLS, Material.STONE_PICKAXE);
    ICONS.put(ShopCategory.RANGED, Material.BOW);
    ICONS.put(ShopCategory.POTIONS, Material.BREWING_STAND);
    ICONS.put(ShopCategory.UTILITY, Material.TNT);
  }

  public ItemShopMenu(BedwarsPlugin plugin) { this.plugin = plugin; }

  public void open(Player p, String arenaId, TeamColor team, ShopCategory cat) {
    String title = plugin.messages().format("shop.title", Map.of("cat", cat.name()));
    Inventory inv = Bukkit.createInventory(new Holder(arenaId, team, cat), 54, title);
    com.example.bedwars.gui.GuiFactory.fillBorder(inv, Material.GRAY_STAINED_GLASS_PANE);

    // categories bar starts at slot 1 leaving border panes at 0 and 8
    int i = 1;
    for (ShopCategory c : ShopCategory.values()) {
      ItemStack it = new ItemStack(ICONS.getOrDefault(c, Material.BARRIER));
      ItemMeta im = it.getItemMeta();
      if (im != null) {
        im.setDisplayName(ChatColor.YELLOW + c.name());
        it.setItemMeta(im);
      }
      inv.setItem(i++, it);
    }

    List<ItemStack> items = new ArrayList<>();
    if (cat == ShopCategory.TOOLS) {
      items.add(plugin.tools().createPickaxeIcon(p));
      items.add(plugin.tools().createAxeIcon(p));
    }
    for (ShopItem si : plugin.shopConfig().items(cat)) {
      ItemStack it;
      if (si.mat == Material.POTION) {
        it = PotionUtil.makeTaggedPotion(plugin, si.id, si.name);
      } else {
        Material mat = si.teamColored ? team.wool : si.mat;
        it = new ItemStack(mat, si.amount);
      }
      it.setAmount(si.amount);
      ItemMeta im = it.getItemMeta();
      if (im != null) {
        String name = si.name.replace("{team}", team.display);
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        List<String> lore = new ArrayList<>();
        lore.add("§7Coût: " + si.cost + " " + displayCurrency(si.currency));
        im.setLore(lore);
        si.enchants.forEach((e,l) -> im.addEnchant(e, l, true));
        it.setItemMeta(im);
      }
      items.add(it);
    }
    for (int idx = 0; idx < items.size() && idx < GRID_ROWS*GRID_COLS; idx++) {
      int r = idx / GRID_COLS;
      int c = idx % GRID_COLS;
      inv.setItem(slotAt(r,c), items.get(idx));
    }

    p.openInventory(inv);
  }

  private static String displayCurrency(Currency c) {
    return switch (c) {
      case IRON -> "§fFer";
      case GOLD -> "§6Or";
      case EMERALD -> "§aÉmeraudes";
      case DIAMOND -> "§bDiamants";
    };
  }

  static final class Holder implements InventoryHolder {
    final String arenaId;
    final TeamColor team;
    final ShopCategory cat;
    Holder(String arenaId, TeamColor team, ShopCategory cat){ this.arenaId = arenaId; this.team = team; this.cat = cat; }
    @Override public Inventory getInventory(){ return null; }
  }
}

