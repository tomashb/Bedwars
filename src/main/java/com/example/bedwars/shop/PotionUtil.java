package com.example.bedwars.shop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;

/** Factory for drinkable potion items. */
public final class PotionUtil {
  private PotionUtil() {}

  public static ItemStack mkPotion(PotionSpec spec) {
    ItemStack it = new ItemStack(Material.POTION);
    PotionMeta pm = (PotionMeta) it.getItemMeta();
    if (pm != null && spec != null) {
      pm.setBasePotionData(new PotionData(spec.type(), false, spec.amplifier() > 1));
      pm.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
      it.setItemMeta(pm);
    }
    return it;
  }
}
