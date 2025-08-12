package com.example.bedwars.command;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Basic administrative command covering a tiny subset of the desired
 * functionality. It currently supports creating, deleting and listing
 * arenas as well as enabling or disabling the simplified event timeline.
 */
public class AdminCommand implements CommandExecutor, TabCompleter {

    private final BedwarsPlugin plugin;

    public AdminCommand(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bedwars.admin")) {
            sender.sendMessage(plugin.getMessages().get("error.not_admin"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getMessages().get("error.usage", Map.of("usage", "/bwadmin <arena|game|debug|maintenance> ...")));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "arena" -> {
                if (!sender.hasPermission("bedwars.admin.arena")) {
                    sender.sendMessage(plugin.getMessages().get("error.not_admin"));
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
            case "game" -> {
                if (!sender.hasPermission("bedwars.admin.game")) {
                    sender.sendMessage(plugin.getMessages().get("error.not_admin"));
                    return true;
                }
                if (args.length < 4 || !args[1].equalsIgnoreCase("events")) {
                    sender.sendMessage(plugin.getMessages().get("error.usage", Map.of("usage", "/bwadmin game events <arena> <enable|disable>")));
                    return true;
                }
                return plugin.getArenaManager().getArena(args[2]).map(arena -> {
                    boolean enable = args[3].equalsIgnoreCase("enable");
                    arena.setEventsEnabled(enable);
                    String key = enable ? "arena.events_enabled" : "arena.events_disabled";
                    sender.sendMessage(plugin.getMessages().get(key, Map.of("arena", arena.getName())));
                    return true;
                }).orElseGet(() -> {
                    sender.sendMessage(plugin.getMessages().get("error.no_arena"));
                    return true;
                });
            }
            case "debug" -> {
                if (args.length < 3 || !args[1].equalsIgnoreCase("status")) {
                    sender.sendMessage(plugin.getMessages().get("error.usage", Map.of("usage", "/bwadmin debug status <arena>")));
                    return true;
                }
                return plugin.getArenaManager().getArena(args[2]).map(arena -> {
                    sender.sendMessage(plugin.getMessages().get("debug.header", Map.of("arena", arena.getName())));
                    sender.sendMessage(plugin.getMessages().get("debug.state", Map.of("state", arena.getState().name())));
                    sender.sendMessage(plugin.getMessages().get("debug.players", Map.of("count", String.valueOf(arena.getPlayerCount()))));
                    sender.sendMessage(plugin.getMessages().get("debug.events", Map.of("status", arena.isEventsEnabled() ? "on" : "off")));
                    return true;
                }).orElseGet(() -> {
                    sender.sendMessage(plugin.getMessages().get("error.no_arena"));
                    return true;
                });
            }
            case "maintenance" -> {
                if (args.length < 3 || !args[1].equalsIgnoreCase("cleanup")) {
                    sender.sendMessage(plugin.getMessages().get("error.usage", Map.of("usage", "/bwadmin maintenance cleanup <arena>")));
                    return true;
                }
                int removed = 0;
                for (World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (entity instanceof Player player) {
                            continue; // never remove players
                        }
                        String tag = entity.getPersistentDataContainer().get(plugin.getArenaKey(), PersistentDataType.STRING);
                        if (tag != null && tag.equalsIgnoreCase(args[2])) {
                            entity.remove();
                            removed++;
                        }
                    }
                }
                sender.sendMessage(plugin.getMessages().get("maintenance.cleaned", Map.of("arena", args[2], "count", String.valueOf(removed))));
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
        if (!sender.hasPermission("bedwars.admin")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return List.of("arena", "game", "debug", "maintenance");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("arena")) {
                return List.of("create", "delete", "list");
            } else if (args[0].equalsIgnoreCase("game")) {
                return List.of("events");
            } else if (args[0].equalsIgnoreCase("debug")) {
                return List.of("status");
            } else if (args[0].equalsIgnoreCase("maintenance")) {
                return List.of("cleanup");
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("arena") && args[1].equalsIgnoreCase("delete")) {
                return new ArrayList<>(plugin.getArenaManager().getArenas().keySet());
            } else if (args[0].equalsIgnoreCase("game") && args[1].equalsIgnoreCase("events")) {
                return new ArrayList<>(plugin.getArenaManager().getArenas().keySet());
            } else if (args[0].equalsIgnoreCase("debug") && args[1].equalsIgnoreCase("status")) {
                return new ArrayList<>(plugin.getArenaManager().getArenas().keySet());
            } else if (args[0].equalsIgnoreCase("maintenance") && args[1].equalsIgnoreCase("cleanup")) {
                return new ArrayList<>(plugin.getArenaManager().getArenas().keySet());
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("arena") && args[1].equalsIgnoreCase("create")) {
                return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("game") && args[1].equalsIgnoreCase("events")) {
                return List.of("enable", "disable");
            }
        }
        return Collections.emptyList();
    }
}

