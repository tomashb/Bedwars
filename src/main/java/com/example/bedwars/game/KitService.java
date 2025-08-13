package com.example.bedwars.game;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamColor;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Gives start/respawn kits to players.
 */
public final class KitService {
  private final BedwarsPlugin plugin;

  public KitService(BedwarsPlugin plugin) { this.plugin = plugin; }

  private ItemStack resolveItem(Map<?,?> map, TeamColor team) {
    String matName = String.valueOf(map.get("mat"));
    int amount = Integer.parseInt(String.valueOf(map.getOrDefault("amount", 1)));
    Material mat;
    if ("WOOL_TEAM".equalsIgnoreCase(matName)) {
      mat = team.wool;
    } else {
      mat = Material.matchMaterial(matName);
      if (mat == null) mat = Material.STONE;
    }
    return new ItemStack(mat, amount);
  }

  public void giveStartKit(Player p, TeamColor team) {
    if (!plugin.getConfig().getBoolean("kit.give-on-start", true)) return;
    List<Map<?,?>> list = plugin.getConfig().getMapList("kit.items");
    if (list != null && !list.isEmpty()) {
      for (Map<?,?> m : list) p.getInventory().addItem(resolveItem(m, team));
    } else {
      p.getInventory().addItem(new ItemStack(team.wool, 16));
      p.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
      p.getInventory().addItem(new ItemStack(Material.COMPASS));
    }
  }

  public void giveRespawnKit(Player p, TeamColor team) {
    p.getInventory().addItem(new ItemStack(team.wool, 8));
    p.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
  }
}
