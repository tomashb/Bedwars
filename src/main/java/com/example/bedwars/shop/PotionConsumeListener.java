package com.example.bedwars.shop;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Applies custom potion effects for shop potions. */
public final class PotionConsumeListener implements Listener {
  private final BedwarsPlugin plugin;
  public PotionConsumeListener(BedwarsPlugin plugin) { this.plugin = plugin; }

  @EventHandler
  public void onConsume(PlayerItemConsumeEvent e) {
    ItemStack it = e.getItem();
    if (it.getType() != Material.POTION) return;
    ItemMeta im = it.getItemMeta();
    if (im == null) return;
    String id = im.getPersistentDataContainer().get(plugin.keys().BW_ITEM(), PersistentDataType.STRING);
    if (id == null) return;
    ShopItem def = plugin.shopConfig().byId(id);
    if (def == null || def.potion == null) return;
    PotionSpec ps = def.potion;
    PotionEffectType pet = ps.type().getEffectType();
    if (pet != null) {
      e.getPlayer().addPotionEffect(new PotionEffect(pet, ps.seconds()*20, ps.amplifier()-1, true, !ps.hideParticles()));
    }
  }
}
