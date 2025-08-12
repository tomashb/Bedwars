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
 * Basic administrative command covering arena management and a few
 * diagnostic utilities.
 */
public class AdminCommand implements CommandExecutor, TabCompleter {

    private final BedwarsPlugin plugin;

    public AdminCommand(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bedwars.admin")) {
            sender.sendMessage(plugin.messages().get("error.not_admin"));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(plugin.messages().get("error.usage", Map.of("usage", "/bwadmin <arena|game|debug|maintenance>")));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "arena" -> {
                if (!sender.hasPermission("bedwars.admin.arena")) {
                    sender.sendMessage(plugin.messages().get("error.not_admin"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(plugin.messages().get("error.usage", Map.of("usage", "/bwadmin arena <create|delete|list|setlobby|save|reload>")));
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "list" -> {
                        String arenas = String.join(", ", plugin.arenas().getArenas().keySet());
                        sender.sendMessage(plugin.messages().get("command.list", Map.of("arenas", arenas)));
                        return true;
                    }
                    case "create" -> {
                        if (args.length < 4) {
                            sender.sendMessage(plugin.messages().get("error.usage", Map.of("usage", "/bwadmin arena create <id> <world>")));
                            return true;
                        }
                        World world = Bukkit.getWorld(args[3]);
                        if (world == null) {
                            sender.sendMessage(plugin.messages().get("error.no_world", Map.of("world", args[3])));
                            return true;
                        }
                        boolean created = plugin.arenas().create(args[2], world.getName());
                        if (created) {
                            sender.sendMessage(plugin.messages().get("arena.created", Map.of("arena", args[2])));
                        } else {
                            sender.sendMessage(plugin.messages().get("arena.exists", Map.of("arena", args[2])));
                        }
                        return true;
                    }
                    case "delete" -> {
                        if (args.length < 3) {
                            sender.sendMessage(plugin.messages().get("error.usage", Map.of("usage", "/bwadmin arena delete <id>")));
                            return true;
                        }
                        boolean deleted = plugin.arenas().deleteArena(args[2]);
                        if (deleted) {
                            sender.sendMessage(plugin.messages().get("arena.deleted", Map.of("arena", args[2])));
                        } else {
                            sender.sendMessage(plugin.messages().get("error.no_arena"));
                        }
                        return true;
                    }
                    case "setlobby" -> {
                        if (!(sender instanceof Player player)) {
                            sender.sendMessage(plugin.messages().get("command.player-only"));
                            return true;
                        }
                        if (args.length < 3) {
                            sender.sendMessage(plugin.messages().get("error.usage", Map.of("usage", "/bwadmin arena setlobby <id>")));
                            return true;
                        }
                        plugin.arenas().setLobby(args[2], player.getLocation());
                        sender.sendMessage(plugin.messages().get("wizard.lobby-set"));
                        return true;
                    }
                    case "save" -> {
                        if (args.length < 3) {
                            sender.sendMessage(plugin.messages().get("error.usage", Map.of("usage", "/bwadmin arena save <id>")));
                            return true;
                        }
                        plugin.arenas().save(args[2]);
                        sender.sendMessage(plugin.messages().get("wizard.saved"));
                        return true;
                    }
                    case "reload" -> {
                        if (args.length < 3) {
                            sender.sendMessage(plugin.messages().get("error.usage", Map.of("usage", "/bwadmin arena reload <id>")));
                            return true;
                        }
                        plugin.arenas().reload(args[2]);
                        sender.sendMessage(plugin.messages().get("wizard.reloaded"));
                        return true;
                    }
                    default -> {
                        sender.sendMessage(plugin.messages().get("command.unknown"));
                        return true;
                    }
                }
            }
            case "team" -> {
                if (!sender.hasPermission("bedwars.admin.team")) {
                    sender.sendMessage(plugin.messages().get("error.not_admin"));
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.messages().get("command.player-only"));
                    return true;
                }
                if (args.length < 4) {
                    sender.sendMessage(plugin.messages().get("error.usage", Map.of("usage", "/bwadmin team <setspawn|setbed> <arena> <team>")));
                    return true;
                }
                try {
                    var team = com.example.bedwars.arena.TeamColor.valueOf(args[3].toUpperCase());
                    if (args[1].equalsIgnoreCase("setspawn")) {
                        plugin.arenas().setTeamSpawn(args[2], team, player.getLocation());
                        sender.sendMessage(plugin.messages().get("wizard.spawn-set", Map.of("team", team.name())));
                        return true;
                    } else if (args[1].equalsIgnoreCase("setbed")) {
                        var block = player.getTargetBlockExact(5);
                        if (block != null && block.getType().name().endsWith("_BED")) {
                            var bed = (org.bukkit.block.data.type.Bed) block.getBlockData();
                            plugin.arenas().setTeamBed(args[2], team, block.getLocation(), bed.getFacing().name());
                            sender.sendMessage(plugin.messages().get("wizard.bed-set", Map.of("team", team.name())));
                        } else {
                            sender.sendMessage(plugin.messages().get("wizard.no-bed"));
                        }
                        return true;
                    }
                } catch (IllegalArgumentException ignored) {
                }
                sender.sendMessage(plugin.messages().get("command.unknown"));
                return true;
            }
            case "gen" -> {
                if (!sender.hasPermission("bedwars.admin.gen")) {
                    sender.sendMessage(plugin.messages().get("error.not_admin"));
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.messages().get("command.player-only"));
                    return true;
                }
                if (args.length < 4 || !args[1].equalsIgnoreCase("add")) {
                    sender.sendMessage(plugin.messages().get("error.usage", Map.of("usage", "/bwadmin gen add <arena> <type>")));
                    return true;
                }
                try {
                    var type = com.example.bedwars.generator.GeneratorType.valueOf(args[3].toUpperCase());
                    plugin.arenas().addGenerator(args[2], type, player.getLocation());
                    sender.sendMessage(plugin.messages().get("wizard.gen-added", Map.of("type", type.name())));
                    return true;
                } catch (IllegalArgumentException ex) {
                    sender.sendMessage(plugin.messages().get("command.unknown"));
                    return true;
                }
            }
            case "npc" -> {
                if (!sender.hasPermission("bedwars.admin.npc")) {
                    sender.sendMessage(plugin.messages().get("error.not_admin"));
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.messages().get("command.player-only"));
                    return true;
                }
                if (args.length < 4 || !args[1].equalsIgnoreCase("add")) {
                    sender.sendMessage(plugin.messages().get("error.usage", Map.of("usage", "/bwadmin npc add <arena> <item|upgrade>")));
                    return true;
                }
                String type = args[3].toLowerCase();
                if (type.equals("item")) {
                    plugin.arenas().addItemShop(args[2], player.getLocation());
                    player.getWorld().spawn(player.getLocation(), org.bukkit.entity.Villager.class, v -> {
                        v.setAI(false);
                        v.setInvulnerable(true);
                        v.setCollidable(false);
                        v.getPersistentDataContainer().set(plugin.arenaKey(), PersistentDataType.STRING, args[2]);
                        v.getPersistentDataContainer().set(plugin.npcKey(), PersistentDataType.STRING, "item");
                    });
                    sender.sendMessage(plugin.messages().get("wizard.npc-item"));
                    return true;
                } else if (type.equals("upgrade")) {
                    plugin.arenas().addUpgradeShop(args[2], player.getLocation());
                    player.getWorld().spawn(player.getLocation(), org.bukkit.entity.Villager.class, v -> {
                        v.setAI(false);
                        v.setInvulnerable(true);
                        v.setCollidable(false);
                        v.getPersistentDataContainer().set(plugin.arenaKey(), PersistentDataType.STRING, args[2]);
                        v.getPersistentDataContainer().set(plugin.npcKey(), PersistentDataType.STRING, "upgrade");
                    });
                    sender.sendMessage(plugin.messages().get("wizard.npc-upgrade"));
                    return true;
                }
                sender.sendMessage(plugin.messages().get("command.unknown"));
                return true;
            }
            case "game" -> {
                if (!sender.hasPermission("bedwars.admin.game")) {
                    sender.sendMessage(plugin.messages().get("error.not_admin"));
                    return true;
                }
                if (args.length < 4 || !args[1].equalsIgnoreCase("events")) {
                    sender.sendMessage(plugin.messages().get("error.usage", Map.of("usage", "/bwadmin game events <arena> <enable|disable>")));
                    return true;
                }
                return plugin.arenas().getArena(args[2]).map(arena -> {
                    boolean enable = args[3].equalsIgnoreCase("enable");
                    arena.setEventsEnabled(enable);
                    String key = enable ? "arena.events_enabled" : "arena.events_disabled";
                    sender.sendMessage(plugin.messages().get(key, Map.of("arena", arena.getName())));
                    return true;
                }).orElseGet(() -> {
                    sender.sendMessage(plugin.messages().get("error.no_arena"));
                    return true;
                });
            }
            case "debug" -> {
                if (args.length < 3 || !args[1].equalsIgnoreCase("status")) {
                    sender.sendMessage(plugin.messages().get("error.usage", Map.of("usage", "/bwadmin debug status <arena>")));
                    return true;
                }
                return plugin.arenas().getArena(args[2]).map(arena -> {
                    sender.sendMessage(plugin.messages().get("debug.header", Map.of("arena", arena.getName())));
                    sender.sendMessage(plugin.messages().get("debug.state", Map.of("state", arena.getState().name())));
                    sender.sendMessage(plugin.messages().get("debug.players", Map.of("count", String.valueOf(arena.getPlayerCount()))));
                    sender.sendMessage(plugin.messages().get("debug.events", Map.of("status", arena.isEventsEnabled() ? "on" : "off")));
                    return true;
                }).orElseGet(() -> {
                    sender.sendMessage(plugin.messages().get("error.no_arena"));
                    return true;
                });
            }
            case "maintenance" -> {
                if (args.length < 3 || !args[1].equalsIgnoreCase("cleanup")) {
                    sender.sendMessage(plugin.messages().get("error.usage", Map.of("usage", "/bwadmin maintenance cleanup <arena>")));
                    return true;
                }
                int removed = 0;
                for (World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (entity instanceof Player) {
                            continue; // do not remove players
                        }
                        String tag = entity.getPersistentDataContainer().get(plugin.arenaKey(), PersistentDataType.STRING);
                        if (tag != null && tag.equalsIgnoreCase(args[2])) {
                            entity.remove();
                            removed++;
                        }
                    }
                }
                sender.sendMessage(plugin.messages().get("maintenance.cleaned", Map.of("arena", args[2], "count", String.valueOf(removed))));
                return true;
            }
            default -> {
                sender.sendMessage(plugin.messages().get("command.unknown"));
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
            return List.of("arena", "team", "gen", "npc", "game", "debug", "maintenance");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("arena")) {
                return List.of("create", "delete", "list", "setlobby", "save", "reload");
            } else if (args[0].equalsIgnoreCase("team")) {
                return List.of("setspawn", "setbed");
            } else if (args[0].equalsIgnoreCase("gen")) {
                return List.of("add");
            } else if (args[0].equalsIgnoreCase("npc")) {
                return List.of("add");
            } else if (args[0].equalsIgnoreCase("game")) {
                return List.of("events");
            } else if (args[0].equalsIgnoreCase("debug")) {
                return List.of("status");
            } else if (args[0].equalsIgnoreCase("maintenance")) {
                return List.of("cleanup");
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("arena") && (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("setlobby") || args[1].equalsIgnoreCase("save") || args[1].equalsIgnoreCase("reload"))) {
                return new ArrayList<>(plugin.arenas().getArenas().keySet());
            } else if (args[0].equalsIgnoreCase("team")) {
                return new ArrayList<>(plugin.arenas().getArenas().keySet());
            } else if (args[0].equalsIgnoreCase("gen") && args[1].equalsIgnoreCase("add")) {
                return new ArrayList<>(plugin.arenas().getArenas().keySet());
            } else if (args[0].equalsIgnoreCase("npc") && args[1].equalsIgnoreCase("add")) {
                return new ArrayList<>(plugin.arenas().getArenas().keySet());
            } else if (args[0].equalsIgnoreCase("game") && args[1].equalsIgnoreCase("events")) {
                return new ArrayList<>(plugin.arenas().getArenas().keySet());
            } else if (args[0].equalsIgnoreCase("debug") && args[1].equalsIgnoreCase("status")) {
                return new ArrayList<>(plugin.arenas().getArenas().keySet());
            } else if (args[0].equalsIgnoreCase("maintenance") && args[1].equalsIgnoreCase("cleanup")) {
                return new ArrayList<>(plugin.arenas().getArenas().keySet());
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("arena") && args[1].equalsIgnoreCase("create")) {
                return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("team")) {
                return List.of("RED", "BLUE", "GREEN", "YELLOW", "AQUA", "WHITE", "PINK", "GRAY");
            } else if (args[0].equalsIgnoreCase("gen") && args[1].equalsIgnoreCase("add")) {
                return List.of("IRON", "GOLD", "DIAMOND", "EMERALD");
            } else if (args[0].equalsIgnoreCase("npc") && args[1].equalsIgnoreCase("add")) {
                return List.of("item", "upgrade");
            } else if (args[0].equalsIgnoreCase("game") && args[1].equalsIgnoreCase("events")) {
                return List.of("enable", "disable");
            }
        }
        return Collections.emptyList();
    }
}
