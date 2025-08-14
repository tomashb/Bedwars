package com.example.bedwars.command;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.game.GameService;
import com.example.bedwars.gui.AdminView;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BwCommand implements CommandExecutor {
  private final BedwarsPlugin plugin;
  private final GameService game;
  private final com.example.bedwars.lobby.LobbyService lobby;

  public BwCommand(BedwarsPlugin plugin) {
    this.plugin = plugin;
    this.game = plugin.game();
    this.lobby = plugin.lobby();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage("Player only.");
      return true;
    }
    if (args.length >= 1) {
      switch (args[0].toLowerCase()) {
        case "join":
          if (args.length < 2) {
            player.sendMessage(plugin.messages().get("game.no-arena"));
            return true;
          }
          game.join(player, args[1]);
          return true;
        case "leave":
          lobby.sendToLobby(player, com.example.bedwars.lobby.LobbyService.Reason.COMMAND);
          return true;
        case "team":
          if (args.length < 2) return true;
          try {
            TeamColor c = TeamColor.valueOf(args[1].toUpperCase());
            plugin.contexts().setTeam(player, c);
            player.sendMessage(plugin.messages().get("game.assign-team").replace("{team}", c.display));
          } catch (IllegalArgumentException ex) {
            player.sendMessage(plugin.messages().get("game.no-arena"));
          }
          return true;
        case "menu":
          if (!player.hasPermission("bedwars.admin.arena")) {
            player.sendMessage(plugin.messages().get("errors.no_perm"));
            return true;
          }
          plugin.menus().open(AdminView.ROOT, player, null);
          return true;
        default:
          break;
      }
    }
    List<String> lines = plugin.messages().getList("help.player");
    player.sendMessage(plugin.messages().get("prefix") + String.join("\n", lines));
    return true;
  }
}
