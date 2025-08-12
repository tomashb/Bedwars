package com.example.bedwars.gui;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Central registry for admin menus with basic click routing.
 */
public class MenuListener implements Listener {

    private final BedwarsPlugin plugin;
    private final Map<AdminView, BWMenu> menus = new EnumMap<>(AdminView.class);
    private final Map<UUID, String> arenaContext = new HashMap<>();

    public MenuListener(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(BWMenu menu) {
        menus.put(menu.id(), menu);
    }

    public void open(Player player, AdminView view, String arenaId, Object... args) {
        if (arenaId != null) {
            arenaContext.put(player.getUniqueId(), arenaId);
        } else {
            arenaContext.remove(player.getUniqueId());
        }
        BWMenu menu = menus.get(view);
        if (menu != null) {
            menu.open(player, args);
        }
    }

    public void openRoot(Player player) {
        open(player, AdminView.ROOT, null);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getView().getTopInventory().getHolder() instanceof BWMenuHolder holder) {
            event.setCancelled(true);
            AdminView view = holder.getView();
            int slot = event.getRawSlot();
            switch (view) {
                case ROOT -> handleRootClick(player, slot);
                case ARENAS -> handleArenasClick(player, slot);
                case ARENA_EDITOR -> handleArenaEditorClick(player, slot);
                case RULES_EVENTS, NPC_SHOPS, GENERATORS, ROTATION, RESET, DIAGNOSTICS -> {
                    if (slot == 26) {
                        openRoot(player);
                    }
                }
                default -> {
                }
            }
        }
    }

    private void handleRootClick(Player player, int slot) {
        switch (slot) {
            case 10 -> open(player, AdminView.ARENAS, null);
            case 12 -> player.closeInventory(); // placeholder for arena creation
            case 14 -> open(player, AdminView.RULES_EVENTS, null);
            case 16 -> open(player, AdminView.NPC_SHOPS, null);
            case 28 -> open(player, AdminView.ROTATION, null);
            case 30 -> open(player, AdminView.RESET, null);
            case 32 -> open(player, AdminView.DIAGNOSTICS, null);
            default -> {
            }
        }
    }

    private void handleArenasClick(Player player, int slot) {
        if (slot == 53) {
            openRoot(player);
            return;
        }
        var inv = player.getOpenInventory().getTopInventory();
        var item = inv.getItem(slot);
        if (item != null && item.hasItemMeta()) {
            String arena = item.getItemMeta().getDisplayName();
            open(player, AdminView.ARENA_EDITOR, arena, arena);
        }
    }

    private void handleArenaEditorClick(Player player, int slot) {
        if (slot == 26) {
            open(player, AdminView.ARENAS, null);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        // nothing for now
    }
}
