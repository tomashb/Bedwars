package com.example.bedwars;

import com.example.bedwars.arena.ArenaManager;
import com.example.bedwars.command.AdminCommand;
import com.example.bedwars.command.BedwarsCommand;
import com.example.bedwars.generator.GeneratorManager;
import com.example.bedwars.gui.*;
import com.example.bedwars.listener.PlayerListener;
import com.example.bedwars.scoreboard.ScoreboardManager;
import com.example.bedwars.util.Messages;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Main plugin entry point wiring services, commands and listeners.
 */
public final class BedwarsPlugin extends JavaPlugin {

    private static BedwarsPlugin instance;

    private Messages messages;
    private ArenaManager arenaManager;
    private GeneratorManager generatorManager;
    private ScoreboardManager scoreboardManager;
    private MenuListener menuListener;
    private NamespacedKey arenaKey;
    private NamespacedKey actionKey;
    private NamespacedKey teamKey;
    private NamespacedKey genTypeKey;
    private NamespacedKey npcKey;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.messages = new Messages(this);
        this.arenaManager = new ArenaManager(this);
        this.generatorManager = new GeneratorManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.menuListener = new MenuListener(this);
        this.arenaKey = new NamespacedKey(this, "arena");
        this.actionKey = new NamespacedKey(this, "bw_action");
        this.teamKey = new NamespacedKey(this, "bw_team");
        this.genTypeKey = new NamespacedKey(this, "bw_genType");
        this.npcKey = new NamespacedKey(this, "bw_npc");

        // register menus
        menuListener.register(new RootMenu(this));
        menuListener.register(new ArenasMenu(this));
        menuListener.register(new ArenaEditorMenu(this));
        menuListener.register(new RulesEventsMenu(this));
        menuListener.register(new NpcShopsMenu(this));
        menuListener.register(new GeneratorsMenu(this));
        menuListener.register(new RotationMenu(this));
        menuListener.register(new ResetMenu(this));
        menuListener.register(new DiagnosticsMenu(this));

        // Commands
        BedwarsCommand bw = new BedwarsCommand(this);
        PluginCommand bwCmd = Objects.requireNonNull(getCommand("bw"));
        bwCmd.setExecutor(bw);
        bwCmd.setTabCompleter(bw);

        AdminCommand admin = new AdminCommand(this);
        PluginCommand adminCmd = Objects.requireNonNull(getCommand("bwadmin"));
        adminCmd.setExecutor(admin);
        adminCmd.setTabCompleter(admin);

        // Listeners
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(menuListener, this);
        pm.registerEvents(new PlayerListener(this), this);
    }

    public static BedwarsPlugin get() {
        return instance;
    }

    public Messages messages() {
        return messages;
    }

    public ArenaManager arenas() {
        return arenaManager;
    }

    public GeneratorManager generators() {
        return generatorManager;
    }

    public ScoreboardManager scoreboards() {
        return scoreboardManager;
    }

    public MenuListener menus() {
        return menuListener;
    }

    public NamespacedKey arenaKey() {
        return arenaKey;
    }

    public NamespacedKey actionKey() {
        return actionKey;
    }

    public NamespacedKey teamKey() {
        return teamKey;
    }

    public NamespacedKey genTypeKey() {
        return genTypeKey;
    }

    public NamespacedKey npcKey() {
        return npcKey;
    }
}
