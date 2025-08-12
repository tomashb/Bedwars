package com.example.bedwars.gui.editor;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.gui.BWMenuHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class GeneratorsEditorMenu {
  private final BedwarsPlugin plugin;
  public static final int SLOT_IRON = 10;
  public static final int SLOT_GOLD = 12;
  public static final int SLOT_DIAMOND = 14;
  public static final int SLOT_EMERALD = 16;
  public static final int SLOT_BACK = 49;

  public GeneratorsEditorMenu(BedwarsPlugin plugin){ this.plugin = plugin; }

  public void open(Player p, String arenaId){
    String title = plugin.messages().get("editor.gens-title").replace("{arena}", arenaId);
    Inventory inv = Bukkit.createInventory(new BWMenuHolder(EditorView.GEN, arenaId), 54, title);
    inv.setItem(SLOT_IRON, icon(Material.IRON_INGOT, "Fer", false));
    inv.setItem(SLOT_GOLD, icon(Material.GOLD_INGOT, "Or", false));
    inv.setItem(SLOT_DIAMOND, icon(Material.DIAMOND, "Diamant", false));
    inv.setItem(SLOT_EMERALD, icon(Material.EMERALD, "Émeraude", false));
    inv.setItem(SLOT_BACK, icon(Material.COMPASS, "Retour", false));
    p.openInventory(inv);
  }

  private ItemStack icon(Material mat, String name, boolean glint){
    ItemStack it = new ItemStack(mat);
    ItemMeta im = it.getItemMeta();
    if(im != null){
      im.setDisplayName(name);
      if(glint){
        im.addEnchant(org.bukkit.enchantments.Enchantment.INFINITY,1,true);
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
      }
      it.setItemMeta(im);
    }
    return it;
  }
}
