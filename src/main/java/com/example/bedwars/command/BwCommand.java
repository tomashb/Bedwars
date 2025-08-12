package com.example.bedwars.command;

import com.example.bedwars.BedwarsPlugin;
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
    List<String> lines = plugin.messages().getList("help.player");
    player.sendMessage(plugin.messages().get("prefix") + String.join("\n", lines));
    return true;
  }
}
