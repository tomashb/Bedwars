package com.example.bedwars;

import com.example.bedwars.arena.ArenaManager;
import com.example.bedwars.commands.BwCommand;
import com.example.bedwars.gen.GeneratorManager;
import com.example.bedwars.shop.ShopManager;
import com.example.bedwars.shop.TeamUpgrades;
import com.example.bedwars.ui.MenuManager;
import com.example.bedwars.ui.Scoreboards;
import com.example.bedwars.util.Configs;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class BedwarsPlugin extends JavaPlugin {
    private static BedwarsPlugin instance;
    public static BedwarsPlugin get() { return instance; }

    private ArenaManager arenaManager;
    private GeneratorManager generatorManager;
    private ShopManager shopManager;
    private TeamUpgrades teamUpgrades;
    private Scoreboards scoreboards;
    private MenuManager menus;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("messages.yml", false);
        Configs.loadMessages(this);
        saveResource("shops/itemshop.yml", false);
        saveResource("arenas/example.yml", false);

        this.arenaManager = new ArenaManager(this);
        this.shopManager = new ShopManager(this);
        this.teamUpgrades = new TeamUpgrades(this, arenaManager);
        this.generatorManager = new GeneratorManager(this, arenaManager);
        this.scoreboards = new Scoreboards(this, arenaManager);
        this.menus = new MenuManager(this, arenaManager);

        BwCommand bw = new BwCommand(this, arenaManager, generatorManager, shopManager);
        getCommand("bw").setExecutor(bw);
        getCommand("bw").setTabCompleter(bw);

        Bukkit.getPluginManager().registerEvents(new com.example.bedwars.listeners.PlayerListener(this, arenaManager, shopManager), this);
        Bukkit.getPluginManager().registerEvents(new com.example.bedwars.listeners.BlockListener(arenaManager), this);
        Bukkit.getPluginManager().registerEvents(new com.example.bedwars.listeners.EntityListener(), this);
        Bukkit.getPluginManager().registerEvents(new com.example.bedwars.listeners.MenuListener(this, arenaManager, menus), this);
        Bukkit.getPluginManager().registerEvents(new com.example.bedwars.listeners.ShopListener(this), this);

        getLogger().info("Bedwars enabled.");
    }

    @Override
    public void onDisable() {
        generatorManager.shutdown();
        arenaManager.shutdown();
    }

    public ArenaManager arenas() { return arenaManager; }
    public GeneratorManager generators() { return generatorManager; }
    public ShopManager shops() { return shopManager; }
    public TeamUpgrades upgrades() { return teamUpgrades; }
    public Scoreboards boards() { return scoreboards; }
    public MenuManager menus() { return menus; }
}
