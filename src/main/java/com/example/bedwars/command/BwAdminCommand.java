package com.example.bedwars.command;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.arena.WorldRef;
import java.util.List;
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
    if (!sender.hasPermission("bedwars.admin.*")) {
      sender.sendMessage(plugin.messages().get("errors.no-perm"));
      return true;
    }
    if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
      List<String> lines = plugin.messages().getList("help.admin");
      sender.sendMessage(plugin.messages().get("prefix") + String.join("\n", lines));
      return true;
    }

    if (args[0].equalsIgnoreCase("arena")) {
      handleArena(sender, args);
      return true;
    }

    if (args[0].equalsIgnoreCase("game")) {
      handleGame(sender, args);
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
}
