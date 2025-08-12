package com.example.bedwars.gui;

import org.bukkit.entity.Player;

/**
 * Basic API for plugin menus.
 */
public interface BWMenu {
    /**
     * @return the view identifier of this menu
     */
    AdminView id();

    /**
     * Opens the menu for the given player.
     *
     * @param player the target player
     * @param args optional extra context
     */
    void open(Player player, Object... args);
}
