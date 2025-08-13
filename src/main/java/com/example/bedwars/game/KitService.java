package com.example.bedwars.game;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Simple kit handling for start and respawn. */
public final class KitService {
  private final BedwarsPlugin plugin;

  public KitService(BedwarsPlugin plugin) { this.plugin = plugin; }

  private void giveBase(Player p, TeamColor team) {
    p.getInventory().clear();
    p.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
    if (plugin.getConfig().getBoolean("kit.give_blocks_on_spawn", false)) {
      p.getInventory().addItem(new ItemStack(team.wool, 16));
    }
    if (plugin.getConfig().getBoolean("kit.give_compass_on_spawn", false)) {
      p.getInventory().addItem(new ItemStack(Material.COMPASS));
    }
  }

  public void giveStartKit(Player p, TeamColor team) {
    giveBase(p, team);
  }

  public void giveRespawnKit(Player p, TeamColor team) {
    giveBase(p, team);
  }
}
