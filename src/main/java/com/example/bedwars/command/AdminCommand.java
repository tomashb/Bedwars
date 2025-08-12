package com.example.bedwars.command;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Basic administrative command covering a tiny subset of the desired
 * functionality. It currently supports creating, deleting and listing
 * arenas to illustrate how the real command framework could be wired.
 */
public class AdminCommand implements CommandExecutor, TabCompleter {

    private final BedwarsPlugin plugin;

    public AdminCommand(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bedwars.admin.arena")) {
            sender.sendMessage(plugin.getMessages().get("error.not_admin"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getMessages().get("error.usage", Map.of("usage", "/bwadmin arena <create|delete|list>")));
            return true;
        }

        if (!args[0].equalsIgnoreCase("arena")) {
            sender.sendMessage(plugin.getMessages().get("command.unknown"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessages().get("error.usage", Map.of("usage", "/bwadmin arena <create|delete|list>")));
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "list" -> {
                String arenas = String.join(", ", plugin.getArenaManager().getArenas().keySet());
                sender.sendMessage(plugin.getMessages().get("command.list", Map.of("arenas", arenas)));
                return true;
            }
            case "create" -> {
                if (args.length < 4) {
                    sender.sendMessage(plugin.getMessages().get("error.usage", Map.of("usage", "/bwadmin arena create <id> <world>")));
                    return true;
                }
                World world = Bukkit.getWorld(args[3]);
                if (world == null) {
                    sender.sendMessage(plugin.getMessages().get("error.no_world", Map.of("world", args[3])));
                    return true;
                }
                boolean created = plugin.getArenaManager().createArena(args[2], world.getName());
                if (created) {
                    sender.sendMessage(plugin.getMessages().get("arena.created", Map.of("arena", args[2])));
                } else {
                    sender.sendMessage(plugin.getMessages().get("arena.exists", Map.of("arena", args[2])));
                }
                return true;
            }
            case "delete" -> {
                if (args.length < 3) {
                    sender.sendMessage(plugin.getMessages().get("error.usage", Map.of("usage", "/bwadmin arena delete <id>")));
                    return true;
                }
                boolean deleted = plugin.getArenaManager().deleteArena(args[2]);
                if (deleted) {
                    sender.sendMessage(plugin.getMessages().get("arena.deleted", Map.of("arena", args[2])));
                } else {
                    sender.sendMessage(plugin.getMessages().get("error.no_arena"));
                }
                return true;
            }
            default -> {
                sender.sendMessage(plugin.getMessages().get("command.unknown"));
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("bedwars.admin.arena")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return Collections.singletonList("arena");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("arena")) {
            return List.of("create", "delete", "list");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("arena")) {
            if (args[1].equalsIgnoreCase("delete")) {
                return new ArrayList<>(plugin.getArenaManager().getArenas().keySet());
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("arena") && args[1].equalsIgnoreCase("create")) {
            return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
