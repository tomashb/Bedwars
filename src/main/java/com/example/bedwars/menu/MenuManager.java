package com.example.bedwars.menu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class MenuManager {
    private MenuManager() {}

    public static void openRoot(Player player) {
        Inventory inv = Bukkit.createInventory(new BWMenuHolder(AdminView.ROOT, null), 54,
                ChatColor.translateAlternateColorCodes('&', "&cBedWars Admin"));
        inv.setItem(10, named(Material.PAPER, "&aArenas"));
        inv.setItem(12, named(Material.ANVIL, "&bCreate Arena"));
        inv.setItem(14, named(Material.BOOK, "&eRules & Events"));
        inv.setItem(16, named(Material.VILLAGER_SPAWN_EGG, "&dNPC & Shops"));
        inv.setItem(28, named(Material.COMPASS, "&6Rotation"));
        inv.setItem(30, named(Material.BARRIER, "&cReset"));
        inv.setItem(32, named(Material.REDSTONE_TORCH, "&7Diagnostics"));
        player.openInventory(inv);
    }

    private static ItemStack named(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            item.setItemMeta(meta);
        }
        return item;
    }
}
