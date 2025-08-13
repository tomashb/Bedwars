package com.example.bedwars.gen;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.ops.Keys;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.UUID;

/**
 * Handles spawning and updating holograms for generators.
 */
public final class GenHologramService {
  private final BedwarsPlugin plugin;

  public GenHologramService(BedwarsPlugin plugin) {
    this.plugin = plugin;
  }

  public void spawnOrUpdate(String arenaId, RuntimeGen g, int count, boolean capReached) {
    if (!plugin.getConfig().getBoolean("holograms.enabled", true)) return;
    double y = plugin.getConfig().getDouble("holograms.y_offset", 1.7);
    Location holoLoc = g.dropLoc.clone().add(0, y, 0);

    TextDisplay td = null;
    for (var ent : holoLoc.getWorld().getNearbyEntities(holoLoc, 0.6, 1.2, 0.6, e -> e instanceof TextDisplay)) {
      var pdc = ent.getPersistentDataContainer();
      Keys keys = plugin.keys();
      String a = pdc.get(keys.ARENA_ID(), PersistentDataType.STRING);
      String gid = pdc.get(keys.GEN_ID(), PersistentDataType.STRING);
      if (arenaId.equals(a) && g.id.toString().equals(gid) && pdc.has(keys.GEN_HOLO(), PersistentDataType.STRING)) {
        td = (TextDisplay) ent;
        break;
      }
    }
    if (td == null) {
      td = (TextDisplay) holoLoc.getWorld().spawn(holoLoc, TextDisplay.class, d -> {
        Keys keys = plugin.keys();
        var pdc = d.getPersistentDataContainer();
        pdc.set(keys.ARENA_ID(), PersistentDataType.STRING, arenaId);
        pdc.set(keys.GEN_ID(), PersistentDataType.STRING, g.id.toString());
        pdc.set(keys.GEN_HOLO(), PersistentDataType.STRING, "1");
        d.setBillboard(Display.Billboard.CENTER);
        d.setShadowed(false);
        d.setSeeThrough(false);
        d.setViewRange(32);
      });
    }
    String line1 = plugin.getConfig().getString("holograms.line_1", "&b{type} &7T{tier}")
        .replace("{type}", g.type.name()).replace("{tier}", String.valueOf(g.tier));
    String line2 = plugin.getConfig().getString("holograms.line_2", "&f{cooldown}s &7(Cap {count}/{cap})")
        .replace("{cooldown}", String.valueOf(Math.max(0, g.cooldown / 20)))
        .replace("{count}", String.valueOf(count))
        .replace("{cap}", String.valueOf(g.cap));
    if (capReached) {
      line2 = plugin.messages().format("gens.cap_reached",
          Map.of("type", g.type.name(), "count", count, "cap", g.cap));
    }
    td.setText(color(line1) + "\n" + color(line2));
  }

  private String color(String s) {
    return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
  }
}
