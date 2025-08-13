package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.game.PlayerContextService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

/** Handles usage of fireball items from the shop. */
public final class FireballListener implements Listener {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;
  private final Map<UUID, Long> cooldown = new HashMap<>();

  public FireballListener(BedwarsPlugin plugin, PlayerContextService ctx) {
    this.plugin = plugin;
    this.ctx = ctx;
  }

  @EventHandler(ignoreCancelled = true)
  public void onInteract(PlayerInteractEvent e) {
    if (!plugin.getConfig().getBoolean("fireball.enabled", true)) return;
    if (e.getHand() != EquipmentSlot.HAND) return;
    ItemStack it = e.getItem();
    if (it == null) return;
    ItemMeta meta = it.getItemMeta();
    if (meta == null) return;
    String tag = meta.getPersistentDataContainer().get(plugin.keys().BW_ITEM(), PersistentDataType.STRING);
    if (!"fireball".equalsIgnoreCase(tag)) return;
    Action act = e.getAction();
    if (act != Action.RIGHT_CLICK_AIR && act != Action.RIGHT_CLICK_BLOCK) return;

    Player p = e.getPlayer();
    String arenaId = ctx.getArena(p);
    if (arenaId == null) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null || a.state() != GameState.RUNNING) return;

    long now = System.currentTimeMillis();
    long end = cooldown.getOrDefault(p.getUniqueId(), 0L);
    if (end > now) {
      plugin.messages().send(p, "fireball.cooldown");
      e.setCancelled(true);
      return;
    }

    e.setCancelled(true);
    // consume one item
    it.setAmount(it.getAmount() - 1);
    if (it.getAmount() <= 0) {
      p.getInventory().setItemInMainHand(null);
    } else {
      p.getInventory().setItemInMainHand(it);
    }

    double speed = plugin.getConfig().getDouble("fireball.speed", 1.2);
    boolean incendiary = plugin.getConfig().getBoolean("fireball.incendiary", false);
    double yield = plugin.getConfig().getDouble("fireball.explosion_yield", 2.5);
    Vector dir = p.getEyeLocation().getDirection().normalize().multiply(speed);
    String type = plugin.getConfig().getString("fireball.type", "BIG");
    if ("SMALL".equalsIgnoreCase(type)) {
      SmallFireball sf = p.launchProjectile(SmallFireball.class, dir);
      sf.setIsIncendiary(incendiary);
    } else {
      Fireball f = p.launchProjectile(Fireball.class, dir);
      f.setIsIncendiary(incendiary);
      f.setYield((float) yield);
    }

    long cd = plugin.getConfig().getLong("fireball.cooldown_ticks", 20);
    cooldown.put(p.getUniqueId(), now + cd * 50L);
  }
}

