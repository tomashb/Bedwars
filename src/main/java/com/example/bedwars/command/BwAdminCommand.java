package com.example.bedwars.command;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.arena.WorldRef;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BwAdminCommand implements CommandExecutor {
  private final BedwarsPlugin plugin;

  public BwAdminCommand(BedwarsPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    try {
      return handleCommand(sender, cmd, label, args);
    } catch (Throwable t) {
      plugin.getLogger().log(java.util.logging.Level.SEVERE,
          "Command error: /" + label + " " + String.join(" ", args), t);
      sender.sendMessage(plugin.messages().get("prefix") +
          plugin.messages().get("errors.internal"));
      return true;
    }
  }

  private boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!sender.hasPermission("bedwars.admin.*")) {
      sender.sendMessage(plugin.messages().get("errors.no_perm"));
      return true;
    }
    if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
      List<String> lines = plugin.messages().getList("help.admin");
      sender.sendMessage(plugin.messages().get("prefix") + String.join("\n", lines));
      return true;
    }

    if (args[0].equalsIgnoreCase("arena")) {
      if (!sender.hasPermission("bedwars.admin.arena")) {
        sender.sendMessage(plugin.messages().get("errors.no_perm"));
        return true;
      }
      handleArena(sender, args);
      return true;
    }

    if (args[0].equalsIgnoreCase("lobby")) {
      if (!sender.hasPermission("bedwars.admin.lobby")) {
        sender.sendMessage(plugin.messages().get("errors.no_perm"));
        return true;
      }
      if (args.length >= 2 && args[1].equalsIgnoreCase("set") && sender instanceof Player p) {
        plugin.lobby().location().setFrom(p);
        sender.sendMessage(plugin.messages().get("lobby.set_ok"));
      } else {
        sender.sendMessage(plugin.messages().get("lobby.missing"));
      }
      return true;
    }

    if (args[0].equalsIgnoreCase("game")) {
      if (!sender.hasPermission("bedwars.admin.game")) {
        sender.sendMessage(plugin.messages().get("errors.no_perm"));
        return true;
      }
      handleGame(sender, args);
      return true;
    }

    if (args[0].equalsIgnoreCase("npc")) {
      if (!sender.hasPermission("bedwars.admin.npc")) {
        sender.sendMessage(plugin.messages().get("errors.no_perm"));
        return true;
      }
      handleNpc(sender, args);
      return true;
    }

    if (args[0].equalsIgnoreCase("backup")) {
      if (!sender.hasPermission("bedwars.admin.backup")) {
        sender.sendMessage(plugin.messages().get("errors.no_perm"));
        return true;
      }
      if (args.length >= 2 && args[1].equalsIgnoreCase("now")) {
        sender.sendMessage(
            plugin.messages().get("prefix") + plugin.messages().get("backup.not_implemented"));
      } else {
        sender.sendMessage(plugin.messages().get("prefix") + "Usage: /bwadmin backup now");
      }
      return true;
    }

    if (args[0].equalsIgnoreCase("debug")) {
      handleDebug(sender, args);
      return true;
    }

    if (args[0].equalsIgnoreCase("maintenance")) {
      handleMaintenance(sender, args);
      return true;
    }

    if (args[0].equalsIgnoreCase("version")) {
      sender.sendMessage(plugin.messages().get("prefix") + plugin.getDescription().getVersion());
      return true;
    }

    List<String> lines = plugin.messages().getList("help.admin");
    sender.sendMessage(plugin.messages().get("prefix") + String.join("\n", lines));
    return true;
  }

  private void handleGame(CommandSender sender, String[] args) {
    if (args.length < 3) {
      sender.sendMessage(plugin.messages().get("prefix") + "Usage: /bwadmin game <start|stop|forcewin> <arena> [team]");
      return;
    }
    String sub = args[1].toLowerCase();
    String arenaId = args[2];
    switch (sub) {
      case "start" -> plugin.game().start(arenaId);
      case "stop" -> plugin.game().stop(arenaId, "admin");
      case "forcewin" -> {
        if (args.length < 4) {
          sender.sendMessage(plugin.messages().get("prefix") + "Team required");
          return;
        }
        try {
          TeamColor c = TeamColor.valueOf(args[3].toUpperCase());
          plugin.game().forceWin(arenaId, c);
        } catch (IllegalArgumentException ex) {
          sender.sendMessage(plugin.messages().get("prefix") + "Unknown team");
        }
      }
      default -> sender.sendMessage(plugin.messages().get("prefix") + "Usage: /bwadmin game <start|stop|forcewin> <arena> [team]");
    }
  }

  private void handleNpc(CommandSender sender, String[] args) {
    String prefix = plugin.messages().get("prefix");
    if (args.length < 3) {
      sender.sendMessage(prefix + "Usage: /bwadmin npc <respawn|list> <arena>");
      return;
    }
    String sub = args[1].toLowerCase();
    String arenaId = args[2];
    var opt = plugin.arenas().get(arenaId);
    if (opt.isEmpty()) {
      msg(sender, "errors.arena_unknown", Map.of("arena", arenaId));
      return;
    }
    var arena = opt.get();
    switch (sub) {
      case "respawn" -> {
        plugin.npcs().despawnAll(arena);
        plugin.npcs().ensureSpawned(arena);
        sender.sendMessage(prefix + "PNJ respawnés.");
      }
      case "list" -> {
        var list = plugin.npcs().list(arena);
        sender.sendMessage(prefix + "PNJ actifs: " + list.size());
        for (var e : list) {
          var l = e.getLocation();
          sender.sendMessage(String.format(" - %s @ %.1f %.1f %.1f", e.getType().name(), l.getX(), l.getY(), l.getZ()));
        }
      }
      default -> sender.sendMessage(prefix + "Usage: /bwadmin npc <respawn|list> <arena>");
    }
  }

  private void handleArena(CommandSender sender, String[] args) {
    if (args.length < 2) {
      sender.sendMessage(plugin.messages().get("prefix") + "Usage: /bwadmin arena <create|list|save|reload|delete>");
      return;
    }
    String prefix = plugin.messages().get("prefix");
    switch (args[1].toLowerCase()) {
      case "create" -> {
        if (args.length < 3) {
          sender.sendMessage(prefix + "Usage: /bwadmin arena create <id> [world]");
          return;
        }
        String id = args[2];
        String worldName;
        if (args.length >= 4) {
          worldName = args[3];
        } else if (sender instanceof Player p) {
          worldName = p.getWorld().getName();
        } else {
          sender.sendMessage(prefix + plugin.messages().get("arena.world-required"));
          return;
        }
        try {
          plugin.arenas().create(id, new WorldRef(worldName));
          sender.sendMessage(prefix + String.format(plugin.messages().get("arena.created"), id));
        } catch (IllegalArgumentException ex) {
          sender.sendMessage(prefix + String.format(plugin.messages().get("arena.exists"), id));
        }
      }
      case "list" -> {
        List<String> ids = plugin.arenas().all().stream().map(Arena::id).toList();
        String joined = ids.isEmpty() ? "aucune" : String.join(", ", ids);
        sender.sendMessage(prefix + String.format(plugin.messages().get("arena.list"), joined));
      }
      case "save" -> {
        if (args.length < 3) {
          sender.sendMessage(prefix + "Usage: /bwadmin arena save <id>");
          return;
        }
        plugin.arenas().save(args[2]);
        sender.sendMessage(prefix + String.format(plugin.messages().get("arena.saved"), args[2]));
      }
      case "reload" -> {
        if (args.length < 3) {
          sender.sendMessage(prefix + "Usage: /bwadmin arena reload <id>");
          return;
        }
        plugin.arenas().load(args[2]);
        sender.sendMessage(prefix + String.format(plugin.messages().get("arena.reloaded"), args[2]));
      }
      case "delete" -> {
        if (args.length < 3) {
          sender.sendMessage(prefix + "Usage: /bwadmin arena delete <id>");
          return;
        }
        boolean ok = plugin.arenas().delete(args[2]);
        if (ok) {
          sender.sendMessage(prefix + String.format(plugin.messages().get("arena.deleted"), args[2]));
        } else {
          sender.sendMessage(prefix + String.format(plugin.messages().get("arena.missing"), args[2]));
        }
      }
      default -> sender.sendMessage(prefix + "Usage: /bwadmin arena <create|list|save|reload|delete>");
    }
  }

  private void handleDebug(CommandSender sender, String[] args) {
    if (!sender.hasPermission("bedwars.admin.debug")) {
      sender.sendMessage(plugin.messages().get("errors.no_perm"));
      return;
    }
    if (args.length < 3 || !args[1].equalsIgnoreCase("status")) {
      sender.sendMessage(plugin.messages().get("prefix") + "Usage: /bwadmin debug status <arena>");
      return;
    }
    onDebugStatus(sender, args[2]);
  }

  private void handleMaintenance(CommandSender sender, String[] args) {
    if (!sender.hasPermission("bedwars.admin.maintenance")) {
      sender.sendMessage(plugin.messages().get("errors.no_perm"));
      return;
    }
    if (args.length < 3 || !args[1].equalsIgnoreCase("cleanup")) {
      sender.sendMessage(plugin.messages().get("prefix") + "Usage: /bwadmin maintenance cleanup <arena>");
      return;
    }
    onMaintenanceCleanup(sender, args[2]);
  }

  private void msg(CommandSender s, String key, Map<String, ?> tokens) {
    s.sendMessage(plugin.messages().format(key, tokens));
  }

  private boolean onDebugStatus(CommandSender s, String id) {
    var opt = plugin.arenas().get(id);
    if (opt.isEmpty()) {
      msg(s, "errors.arena_unknown", Map.of("arena", id));
      return true;
    }
    Arena a = opt.get();
    msg(s, "debug.header", Map.of("arena", id, "state", a.state()));
    int total = plugin.contexts().countPlayers(id);
    int alive = (int) plugin.contexts().playersInArena(id).stream().filter(plugin.contexts()::isAlive).count();
    int spect = total - alive;
    msg(s, "debug.players", Map.of("total", total, "alive", alive, "spect", spect));
    for (TeamColor c : a.activeTeams()) {
      String bed = a.team(c).bedBlock() != null ? "✔" : "✖";
      int aliveTeam = plugin.contexts().aliveCount(id, c);
      msg(s, "debug.team_line", Map.of("color", c.color + c.display, "bed", bed, "alive", aliveTeam));
    }
    msg(s, "debug.gens_base", plugin.generators().baseSummary(id));
    msg(s, "debug.diamond", plugin.generators().diamondSummary(id));
    msg(s, "debug.emerald", plugin.generators().emeraldSummary(id));
    msg(s, "debug.events", plugin.game().timelineSummary(id));
    msg(s, "debug.tasks", plugin.tasks().summary(id));
    return true;
  }

  private boolean onMaintenanceCleanup(CommandSender s, String id) {
    var opt = plugin.arenas().get(id);
    if (opt.isEmpty()) {
      msg(s, "errors.arena_unknown", Map.of("arena", id));
      return true;
    }
    msg(s, "maintenance.start", Map.of("arena", id));
    Arena a = opt.get();
    int total = 0;
    Map<String, Integer> byType = new LinkedHashMap<>();
    NamespacedKey key = plugin.keys().ARENA_ID();
    World w = Bukkit.getWorld(a.world().name());
    if (w != null) {
      for (Entity e : new ArrayList<>(w.getEntities())) if (!(e instanceof Player)) {
        String tag = e.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (id.equals(tag)) {
          e.remove();
          total++;
          byType.merge(e.getType().name(), 1, Integer::sum);
        }
      }
    }
    String breakdown = byType.entrySet().stream()
        .map(en -> en.getKey() + "=" + en.getValue())
        .collect(Collectors.joining(", "));
    msg(s, "maintenance.done", Map.of("arena", id, "count", total, "breakdown", breakdown));
    return true;
  }
}
