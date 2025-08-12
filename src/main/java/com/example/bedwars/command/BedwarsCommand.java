package com.example.bedwars.command;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            sender.sendMessage("Commandes joueurs uniquement");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            player.sendMessage("§e/bw list§7 - liste des arènes");
            player.sendMessage("§e/bw join <arène>§7 - rejoindre une arène");
            player.sendMessage("§e/bw leave§7 - quitter l'arène");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> {
                player.sendMessage("§aArènes disponibles: " + String.join(", ", plugin.getArenaManager().getArenas().keySet()));
                return true;
            }
            case "join" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /bw join <arène>");
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
                player.sendMessage("§cSous-commande inconnue");
                return true;
            }
        }
    }
}
