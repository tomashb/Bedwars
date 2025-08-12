package com.example.bedwars.gui;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.generator.GeneratorType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * GUI used to configure arena elements such as lobby, team spawns,
 * beds, generators and NPC shops.
 */
public class ArenaEditorMenu implements BWMenu {

    private final BedwarsPlugin plugin;

    public ArenaEditorMenu(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AdminView id() {
        return AdminView.ARENA_EDITOR;
    }

    @Override
    public void open(Player player, Object... args) {
        String arenaId = args.length > 0 ? String.valueOf(args[0]) : "?";
        String title = ChatColor.translateAlternateColorCodes('&', "&6Édition " + arenaId);
        Inventory inv = Bukkit.createInventory(new BWMenuHolder(AdminView.ARENA_EDITOR, arenaId), 27, title);

        Arena arena = plugin.arenas().getArena(arenaId).orElse(null);

        // lobby
        inv.setItem(0, action(Material.PAPER, "Définir lobby", "SET_LOBBY", null, arena != null && arena.getLobby() != null));

        // team spawns and beds for enabled teams
        int col = 10;
        if (arena != null) {
            for (TeamColor color : arena.getEnabledTeams()) {
                inv.setItem(col, teamAction(color.wool(), color.chatColor() + "Spawn", "TEAM_SPAWN", color.name(), arena.getTeamSpawn(color) != null));
                inv.setItem(col + 9, teamAction(color.bed(), color.chatColor() + "Lit", "TEAM_BED", color.name(), arena.getTeamBed(color) != null));
                col += 2;
            }
        } else {
            inv.setItem(10, teamAction(Material.RED_WOOL, ChatColor.RED + "Spawn", "TEAM_SPAWN", "RED", false));
            inv.setItem(19, teamAction(Material.RED_BED, ChatColor.RED + "Lit", "TEAM_BED", "RED", false));
            inv.setItem(12, teamAction(Material.BLUE_WOOL, ChatColor.BLUE + "Spawn", "TEAM_SPAWN", "BLUE", false));
            inv.setItem(21, teamAction(Material.BLUE_BED, ChatColor.BLUE + "Lit", "TEAM_BED", "BLUE", false));
        }

        // generators
        inv.setItem(2, action(Material.IRON_INGOT, "Générateur Fer", "GEN_ADD", GeneratorType.IRON.name(), arena != null && !arena.getGenerators().isEmpty()));
        inv.setItem(3, action(Material.GOLD_INGOT, "Générateur Or", "GEN_ADD", GeneratorType.GOLD.name(), arena != null && !arena.getGenerators().isEmpty()));
        inv.setItem(4, action(Material.DIAMOND, "Générateur Diamant", "GEN_ADD", GeneratorType.DIAMOND.name(), arena != null && !arena.getGenerators().isEmpty()));
        inv.setItem(5, action(Material.EMERALD, "Générateur Émeraude", "GEN_ADD", GeneratorType.EMERALD.name(), arena != null && !arena.getGenerators().isEmpty()));

        // NPCs
        inv.setItem(15, action(Material.CHEST, "PNJ Objets", "NPC_ITEM", null, arena != null && !arena.getItemShops().isEmpty()));
        inv.setItem(16, action(Material.ANVIL, "PNJ Améliorations", "NPC_UPGRADE", null, arena != null && !arena.getUpgradeShops().isEmpty()));

        // save / reload / back
        inv.setItem(24, action(Material.WRITABLE_BOOK, "Sauver", "SAVE", null, false));
        inv.setItem(25, action(Material.BOOK, "Recharger", "RELOAD", null, false));
        inv.setItem(26, action(Material.BARRIER, ChatColor.RED + "Retour", "BACK", null, false));

        player.openInventory(inv);
    }

    private ItemStack teamAction(Material mat, String name, String action, String team, boolean done) {
        ItemStack it = action(mat, name, action, team, done);
        return it;
    }

    private ItemStack action(Material mat, String name, String action, String extra, boolean done) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e" + name));
            meta.getPersistentDataContainer().set(plugin.actionKey(), PersistentDataType.STRING, action);
            if (extra != null) {
                if (action.equals("TEAM_SPAWN") || action.equals("TEAM_BED")) {
                    meta.getPersistentDataContainer().set(plugin.teamKey(), PersistentDataType.STRING, extra);
                } else if (action.equals("GEN_ADD")) {
                    meta.getPersistentDataContainer().set(plugin.genTypeKey(), PersistentDataType.STRING, extra);
                }
            }
            if (done) {
                meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            it.setItemMeta(meta);
        }
        return it;
    }
}
