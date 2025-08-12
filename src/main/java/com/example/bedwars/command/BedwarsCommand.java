package com.example.bedwars.command;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Very small command handler implementing only a subset of the
 * specification: list, join and leave. This is meant as a convenience
 * for manual testing of the skeleton plugin.
 */
public class BedwarsCommand implements CommandExecutor, TabCompleter {

    private final BedwarsPlugin plugin;

    public BedwarsCommand(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.messages().get("command.player-only"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            player.sendMessage(plugin.messages().get("command.help.list"));
            player.sendMessage(plugin.messages().get("command.help.join"));
            player.sendMessage(plugin.messages().get("command.help.leave"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "menu", "admin", "setup" -> {
                if (!player.hasPermission("bedwars.admin")) {
                    player.sendMessage(plugin.messages().get("error.not_admin"));
                    return true;
                }
                  plugin.menus().openRoot(player);
                return true;
            }
            case "list" -> {
                String arenas = String.join(", ", plugin.arenas().getArenas().keySet());
                player.sendMessage(plugin.messages().get("command.list", Map.of("arenas", arenas)));
                return true;
            }
            case "join" -> {
                if (args.length < 2) {
                    player.sendMessage(plugin.messages().get("command.join-usage"));
                    return true;
                }
                if (!player.hasPermission("bedwars.join")) {
                    player.sendMessage(plugin.messages().get("error.not_admin"));
                    return true;
                }
                plugin.arenas().joinArena(player, args[1]);
                return true;
            }
            case "leave" -> {
                plugin.arenas().leaveArena(player);
                return true;
            }
            default -> {
                player.sendMessage(plugin.messages().get("command.unknown"));
                return true;
            }
        }
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return java.util.Collections.emptyList();
        }
        if (args.length == 1) {
            java.util.List<String> base = new java.util.ArrayList<>();
            base.add("menu");
            base.add("list");
            base.add("join");
            base.add("leave");
            return base;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            return new java.util.ArrayList<>(plugin.arenas().getArenas().keySet());
        }
        return java.util.Collections.emptyList();
    }
}
