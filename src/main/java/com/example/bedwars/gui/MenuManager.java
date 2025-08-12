package com.example.bedwars.gui;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.entity.Player;

public final class MenuManager {
  private final BedwarsPlugin plugin;
  private final RootMenu root;
  // placeholders :
  private final placeholders.ArenasMenu arenas;
  private final placeholders.RulesEventsMenu rules;
  private final placeholders.NpcShopsMenu shops;
  private final placeholders.GeneratorsMenu gens;
  private final placeholders.RotationMenu rotation;
  private final placeholders.ResetMenu reset;
  private final placeholders.DiagnosticsMenu diag;
  private final placeholders.InfoMenu info;

  public MenuManager(BedwarsPlugin plugin) {
    this.plugin = plugin;
    this.root = new RootMenu(plugin);
    this.arenas = new placeholders.ArenasMenu(plugin);
    this.rules = new placeholders.RulesEventsMenu(plugin);
    this.shops = new placeholders.NpcShopsMenu(plugin);
    this.gens = new placeholders.GeneratorsMenu(plugin);
    this.rotation = new placeholders.RotationMenu(plugin);
    this.reset = new placeholders.ResetMenu(plugin);
    this.diag = new placeholders.DiagnosticsMenu(plugin);
    this.info = new placeholders.InfoMenu(plugin);
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
