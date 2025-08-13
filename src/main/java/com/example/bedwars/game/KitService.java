package com.example.bedwars.game;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamColor;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Gives start/respawn kits to players.
 */
public final class KitService {
  private final BedwarsPlugin plugin;

  public KitService(BedwarsPlugin plugin) { this.plugin = plugin; }

  @SuppressWarnings("unchecked")
  private ItemStack resolveItem(Map<String, Object> map, TeamColor teamColor) {
    // MAT
    String matName = String.valueOf(map.get("mat"));
    Material mat;
    if ("WOOL_TEAM".equalsIgnoreCase(matName)) {
      mat = teamColor.wool;
    } else {
      mat = Material.matchMaterial(matName);
      if (mat == null) mat = Material.STONE;
    }

    // AMOUNT
    int amount = asInt(map.get("amount"), 1);

    ItemStack it = new ItemStack(mat, Math.max(1, amount));

    // NAME (optionnel)
    Object nameObj = map.get("name");
    if (nameObj != null) {
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
        String display = org.bukkit.ChatColor.translateAlternateColorCodes('&', String.valueOf(nameObj));
        meta.setDisplayName(display);
        it.setItemMeta(meta);
      }
    }

    // ENCHANTS (optionnel) : { SHARPNESS: 1, EFFICIENCY: 2 }
    Object enchObj = map.get("enchants");
    if (enchObj instanceof Map<?,?> em) {
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
        for (Map.Entry<?,?> e : em.entrySet()) {
          Enchantment ench = Enchantment.getByName(String.valueOf(e.getKey()));
          int lvl = asInt(e.getValue(), 1);
          if (ench != null) meta.addEnchant(ench, Math.max(1, lvl), true);
        }
        it.setItemMeta(meta);
      }
    }

    return it;
  }

  private static int asInt(Object o, int defVal) {
    if (o instanceof Number n) return n.intValue();
    if (o instanceof String s) {
      try {
        return Integer.parseInt(s.trim());
      } catch (NumberFormatException ignored) {
      }
    }
    return defVal;
  }

  @SuppressWarnings("unchecked")
  public void giveStartKit(Player p, TeamColor team) {
    if (!plugin.getConfig().getBoolean("kit.give-on-start", true)) return;
    var list = (List<Map<String, Object>>) plugin.getConfig().getList("kit.items", List.of());
    if (list.isEmpty()) {
      p.getInventory().addItem(new ItemStack(team.wool, 16));
      p.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
      p.getInventory().addItem(new ItemStack(Material.COMPASS));
      return;
    }
    for (Map<String, Object> entry : list) {
      ItemStack it = resolveItem(entry, team);
      p.getInventory().addItem(it);
    }
  }

  @SuppressWarnings("unchecked")
  public void giveRespawnKit(Player p, TeamColor team) {
    var list = (List<Map<String, Object>>) plugin.getConfig().getList("kit.respawn-items", List.of());
    if (list.isEmpty()) {
      p.getInventory().addItem(new ItemStack(team.wool, 8));
      p.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
      return;
    }
    for (Map<String, Object> entry : list) {
      ItemStack it = resolveItem(entry, team);
      p.getInventory().addItem(it);
    }
  }
}
