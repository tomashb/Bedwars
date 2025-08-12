package com.example.bedwars.gui.editor;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.gui.BWMenuHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ArenaEditorMenu {
  private final BedwarsPlugin plugin;
  public static final int SLOT_LOBBY = 10;
  public static final int SLOT_TEAMS = 12;
  public static final int SLOT_NPC = 14;
  public static final int SLOT_GENS = 16;
  public static final int SLOT_SAVE = 28;
  public static final int SLOT_RELOAD = 30;
  public static final int SLOT_DELETE = 32;
  public static final int SLOT_BACK = 49;

  public ArenaEditorMenu(BedwarsPlugin plugin){ this.plugin = plugin; }

  public void open(Player p, String arenaId){
    Arena a = plugin.arenas().get(arenaId).orElseThrow();
    String title = plugin.messages().get("editor.title").replace("{arena}", arenaId);
    Inventory inv = Bukkit.createInventory(new BWMenuHolder(EditorView.ARENA, arenaId), 54, title);
    inv.setItem(SLOT_LOBBY, icon(Material.PAPER, "Set Lobby", a.lobby()!=null));
    inv.setItem(SLOT_TEAMS, icon(Material.WHITE_WOOL, "Équipes", false));
    inv.setItem(SLOT_NPC, icon(Material.EMERALD, "PNJ", !a.npcs().isEmpty()));
    inv.setItem(SLOT_GENS, icon(Material.HOPPER, "Générateurs", !a.generators().isEmpty()));
    inv.setItem(SLOT_SAVE, icon(Material.ANVIL, "Sauver", false));
    inv.setItem(SLOT_RELOAD, icon(Material.BOOK, "Recharger", false));
    inv.setItem(SLOT_DELETE, icon(Material.BARRIER, "Supprimer", false));
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
