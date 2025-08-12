package com.example.bedwars.command;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Very small command handler implementing only a subset of the
 * specification: list, join and leave. This is meant as a convenience
 * for manual testing of the skeleton plugin.
 */
public class BedwarsCommand implements CommandExecutor {

    private final BedwarsPlugin plugin;

    public BedwarsCommand(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessages().get("command.player-only"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            player.sendMessage(plugin.getMessages().get("command.help.list"));
            player.sendMessage(plugin.getMessages().get("command.help.join"));
            player.sendMessage(plugin.getMessages().get("command.help.leave"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> {
                String arenas = String.join(", ", plugin.getArenaManager().getArenas().keySet());
                player.sendMessage(plugin.getMessages().get("command.list", Map.of("arenas", arenas)));
                return true;
            }
            case "join" -> {
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessages().get("command.join-usage"));
                    return true;
                }
                plugin.getArenaManager().joinArena(player, args[1]);
                return true;
            }
            case "leave" -> {
                plugin.getArenaManager().leaveArena(player);
                return true;
            }
            default -> {
                player.sendMessage(plugin.getMessages().get("command.unknown"));
                return true;
            }
        }
    }
}
