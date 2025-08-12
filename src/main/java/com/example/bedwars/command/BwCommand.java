package com.example.bedwars.command;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.gui.AdminView;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BwCommand implements CommandExecutor {
  private final BedwarsPlugin plugin;

  public BwCommand(BedwarsPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage("Player only.");
      return true;
    }
    if (args.length >= 1 && args[0].equalsIgnoreCase("menu")) {
      if (!player.hasPermission("bedwars.admin")) {
        player.sendMessage(plugin.messages().get("admin.no-perm"));
        return true;
      }
      plugin.menus().open(AdminView.ROOT, player, null);
      return true;
    }
    List<String> lines = plugin.messages().getList("help.player");
    player.sendMessage(plugin.messages().get("prefix") + String.join("\n", lines));
    return true;
  }
}
