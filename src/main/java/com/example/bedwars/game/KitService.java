package com.example.bedwars.game;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

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
    if (plugin.getConfig().getBoolean("armor.dye_colored", true)) {
      p.getInventory().setHelmet(colored(Material.LEATHER_HELMET, team));
      p.getInventory().setChestplate(colored(Material.LEATHER_CHESTPLATE, team));
      p.getInventory().setLeggings(colored(Material.LEATHER_LEGGINGS, team));
      p.getInventory().setBoots(colored(Material.LEATHER_BOOTS, team));
    }
  }

  private ItemStack colored(Material type, TeamColor team) {
    ItemStack it = new ItemStack(type);
    var meta = (LeatherArmorMeta) it.getItemMeta();
    if (meta != null) {
      meta.setColor(toColor(team));
      it.setItemMeta(meta);
    }
    return it;
  }

  private Color toColor(TeamColor team) {
    return switch (team) {
      case RED -> Color.RED;
      case BLUE -> Color.BLUE;
      case GREEN -> Color.GREEN;
      case YELLOW -> Color.YELLOW;
      case AQUA -> Color.AQUA;
      case WHITE -> Color.WHITE;
      case PINK -> Color.FUCHSIA;
      case GRAY -> Color.GRAY;
    };
  }

  public void giveStartKit(Player p, TeamColor team) {
    giveBase(p, team);
  }

  public void giveRespawnKit(Player p, TeamColor team) {
    giveBase(p, team);
  }
}
