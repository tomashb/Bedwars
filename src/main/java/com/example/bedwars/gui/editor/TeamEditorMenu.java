package com.example.bedwars.gui.editor;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.gui.BWMenuHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class TeamEditorMenu {
  private final BedwarsPlugin plugin;
  private static final int BASE_TEAM = 10;
  private static final int BASE_SPAWN = 19;
  private static final int BASE_BED = 28;
  public static final int SLOT_BACK = 49;

  public TeamEditorMenu(BedwarsPlugin plugin){ this.plugin = plugin; }

  public void open(Player p, String arenaId){
    Arena a = plugin.arenas().get(arenaId).orElseThrow();
    String title = plugin.messages().get("editor.team-title").replace("{arena}", arenaId);
    Inventory inv = Bukkit.createInventory(new BWMenuHolder(EditorView.TEAM, arenaId), 54, title);
    TeamColor[] colors = TeamColor.values();
    for(int i=0;i<colors.length;i++){
      TeamColor c = colors[i];
      boolean enabled = a.enabledTeams().contains(c);
      inv.setItem(BASE_TEAM+i, icon(c.wool, c.display, enabled));
      boolean hasSpawn = a.team(c).spawn()!=null;
      inv.setItem(BASE_SPAWN+i, icon(Material.COMPASS, "Spawn "+c.display, hasSpawn));
      boolean hasBed = a.team(c).bedBlock()!=null;
      inv.setItem(BASE_BED+i, icon(Material.RED_BED, "Lit "+c.display, hasBed));
    }
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
