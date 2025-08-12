package com.example.bedwars;

import com.example.bedwars.arena.ArenaManager;
import com.example.bedwars.command.AdminCommand;
import com.example.bedwars.command.BedwarsCommand;
import com.example.bedwars.gui.MenuManager;
import com.example.bedwars.gui.*;
import com.example.bedwars.listener.PlayerListener;
import com.example.bedwars.util.MessageManager;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class. This is only a lightweight skeleton that wires
 * together a couple of managers and commands so the plugin can
 * actually load. It is not a full BedWars implementation but provides
 * a starting point for further development.
 */
public class BedwarsPlugin extends JavaPlugin {

    private ArenaManager arenaManager;
    private MessageManager messageManager;
    private MenuManager menuManager;
    private NamespacedKey arenaKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.messageManager = new MessageManager(this);
        this.arenaManager = new ArenaManager(this);
        this.menuManager = new MenuManager(this);
        // Register menus
        menuManager.register(new RootMenu(this));
        menuManager.register(new ArenasMenu(this));
        menuManager.register(new ArenaEditorMenu(this));
        menuManager.register(new RulesEventsMenu(this));
        menuManager.register(new NpcShopsMenu(this));
        menuManager.register(new GeneratorsMenu(this));
        menuManager.register(new RotationMenu(this));
        menuManager.register(new ResetMenu(this));
        menuManager.register(new DiagnosticsMenu(this));

        this.arenaKey = new NamespacedKey(this, "bw_arena");

        // Register command executors
        getCommand("bw").setExecutor(new BedwarsCommand(this));
        AdminCommand adminCmd = new AdminCommand(this);
        getCommand("bwadmin").setExecutor(adminCmd);
        getCommand("bwadmin").setTabCompleter(adminCmd);

        // Register basic listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(menuManager, this);
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public MessageManager getMessages() {
        return messageManager;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    /**
     * Namespaced key used to tag entities belonging to a specific arena.
     * This key enables maintenance routines such as cleanup to locate
     * plugin-created entities safely.
     *
     * @return NamespacedKey for arena tags
     */
    public NamespacedKey getArenaKey() {
        return arenaKey;
    }
}
