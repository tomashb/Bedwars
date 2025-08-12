package com.example.bedwars.gui;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Main administration menu. Provides navigation to other views.
 */
public class RootMenu implements BWMenu {

    private final BedwarsPlugin plugin;

    public RootMenu(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AdminView id() {
        return AdminView.ROOT;
    }

    @Override
    public void open(Player player, Object... args) {
        String title = ChatColor.translateAlternateColorCodes('&',
                String.valueOf(plugin.getMessages().get("admin.menu-title")));
        Inventory inv = Bukkit.createInventory(new BWMenuHolder(AdminView.ROOT, null), 54, title);
        inv.setItem(10, item(Material.MAP, plugin.getMessages().get("admin.menu.arenas")));
        inv.setItem(12, item(Material.LIME_WOOL, plugin.getMessages().get("admin.menu.create")));
        inv.setItem(14, item(Material.ANVIL, plugin.getMessages().get("admin.menu.rules")));
        inv.setItem(16, item(Material.ARMOR_STAND, plugin.getMessages().get("admin.menu.npc")));
        inv.setItem(28, item(Material.ENDER_PEARL, plugin.getMessages().get("admin.menu.rotation")));
        inv.setItem(30, item(Material.COMPARATOR, plugin.getMessages().get("admin.menu.diagnostics")));
        inv.setItem(32, item(Material.PAPER, plugin.getMessages().get("admin.menu.info")));
        player.openInventory(inv);
    }

    private ItemStack item(Material mat, Object name) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            String display = ChatColor.translateAlternateColorCodes('&', String.valueOf(name));
            meta.setDisplayName(display);
            it.setItemMeta(meta);
        }
        return it;
    }
}
