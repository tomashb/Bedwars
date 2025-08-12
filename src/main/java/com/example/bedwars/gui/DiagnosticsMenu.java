package com.example.bedwars.gui;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DiagnosticsMenu implements BWMenu {

    private final BedwarsPlugin plugin;

    public DiagnosticsMenu(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AdminView id() {
        return AdminView.DIAGNOSTICS;
    }

    @Override
    public void open(Player player, Object... args) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6Diagnostics");
        Inventory inv = Bukkit.createInventory(new BWMenuHolder(AdminView.DIAGNOSTICS, null), 27, title);
        inv.setItem(26, backItem());
        player.openInventory(inv);
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
