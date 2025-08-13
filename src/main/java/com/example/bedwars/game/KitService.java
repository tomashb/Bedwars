package com.example.bedwars.game;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;

/** Simple kit handling for start and respawn. */
public final class KitService {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;

  public KitService(BedwarsPlugin plugin, PlayerContextService ctx) {
    this.plugin = plugin; this.ctx = ctx;
  }

  private void giveBase(Player p, TeamColor team) {
    p.getInventory().clear();
    p.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
    if (plugin.getConfig().getBoolean("kit.give_blocks_on_spawn", false)) {
      p.getInventory().addItem(new ItemStack(team.wool, 16));
    }
    if (plugin.getConfig().getBoolean("kit.give_compass_on_spawn", false)) {
      ItemStack comp = new ItemStack(Material.COMPASS);
      var meta = comp.getItemMeta();
      if (meta != null) {
        meta.getPersistentDataContainer().set(plugin.keys().BW_ITEM(), PersistentDataType.STRING, "compass");
        comp.setItemMeta(meta);
      }
      p.getInventory().addItem(comp);
    }
    boolean dye = plugin.getConfig().getBoolean("armor.dye_colored", true);
    if (dye) {
      p.getInventory().setHelmet(colored(Material.LEATHER_HELMET, team));
      p.getInventory().setChestplate(colored(Material.LEATHER_CHESTPLATE, team));
    }
    int tier;
    if (plugin.getConfig().getBoolean("shop.armor_persistent", true)) {
      tier = ctx.getArmorTier(p);
    } else {
      tier = 0;
      ctx.setArmorTier(p, 0);
    }
    equipTier(p, team, tier, dye);
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

  private void equipTier(Player p, TeamColor team, int tier, boolean dye) {
    switch (tier) {
      case 3 -> {
        p.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        p.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
      }
      case 2 -> {
        p.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        p.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
      }
      case 1 -> {
        p.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        p.getInventory().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
      }
      default -> {
        if (dye) {
          p.getInventory().setLeggings(colored(Material.LEATHER_LEGGINGS, team));
          p.getInventory().setBoots(colored(Material.LEATHER_BOOTS, team));
        } else {
          p.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
          p.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
        }
      }
    }
  }
}
