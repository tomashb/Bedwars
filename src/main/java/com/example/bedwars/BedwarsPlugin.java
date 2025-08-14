package com.example.bedwars;

import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;
import com.example.bedwars.util.Messages;
import com.example.bedwars.command.BwCommand;
import com.example.bedwars.command.BwAdminCommand;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.ArenaManager;
import com.example.bedwars.gui.MenuManager;
import com.example.bedwars.ops.Keys;
import com.example.bedwars.listeners.MenuListener;
import com.example.bedwars.listeners.EditorListener;
import com.example.bedwars.listeners.UpgradeApplyListener;
import com.example.bedwars.listeners.GameStateListener;
import com.example.bedwars.listeners.JoinLeaveListener;
import com.example.bedwars.listeners.BedListener;
import com.example.bedwars.listeners.DamageRulesListener;
import com.example.bedwars.listeners.EntityExplodeListener;
import com.example.bedwars.listeners.CompassListener;
import com.example.bedwars.listeners.ArmorLockListener;
import com.example.bedwars.listeners.GameplayListener;
import com.example.bedwars.listeners.FireballListener;
import com.example.bedwars.rules.BuildRulesListener;
import com.example.bedwars.listeners.DeathListener;
import com.example.bedwars.listeners.VoidFailSafeListener;
import com.example.bedwars.listeners.PlayerRespawnListener;
import com.example.bedwars.listeners.TntListener;
import com.example.bedwars.lobby.LobbyItemsService;
import com.example.bedwars.lobby.LobbyListener;
import com.example.bedwars.gui.TeamSelectMenu;
import com.example.bedwars.setup.PromptService;
import com.example.bedwars.shop.ShopConfig;
import com.example.bedwars.shop.UpgradeService;
import com.example.bedwars.shop.ShopListener;
import com.example.bedwars.game.PlayerContextService;
import com.example.bedwars.game.TeamAssignment;
import com.example.bedwars.game.KitService;
import com.example.bedwars.game.SpectatorService;
import com.example.bedwars.game.GameMessages;
import com.example.bedwars.game.GameService;
import com.example.bedwars.game.DeathRespawnService;
import com.example.bedwars.game.LightingService;
import com.example.bedwars.gen.GeneratorManager;
import com.example.bedwars.gen.GeneratorProfileService;
import com.example.bedwars.shop.NpcManager;
import com.example.bedwars.services.BuildRulesService;
import com.example.bedwars.services.TasksService;
import com.example.bedwars.game.RotationManager;
import com.example.bedwars.game.ResetManager;
import com.example.bedwars.border.BorderService;
import com.example.bedwars.border.BorderMoveListener;
import com.example.bedwars.border.BorderBuildListener;
import com.example.bedwars.border.BorderStateListener;
import com.example.bedwars.arena.GameState;

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
  private GeneratorProfileService generatorProfiles;
  private NpcManager npcManager;
  private TeamAssignment teamAssignment;
  private KitService kitService;
  private SpectatorService spectatorService;
  private GameMessages gameMessages;
  private GameService gameService;
  private LightingService lightingService;
  private BuildRulesService buildRules;
  private DeathRespawnService deathService;
  private LobbyItemsService lobbyItems;
  private TeamSelectMenu teamSelectMenu;
  private com.example.bedwars.hud.ScoreboardManager scoreboardManager;
  private com.example.bedwars.hud.ActionBarBus actionBarBus;
  private RotationManager rotationManager;
  private ResetManager resetManager;
  private TasksService tasksService;
  private BorderService borderService;
  private com.example.bedwars.services.ToolsService toolsService;

  @Override
  public void onEnable() {
    instance = this;
    saveDefaultConfig();
    this.messages = new Messages(this);
    this.keys = new Keys(this);
    this.arenaManager = new ArenaManager(this);
    this.arenaManager.loadAll();
    saveResource("rotation.yml", false);
    this.rotationManager = new RotationManager(this);
    this.resetManager = new ResetManager(this);
    this.resetManager.restoreWorldsOnEnable();
    this.menuManager = new MenuManager(this);
    this.promptService = new PromptService(this);
    this.shopConfig = new ShopConfig(this);
    this.contextService = new PlayerContextService();
    this.teamAssignment = new TeamAssignment(contextService);
    this.kitService = new KitService(this, contextService);
    this.spectatorService = new SpectatorService();
    this.gameMessages = new GameMessages(this, contextService);
    this.lobbyItems = new LobbyItemsService(this);
    this.teamSelectMenu = new TeamSelectMenu(this, contextService);
    this.lightingService = new LightingService(this);
    this.borderService = new BorderService(this);
    for (Arena a : this.arenaManager.all()) {
      this.lightingService.applyDayClear(a);
      this.lightingService.relightArena(a);
      this.borderService.apply(a);
    }
    this.gameService = new GameService(this, contextService, teamAssignment, kitService, spectatorService, gameMessages, lobbyItems);
    this.deathService = new DeathRespawnService(this, contextService, kitService, spectatorService, gameMessages, gameService);
    this.gameService.setDeathService(deathService);
    this.upgradeService = new UpgradeService(this, contextService);
    this.buildRules = new BuildRulesService(this);
    this.buildRules.rebuildWhitelistFromShop(shopConfig);
    this.generatorProfiles = new GeneratorProfileService(this);
    this.generatorManager = new GeneratorManager(this);
    this.generatorManager.start();
    this.npcManager = new NpcManager(this);
    for (Arena a : this.arenaManager.all()) {
      this.npcManager.ensureSpawned(a);
    }
    this.tasksService = new TasksService(this);
    this.toolsService = new com.example.bedwars.services.ToolsService(this);

    this.actionBarBus = new com.example.bedwars.hud.ActionBarBus();
    if (getConfig().getBoolean("actionbar.enabled", true)) {
      this.actionBarBus.start(this);
    }
    this.scoreboardManager = new com.example.bedwars.hud.ScoreboardManager(this);
    if (getConfig().getBoolean("scoreboard.enabled", true)) {
      org.bukkit.Bukkit.getScheduler().runTaskTimer(this, () -> scoreboardManager.tick(), 20L, 20L);
    }

    Objects.requireNonNull(getCommand("bw")).setExecutor(new BwCommand(this));
    Objects.requireNonNull(getCommand("bwadmin")).setExecutor(new BwAdminCommand(this));

    getServer().getPluginManager().registerEvents(new MenuListener(this), this);
    getServer().getPluginManager().registerEvents(new EditorListener(this), this);
    getServer().getPluginManager().registerEvents(promptService, this);
    getServer().getPluginManager().registerEvents(new ShopListener(this, contextService), this);
    getServer().getPluginManager().registerEvents(new UpgradeApplyListener(this, contextService), this);
    getServer().getPluginManager().registerEvents(new GameStateListener(this), this);
    getServer().getPluginManager().registerEvents(new JoinLeaveListener(gameService), this);
    getServer().getPluginManager().registerEvents(new BedListener(this, gameService, contextService), this);
    getServer().getPluginManager().registerEvents(new DeathListener(this, deathService, contextService), this);
    getServer().getPluginManager().registerEvents(new VoidFailSafeListener(this, deathService, contextService), this);
    getServer().getPluginManager().registerEvents(new PlayerRespawnListener(contextService), this);
    getServer().getPluginManager().registerEvents(new BuildRulesListener(this, contextService, buildRules), this);
    getServer().getPluginManager().registerEvents(new DamageRulesListener(this, contextService), this);
    getServer().getPluginManager().registerEvents(new EntityExplodeListener(this, buildRules), this);
    getServer().getPluginManager().registerEvents(new TntListener(this, contextService, buildRules), this);
    getServer().getPluginManager().registerEvents(teamSelectMenu, this);
    getServer().getPluginManager().registerEvents(new LobbyListener(this, lobbyItems, teamSelectMenu, contextService, gameService), this);
    getServer().getPluginManager().registerEvents(new CompassListener(this, contextService, teamSelectMenu), this);
    getServer().getPluginManager().registerEvents(new ArmorLockListener(this, contextService), this);
    getServer().getPluginManager().registerEvents(new GameplayListener(this, contextService, lobbyItems), this);
    getServer().getPluginManager().registerEvents(new FireballListener(this, contextService), this);
    getServer().getPluginManager().registerEvents(new BorderMoveListener(this, contextService, borderService), this);
    getServer().getPluginManager().registerEvents(new BorderBuildListener(this, contextService, borderService), this);
    getServer().getPluginManager().registerEvents(new BorderStateListener(borderService), this);
    getServer().getPluginManager().registerEvents(new com.example.bedwars.listeners.ToolsListener(toolsService), this);

    getLogger().info("Bedwars loaded.");
  }

  @Override
  public void onDisable() {
    if (arenaManager != null) {
      arenaManager.all().forEach(a -> {
        if (a.state() == GameState.RUNNING && buildRules != null) {
          buildRules.cleanupPlacedSync(a);
        }
      });
      arenaManager.saveAll();
    }
    if (npcManager != null) {
      arenaManager.all().forEach(npcManager::despawnAll);
    }
    if (generatorManager != null) generatorManager.stop();
    if (scoreboardManager != null) scoreboardManager.clear();
    getLogger().info("Bedwars disabled.");
  }

  public static BedwarsPlugin get() {
    return instance;
  }

  public Messages messages() {
    return messages;
  }

  public String msg(String key){ return messages.msg(key); }
  public String msg(String key, java.util.Map<String, ?> tokens){ return messages.msg(key, tokens); }

  public void logInfo(String fmt, Object... args){ getLogger().info(String.format(fmt, args)); }
  public void logWarning(String fmt, Object... args){ getLogger().warning(String.format(fmt, args)); }
  public void logSevere(String fmt, Object... args){ getLogger().severe(String.format(fmt, args)); }

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
  public GeneratorProfileService profiles() { return generatorProfiles; }
  public NpcManager npcs() { return npcManager; }
  public GameService game() { return gameService; }
  public LightingService lighting() { return lightingService; }
  public BuildRulesService buildRules() { return buildRules; }
  public com.example.bedwars.hud.ScoreboardManager scoreboard() { return scoreboardManager; }
  public com.example.bedwars.hud.ActionBarBus actionBar() { return actionBarBus; }
  public RotationManager rotation() { return rotationManager; }
  public ResetManager reset() { return resetManager; }
  public TasksService tasks() { return tasksService; }
  public BorderService border() { return borderService; }
  public com.example.bedwars.services.ToolsService tools() { return toolsService; }
}
