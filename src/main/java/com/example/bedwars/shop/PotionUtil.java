package com.example.bedwars.shop;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/** Utility for creating tagged, drinkable potions. */
public final class PotionUtil {
  private PotionUtil() {}

  /**
   * Creates a basic {@link Material#POTION} tagged with a persistent id used
   * by {@link com.example.bedwars.shop.PotionConsumeListener}.
   */
  public static ItemStack makeTaggedPotion(JavaPlugin plugin, String id, String display) {
    ItemStack it = new ItemStack(Material.POTION);
    PotionMeta pm = (PotionMeta) it.getItemMeta();
    if (pm != null) {
      pm.displayName(Component.text(ChatColor.translateAlternateColorCodes('&', display)));
      try {
        pm.addItemFlags(ItemFlag.valueOf("HIDE_POTION_EFFECTS"));
      } catch (IllegalArgumentException ignored) {
        // Flag not supported on this server implementation.
      }
      pm.getPersistentDataContainer().set(new NamespacedKey(plugin, "bw_potion"), PersistentDataType.STRING, id);
      it.setItemMeta(pm);
    }
    return it;
  }
}
