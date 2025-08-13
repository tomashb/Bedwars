package com.example.bedwars;

import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;
import com.example.bedwars.util.Messages;
import com.example.bedwars.command.BwCommand;
import com.example.bedwars.command.BwAdminCommand;
import com.example.bedwars.arena.ArenaManager;
import com.example.bedwars.gui.MenuManager;
import com.example.bedwars.ops.Keys;
import com.example.bedwars.listeners.MenuListener;
import com.example.bedwars.listeners.EditorListener;
import com.example.bedwars.listeners.UpgradeApplyListener;
import com.example.bedwars.listeners.GameStateListener;
import com.example.bedwars.setup.PromptService;
import com.example.bedwars.shop.ShopConfig;
import com.example.bedwars.shop.UpgradeService;
import com.example.bedwars.shop.ShopListener;
import com.example.bedwars.service.PlayerContextService;
import com.example.bedwars.gen.GeneratorManager;

public final class BedwarsPlugin extends JavaPlugin {

  private static BedwarsPlugin instance;
  private Messages messages;
  private Keys keys;
  private ArenaManager arenaManager;
  private MenuManager menuManager;
  private PromptService promptService;
  private ShopConfig shopConfig;
  private PlayerContextService contextService;
  private UpgradeService upgradeService;
  private GeneratorManager generatorManager;

  @Override
  public void onEnable() {
    instance = this;
    saveDefaultConfig();
    this.messages = new Messages(this);
    this.keys = new Keys(this);
    this.arenaManager = new ArenaManager(this);
    this.arenaManager.loadAll();
    this.menuManager = new MenuManager(this);
    this.promptService = new PromptService(this);
    this.shopConfig = new ShopConfig(this);
    this.contextService = new PlayerContextService();
    this.upgradeService = new UpgradeService(this, contextService);
    this.generatorManager = new GeneratorManager(this);
    this.generatorManager.start();

    Objects.requireNonNull(getCommand("bw")).setExecutor(new BwCommand(this));
    Objects.requireNonNull(getCommand("bwadmin")).setExecutor(new BwAdminCommand(this));

    getServer().getPluginManager().registerEvents(new MenuListener(this), this);
    getServer().getPluginManager().registerEvents(new EditorListener(this), this);
    getServer().getPluginManager().registerEvents(promptService, this);
    getServer().getPluginManager().registerEvents(new ShopListener(this, contextService), this);
    getServer().getPluginManager().registerEvents(new UpgradeApplyListener(this, contextService), this);
    getServer().getPluginManager().registerEvents(new GameStateListener(this), this);

    getLogger().info("Bedwars loaded.");
  }

  @Override
  public void onDisable() {
    if (arenaManager != null) arenaManager.saveAll();
    if (generatorManager != null) generatorManager.stop();
    getLogger().info("Bedwars disabled.");
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

  public Keys keys() {
    return keys;
  }

  public MenuManager menus() {
    return menuManager;
  }

  public PromptService prompts() {
    return promptService;
  }

  public ShopConfig shopConfig() { return shopConfig; }
  public PlayerContextService contexts() { return contextService; }
  public UpgradeService upgrades() { return upgradeService; }
  public GeneratorManager generators() { return generatorManager; }
}
