package com.example.bedwars.shop;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

/** Applies effects for drinkable shop potions tagged via PDC. */
public final class PotionConsumeListener implements Listener {
  private final JavaPlugin plugin;
  public PotionConsumeListener(JavaPlugin plugin) { this.plugin = plugin; }

  @EventHandler(ignoreCancelled = true)
  public void onConsume(PlayerItemConsumeEvent e) {
    ItemStack item = e.getItem();
    if (item.getType() != Material.POTION) return;
    ItemMeta meta = item.getItemMeta();
    if (meta == null) return;
    String id = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "bw_potion"), PersistentDataType.STRING);
    if (id == null) return;

    Player p = e.getPlayer();
    switch (id) {
      case "SPEED2_45" -> p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 45*20, 1, true, false, false));
      case "JUMP5_45" -> p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 45*20, 4, true, false, false));
      case "INVIS_30" -> p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 30*20, 0, true, false, false));
      default -> {}
    }
  }
}
