package com.example.bedwars.gui.placeholders;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.gui.AdminView;
import com.example.bedwars.gui.BWMenuHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a paginated list of arenas and allows navigation to their editors.
 */
public final class ArenasMenu {
  private final BedwarsPlugin plugin;
  public static final int SLOT_BACK = 49;
  public static final int SLOT_REFRESH = 53;

  public ArenasMenu(BedwarsPlugin plugin){ this.plugin = plugin; }

  public void open(Player p) {
    Inventory inv = Bukkit.createInventory(new BWMenuHolder(AdminView.ARENAS, null),
        54, plugin.messages().get("admin.root.arenas"));

    int[] starts = {10, 19, 28};
    int row = 0, col = 0;

    for (Arena a : plugin.arenas().all()) {
      int slot = starts[row] + col;
      inv.setItem(slot, arenaIcon(a));
      if (++col == 7) { col = 0; if (++row == starts.length) break; }
    }

    inv.setItem(SLOT_BACK, icon(Material.COMPASS, "&fRetour"));
    inv.setItem(SLOT_REFRESH, icon(Material.SUNFLOWER, "&eRafraîchir"));
    p.openInventory(inv);
  }

  private ItemStack arenaIcon(Arena a) {
    ItemStack it = new ItemStack(Material.MAP);
    ItemMeta im = it.getItemMeta();
    im.setDisplayName("§e" + a.id());
    List<String> lore = new ArrayList<>();
    lore.add("§7Monde: §f" + a.world().name());
    lore.add("§7Équipes actives: §f" + a.enabledTeams().size());
    lore.add("§7État: §f" + a.state().name());
    im.setLore(lore);
    im.getPersistentDataContainer().set(plugin.keys().ARENA_ID(), PersistentDataType.STRING, a.id());
    it.setItemMeta(im);
    return it;
  }

  private ItemStack icon(Material m, String name){
    ItemStack it = new ItemStack(m);
    ItemMeta im = it.getItemMeta();
    im.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', name));
    it.setItemMeta(im);
    return it;
  }
}

