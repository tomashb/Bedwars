package com.example.bedwars;

import com.example.bedwars.arena.ArenaManager;
import com.example.bedwars.command.BedwarsCommand;
import com.example.bedwars.listener.PlayerListener;
import com.example.bedwars.util.MessageManager;
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

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.messageManager = new MessageManager(this);
        this.arenaManager = new ArenaManager(this);

        // Register command executor
        getCommand("bw").setExecutor(new BedwarsCommand(this));

        // Register basic listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public MessageManager getMessages() {
        return messageManager;
    }
}
