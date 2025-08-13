package com.example.bedwars.util;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

public final class Compat {
  private Compat(){}

  public static Enchantment sharpness() {
    Enchantment e = Enchantment.SHARPNESS; // 1.21+
    if (e == null) e = Enchantment.getByKey(NamespacedKey.minecraft("damage_all"));
    return e;
  }
  public static Enchantment protection() {
    Enchantment e = Enchantment.PROTECTION; // 1.21+
    if (e == null) e = Enchantment.getByKey(NamespacedKey.minecraft("protection_environmental"));
    return e;
  }
  public static PotionEffectType haste() {
    PotionEffectType t = PotionEffectType.HASTE; // 1.21+
    if (t == null) t = PotionEffectType.getByName("FAST_DIGGING");
    return t;
  }
}
