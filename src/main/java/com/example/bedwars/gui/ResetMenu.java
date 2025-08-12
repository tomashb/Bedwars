package com.example.bedwars.gui;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ResetMenu implements BWMenu {

    private final BedwarsPlugin plugin;

    public ResetMenu(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AdminView id() {
        return AdminView.RESET;
    }

    @Override
    public void open(Player player, Object... args) {
        String title = ChatColor.translateAlternateColorCodes('&',
                String.valueOf(plugin.getMessages().get("admin.reset-title")));
        Inventory inv = Bukkit.createInventory(new BWMenuHolder(AdminView.RESET, null), 27, title);
        inv.setItem(26, icon(Material.BARRIER, plugin.getMessages().get("admin.menu.info"),
                plugin.getMessages().get("admin.menu.info-lore")));
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
