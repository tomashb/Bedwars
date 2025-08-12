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
 * Very lightweight management menu. It only displays a few placeholder
 * items so administrators can open a GUI and navigate to future
 * functionality. Real editing logic is intentionally omitted but the
 * structure mirrors the spec so new features can hook into it later.
 */
public class AdminMenu {

    private final BedwarsPlugin plugin;

    public AdminMenu(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the main management GUI for the given player.
     *
     * @param player the player to open the GUI for
     */
    public void open(Player player) {
        String title = String.valueOf(plugin.getMessages().get("admin.menu-title"));
        title = ChatColor.translateAlternateColorCodes('&', title);

        Inventory inv = Bukkit.createInventory(player, 54, title);
        inv.setItem(10, item(Material.MAP, plugin.getMessages().get("admin.menu.arenas")));
        inv.setItem(12, item(Material.LIME_WOOL, plugin.getMessages().get("admin.menu.create")));
        inv.setItem(14, item(Material.ANVIL, plugin.getMessages().get("admin.menu.rules")));
        inv.setItem(16, item(Material.ARMOR_STAND, plugin.getMessages().get("admin.menu.npc")));
        inv.setItem(28, item(Material.ENDER_PEARL, plugin.getMessages().get("admin.menu.rotation")));
        inv.setItem(30, item(Material.REDSTONE_COMPARATOR, plugin.getMessages().get("admin.menu.diagnostics")));
        inv.setItem(32, item(Material.PAPER, plugin.getMessages().get("admin.menu.info")));
        player.openInventory(inv);
    }

    private ItemStack item(Material mat, String name) {
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
