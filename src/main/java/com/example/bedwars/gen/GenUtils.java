package com.example.bedwars.gen;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.shop.TeamUpgradesState;
import com.example.bedwars.ops.Keys;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Display;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;
import java.util.Collection;

/**
 * Helper utilities for generator runtime.
 */
public final class GenUtils {

  private GenUtils(){}

  // Is generator a base one (team generators near spawn)?
  public static boolean isBaseGenerator(Arena arena, com.example.bedwars.gen.Generator g, double radius) {
    if (g.type() != GeneratorType.TEAM_IRON &&
        g.type() != GeneratorType.TEAM_GOLD &&
        g.type() != GeneratorType.TEAM_EMERALD) return false;
    var loc = g.location();
    for (var entry : arena.teams().entrySet()) {
      var td = entry.getValue();
      if (td.spawn() == null) continue;
      if (!loc.getWorld().equals(td.spawn().getWorld())) continue;
      if (loc.distanceSquared(td.spawn()) <= radius * radius) return true;
    }
    return false;
  }

  // Compute forge multiplier (max level across enabled teams)
  public static double forgeMultiplier(Arena arena) {
    int lvl = 0;
    for (TeamColor tc : arena.enabledTeams()) {
      TeamUpgradesState up = arena.team(tc).upgrades();
      if (up != null) lvl = Math.max(lvl, up.forge());
    }
    return BedwarsPlugin.get().getConfig().getDouble("forge.multipliers." + lvl, 1.0);
  }

  // Drop the item corresponding to generator type and tag with PDC
  public static void drop(BedwarsPlugin plugin, String arenaId, UUID genId, RuntimeGen rg, int amount) {
    Material mat = Material.matchMaterial(plugin.getConfig().getString("drops." + rg.type.name()));
    if (mat == null) mat = Material.IRON_INGOT;
    ItemStack stack = new ItemStack(mat, amount);

    World world = rg.dropLoc.getWorld();
    world.dropItem(rg.dropLoc, stack, it -> {
      it.setVelocity(new org.bukkit.util.Vector(0, 0.1, 0));
      var pdc = it.getPersistentDataContainer();
      Keys keys = plugin.keys();
      pdc.set(keys.ARENA_ID(), PersistentDataType.STRING, arenaId);
      pdc.set(keys.GEN_ID(), PersistentDataType.STRING, genId.toString());
    });
  }

  // Hologram utilities (TextDisplay)
  public static void spawnOrUpdateHolo(BedwarsPlugin plugin, String arenaId, UUID genId, RuntimeGen rg) {
    updateHolo(plugin, arenaId, genId, rg); // update handles creation if absent
  }

  public static void updateHolo(BedwarsPlugin plugin, String arenaId, UUID genId, RuntimeGen rg) {
    if (!plugin.getConfig().getBoolean("holograms.enabled", true)) return;
    double y = plugin.getConfig().getDouble("holograms.y-offset", 1.7);
    Location holoLoc = rg.dropLoc.clone().add(0, y, 0);

    TextDisplay td = null;
    for (var ent : holoLoc.getWorld().getNearbyEntities(holoLoc, 0.6, 1.2, 0.6, e -> e instanceof TextDisplay)) {
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
    String l1 = color(plugin.getConfig().getString("holograms.line-1", "&b{type} &7T{tier}")
        .replace("{type}", rg.type.name())
        .replace("{tier}", String.valueOf(rg.tier)));
    String l2 = color(plugin.getConfig().getString("holograms.line-2", "&f{cooldown}s")
        .replace("{cooldown}", String.valueOf(Math.max(0, rg.cooldown / 20))));
    td.setText(l1 + "\n" + l2);
  }

  private static String color(String s) {
    return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
  }

  // Remove setup markers (ArmorStand/TextDisplay tagged GEN_MARKER)
  public static void removeSetupMarkers(Arena arena) {
    World w = arena.lobby() != null ? arena.lobby().getWorld() : Bukkit.getWorld(arena.world().name());
    if (w == null) return;
    for (var ent : w.getEntities()) {
      var pdc = ent.getPersistentDataContainer();
      Keys keys = BedwarsPlugin.get().keys();
      String a = pdc.get(keys.ARENA_ID(), PersistentDataType.STRING);
      if (arena.id().equals(a) && pdc.has(keys.GEN_MARKER(), PersistentDataType.STRING)) {
        ent.remove();
      }
    }
  }

  /** Count tagged items around a point belonging to a specific generator. */
  public static int countTaggedItemsAround(World w, Keys keys, String arenaId, UUID genId,
                                           Location center, double radius) {
    if (w == null) return 0;
    Collection<Entity> ents = w.getNearbyEntities(center, radius, radius, radius, e -> e instanceof Item);
    int count = 0;
    for (Entity e : ents) {
      Item it = (Item) e;
      var pdc = it.getPersistentDataContainer();
      String a = pdc.get(keys.ARENA_ID(), PersistentDataType.STRING);
      String g = pdc.get(keys.GEN_ID(), PersistentDataType.STRING);
      if (arenaId.equals(a) && genId.toString().equals(g)) {
        count += it.getItemStack().getAmount();
      }
    }
    return count;
  }

  /** List nearby players around a location within a radius using Spigot API. */
  public static java.util.List<Player> nearbyPlayers(World w, Location c, double r) {
    var ents = w.getNearbyEntities(c, r, r, r, e -> e instanceof Player);
    return ents.stream().map(e -> (Player) e).toList();
  }
}
