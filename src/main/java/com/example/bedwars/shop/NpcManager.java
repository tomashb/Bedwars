package com.example.bedwars.shop;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;

/**
 * Handles spawning and cleanup of shop/upgrade NPCs.
 */
public final class NpcManager {
  private final BedwarsPlugin plugin;

  public NpcManager(BedwarsPlugin plugin) {
    this.plugin = plugin;
  }

  /** Ensure NPCs for the given arena are spawned next tick. */
  public void ensureSpawned(Arena arena) {
    Bukkit.getScheduler().runTask(plugin, () -> spawnAll(arena));
  }

  /** Spawn all NPCs configured for the arena. */
  public void spawnAll(Arena arena) {
    World w = Bukkit.getWorld(arena.world().name());
    if (w == null) return;
    for (NpcData def : arena.npcs()) {
      Location loc = def.location();
      Chunk c = loc.getChunk();
      if (!c.isLoaded()) c.load();
      Villager v = (Villager) w.spawnEntity(loc, EntityType.VILLAGER);
      v.setAI(false);
      v.setInvulnerable(true);
      v.setCollidable(false);
      v.setGravity(false);
      v.setCanPickupItems(false);
      v.setRemoveWhenFarAway(false);
      switch (def.type()) {
        case ITEM -> v.setCustomName(org.bukkit.ChatColor.GREEN + "Objets");
        case UPGRADE -> v.setCustomName(org.bukkit.ChatColor.AQUA + "Améliorations");
      }
      v.setCustomNameVisible(true);
      var pdc = v.getPersistentDataContainer();
      pdc.set(plugin.keys().ARENA_ID(), PersistentDataType.STRING, arena.id());
      pdc.set(plugin.keys().NPC_KIND(), PersistentDataType.STRING, def.type().name().toLowerCase());
    }
  }

  /** Remove all NPCs tagged for the arena. */
  public void despawnAll(Arena arena) {
    World w = Bukkit.getWorld(arena.world().name());
    if (w == null) return;
    NamespacedKey arenaKey = plugin.keys().ARENA_ID();
    NamespacedKey npcKey = plugin.keys().NPC_KIND();
    for (Entity e : new ArrayList<>(w.getEntities())) {
      if (e instanceof org.bukkit.entity.Player) continue;
      var pdc = e.getPersistentDataContainer();
      String aId = pdc.get(arenaKey, PersistentDataType.STRING);
      if (arena.id().equals(aId) && pdc.has(npcKey, PersistentDataType.STRING)) {
        e.remove();
      }
    }
  }

  /** List all active NPC entities for the arena. */
  public List<Entity> list(Arena arena) {
    World w = Bukkit.getWorld(arena.world().name());
    if (w == null) return List.of();
    NamespacedKey arenaKey = plugin.keys().ARENA_ID();
    NamespacedKey npcKey = plugin.keys().NPC_KIND();
    List<Entity> out = new ArrayList<>();
    for (Entity e : w.getEntities()) {
      if (e instanceof org.bukkit.entity.Player) continue;
      var pdc = e.getPersistentDataContainer();
      String aId = pdc.get(arenaKey, PersistentDataType.STRING);
      if (arena.id().equals(aId) && pdc.has(npcKey, PersistentDataType.STRING)) {
        out.add(e);
      }
    }
    return out;
  }
}
