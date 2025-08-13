package com.example.bedwars.gen;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.ops.Keys;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/**
 * Handles spawning and updating holograms for diamond/emerald generators.
 */
public final class GenHologramService {
  private final BedwarsPlugin plugin;

  public GenHologramService(BedwarsPlugin plugin) {
    this.plugin = plugin;
  }

  public void updateOrCreate(String arenaId, UUID genId, Location loc, GeneratorType type, int tier, int seconds) {
    if (!plugin.getConfig().getBoolean("holograms.enabled", true)) return;
    double y = plugin.getConfig().getDouble("holograms.y_offset", 1.7);
    Location holoLoc = loc.clone().add(0, y, 0);

    TextDisplay td = null;
    for (Entity ent : holoLoc.getWorld().getNearbyEntities(holoLoc, 0.6, 1.2, 0.6, e -> e instanceof TextDisplay)) {
      var pdc = ent.getPersistentDataContainer();
      Keys keys = plugin.keys();
      String a = pdc.get(keys.ARENA_ID(), PersistentDataType.STRING);
      String gid = pdc.get(keys.GEN_ID(), PersistentDataType.STRING);
      if (arenaId.equals(a) && genId.toString().equals(gid) && pdc.has(keys.GEN_HOLO(), PersistentDataType.STRING)) {
        td = (TextDisplay) ent;
        break;
      }
    }
    if (td == null) {
      td = (TextDisplay) holoLoc.getWorld().spawn(holoLoc, TextDisplay.class, d -> {
        Keys keys = plugin.keys();
        var pdc = d.getPersistentDataContainer();
        pdc.set(keys.ARENA_ID(), PersistentDataType.STRING, arenaId);
        pdc.set(keys.GEN_ID(), PersistentDataType.STRING, genId.toString());
        pdc.set(keys.GEN_HOLO(), PersistentDataType.STRING, "1");
        d.setBillboard(Display.Billboard.CENTER);
        d.setShadowed(false);
        d.setSeeThrough(false);
        d.setViewRange(32);
      });
    }
    String line1 = plugin.getConfig().getString("holograms.line_1", "&b{type} &7T{tier}")
        .replace("{type}", type.name())
        .replace("{tier}", String.valueOf(tier));
    String line2 = plugin.getConfig().getString("holograms.line_2", "&f{cooldown}s")
        .replace("{cooldown}", String.valueOf(seconds));
    td.setText(color(line1) + "\n" + color(line2));
  }

  public void removeAll(String arenaId) {
    var keys = plugin.keys();
    for (var world : plugin.getServer().getWorlds()) {
      for (Entity ent : world.getEntitiesByClass(TextDisplay.class)) {
        var pdc = ent.getPersistentDataContainer();
        String a = pdc.get(keys.ARENA_ID(), PersistentDataType.STRING);
        if (arenaId.equals(a) && pdc.has(keys.GEN_HOLO(), PersistentDataType.STRING)) {
          ent.remove();
        }
      }
    }
  }

  private String color(String s) {
    return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
  }
}
