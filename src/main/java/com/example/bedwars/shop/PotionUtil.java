package com.example.bedwars.shop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;

/** Factory for drinkable potion items. */
public final class PotionUtil {
  private PotionUtil() {}

  /**
   * Tries to apply the {@code HIDE_POTION_EFFECTS} flag if the running server
   * supports it. Some API builds omit the constant which would otherwise throw
   * an {@link IllegalArgumentException} during compilation.
   */
  public static void tryHidePotionEffects(ItemMeta meta) {
    if (meta == null) return;
    try {
      ItemFlag flag = ItemFlag.valueOf("HIDE_POTION_EFFECTS");
      meta.addItemFlags(flag);
    } catch (IllegalArgumentException ignored) {
      // Flag not present on this distribution; ignore silently.
    }
  }

  public static ItemStack mkPotion(PotionSpec spec) {
    ItemStack it = new ItemStack(Material.POTION);
    PotionMeta pm = (PotionMeta) it.getItemMeta();
    if (pm != null && spec != null) {
      // TODO: replace deprecated PotionData usage with PDC-based handling on
      // consumption via PlayerItemConsumeEvent.
      pm.setBasePotionData(new PotionData(spec.type(), false, spec.amplifier() > 1));
      tryHidePotionEffects(pm);
      it.setItemMeta(pm);
    }
    return it;
  }
}
