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

public final class NpcEditorMenu {
  private final BedwarsPlugin plugin;
  public static final int SLOT_ADD_ITEM = 11;
  public static final int SLOT_ADD_UPGRADE = 15;
  public static final int SLOT_BACK = 49;

  public NpcEditorMenu(BedwarsPlugin plugin){ this.plugin = plugin; }

  public void open(Player p, String arenaId){
    String title = plugin.messages().get("editor.npc-title").replace("{arena}", arenaId);
    Inventory inv = Bukkit.createInventory(new BWMenuHolder(EditorView.NPC, arenaId), 54, title);
    inv.setItem(SLOT_ADD_ITEM, icon(Material.CHEST, "Ajouter PNJ Objets", false));
    inv.setItem(SLOT_ADD_UPGRADE, icon(Material.ANVIL, "Ajouter PNJ Améliorations", false));
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
