package com.example.bedwars.gui;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ArenasMenu implements BWMenu {

    private final BedwarsPlugin plugin;

    public ArenasMenu(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AdminView id() {
        return AdminView.ARENAS;
    }

    @Override
    public void open(Player player, Object... args) {
        String title = ChatColor.translateAlternateColorCodes('&',
                String.valueOf(plugin.messages().get("admin.menu.arenas")));
        Inventory inv = Bukkit.createInventory(new BWMenuHolder(AdminView.ARENAS, null), 54, title);
        int slot = 0;
        for (String arenaId : plugin.arenas().getArenas().keySet()) {
            if (slot >= 53) {
                break;
            }
            inv.setItem(slot++, arenaItem(arenaId));
        }
        inv.setItem(53, backItem());
        player.openInventory(inv);
    }

    private ItemStack arenaItem(String id) {
        ItemStack map = new ItemStack(Material.MAP);
        ItemMeta meta = map.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(id);
            map.setItemMeta(meta);
        }
        return map;
    }

    private ItemStack backItem() {
        ItemStack it = new ItemStack(Material.BARRIER);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Retour");
            it.setItemMeta(meta);
        }
        return it;
    }
}
