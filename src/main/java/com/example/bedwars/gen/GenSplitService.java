package com.example.bedwars.gen;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

/**
 * Implements the generator split logic for team bases.
 */
public final class GenSplitService {
  private final BedwarsPlugin plugin;
  private final boolean enabled;

  public GenSplitService(BedwarsPlugin plugin) {
    this.plugin = plugin;
    this.enabled = plugin.getConfig().getBoolean("generators.split_generator", true);
  }

  public boolean isEnabled() { return enabled; }

  /**
   * Drop the given material for the players standing on the pad. If no players
   * are present, the item is simply dropped at the generator location.
   */
  public void distribute(RuntimeGen g, Material mat, int amount, Collection<Player> nearby,
                          AutoCollectService autoCollect, String arenaId) {
    ItemStack stack = new ItemStack(mat, amount);
    if (enabled && g.teamBase && !nearby.isEmpty()) {
      for (Player pl : nearby) {
        autoCollect.giveOrDrop(pl, stack.clone(), g.dropLoc, arenaId, g.id);
      }
    } else {
      autoCollect.giveOrDrop(null, stack, g.dropLoc, arenaId, g.id);
    }
  }
}
