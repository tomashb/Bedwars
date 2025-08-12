package com.example.bedwars;

import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;
import com.example.bedwars.util.Messages;
import com.example.bedwars.command.BwCommand;
import com.example.bedwars.command.BwAdminCommand;
import com.example.bedwars.arena.ArenaManager;
import com.example.bedwars.gui.MenuManager;
import com.example.bedwars.listeners.MenuListener;
import com.example.bedwars.listeners.EditorListener;
import com.example.bedwars.setup.PromptService;

public final class BedwarsPlugin extends JavaPlugin {

  private static BedwarsPlugin instance;
  private Messages messages;
  private ArenaManager arenaManager;
  private MenuManager menuManager;
  private PromptService promptService;

  @Override
  public void onEnable() {
    instance = this;
    saveDefaultConfig();
    this.messages = new Messages(this);
    this.arenaManager = new ArenaManager(this);
    this.arenaManager.loadAll();
    this.menuManager = new MenuManager(this);
    this.promptService = new PromptService(this);

    Objects.requireNonNull(getCommand("bw")).setExecutor(new BwCommand(this));
    Objects.requireNonNull(getCommand("bwadmin")).setExecutor(new BwAdminCommand(this));

    getServer().getPluginManager().registerEvents(new MenuListener(this), this);
    getServer().getPluginManager().registerEvents(new EditorListener(this), this);
    getServer().getPluginManager().registerEvents(promptService, this);

    getLogger().info("Bedwars loaded.");
  }

  @Override
  public void onDisable() {
    if (arenaManager != null) arenaManager.saveAll();
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

  public MenuManager menus() {
    return menuManager;
  }

  public PromptService prompts() {
    return promptService;
  }
}
