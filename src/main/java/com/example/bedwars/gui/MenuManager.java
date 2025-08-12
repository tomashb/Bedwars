package com.example.bedwars.gui;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.gui.placeholders.*;
import org.bukkit.entity.Player;

public final class MenuManager {
  private final BedwarsPlugin plugin;
  private final RootMenu root;
  private final ArenasMenu arenas;
  private final RulesEventsMenu rules;
  private final NpcShopsMenu shops;
  private final GeneratorsMenu gens;
  private final RotationMenu rotation;
  private final ResetMenu reset;
  private final DiagnosticsMenu diag;
  private final InfoMenu info;

  public MenuManager(BedwarsPlugin plugin) {
    this.plugin = plugin;
    this.root = new RootMenu(plugin);
    this.arenas = new ArenasMenu(plugin);
    this.rules = new RulesEventsMenu(plugin);
    this.shops = new NpcShopsMenu(plugin);
    this.gens = new GeneratorsMenu(plugin);
    this.rotation = new RotationMenu(plugin);
    this.reset = new ResetMenu(plugin);
    this.diag = new DiagnosticsMenu(plugin);
    this.info = new InfoMenu(plugin);
  }

  public void open(AdminView v, Player p, String arenaId) {
    switch (v) {
      case ROOT -> root.open(p);
      case ARENAS -> arenas.open(p);
      case RULES_EVENTS -> rules.open(p);
      case NPC_SHOPS -> shops.open(p);
      case GENERATORS -> gens.open(p);
      case ROTATION -> rotation.open(p);
      case RESET -> reset.open(p);
      case DIAGNOSTICS -> diag.open(p);
      case INFO -> info.open(p);
      default -> root.open(p);
    }
  }
}
