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
        inv.setItem(10, icon(Material.MAP, plugin.getMessages().get("admin.menu.arenas"), null));
        inv.setItem(12, icon(Material.LIME_WOOL, plugin.getMessages().get("admin.menu.create"), null));
        inv.setItem(14, icon(Material.ANVIL, plugin.getMessages().get("admin.menu.rules"), null));
        inv.setItem(16, icon(Material.ARMOR_STAND, plugin.getMessages().get("admin.menu.npc"), null));
        inv.setItem(28, icon(Material.ENDER_PEARL, plugin.getMessages().get("admin.menu.rotation"), null));
        inv.setItem(30, icon(Material.TNT, plugin.getMessages().get("admin.menu.reset"), plugin.getMessages().get("admin.menu.reset-lore")));
        inv.setItem(32, icon(Material.COMPARATOR, plugin.getMessages().get("admin.menu.diagnostics"), plugin.getMessages().get("admin.menu.diagnostics-lore")));
        inv.setItem(34, icon(Material.PAPER, plugin.getMessages().get("admin.menu.info"), plugin.getMessages().get("admin.menu.info-lore")));
        player.openInventory(inv);
    }

    private ItemStack icon(Material mat, String name, String lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta im = it.getItemMeta();
        if (im != null) {
            im.setDisplayName(ChatColor.translateAlternateColorCodes('&', String.valueOf(name)));
            if (lore != null && !lore.isEmpty())
                im.setLore(java.util.List.of(ChatColor.translateAlternateColorCodes('&', lore)));
            it.setItemMeta(im);
        }
        return it;
    }
}
