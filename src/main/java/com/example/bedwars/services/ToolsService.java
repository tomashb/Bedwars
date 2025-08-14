package com.example.bedwars.services;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.shop.Currency;
import com.example.bedwars.shop.PurchaseService;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Manages permanent tool tiers for players (pickaxe progression).
 */
public final class ToolsService {
  public enum PickTier { T0, T1, T2, T3, T4 }
  public enum AxeTier { T0, T1, T2, T3, T4 }
  public record PickSpec(Material mat, int eff, Currency cur, int cost, PickTier requires) {}
  public record AxeSpec(Material mat, int eff, Currency cur, int cost, AxeTier requires) {}

  private final BedwarsPlugin plugin;
  private final Map<PickTier, PickSpec> specs = new EnumMap<>(PickTier.class);
  private final Map<AxeTier, AxeSpec> axeSpecs = new EnumMap<>(AxeTier.class);
  private final Map<UUID, PlayerData> data = new HashMap<>();
  private final File dataDir;
  private final boolean pickRequireSequential;
  private final boolean pickDowngradeOnDeath;
  private final boolean pickUnbreakable;
  private final boolean pickNoDrop;
  private final boolean axeRequireSequential;
  private final boolean axeDowngradeOnDeath;
  private final boolean axeUnbreakable;
  private final boolean axeNoDrop;

  private static final Enchantment EFFICIENCY_ENCH =
      resolveEnchant("DIG_SPEED", "EFFICIENCY", "efficiency");

  private static Enchantment resolveEnchant(String legacyField, String altField, String key) {
    try {
      var f = Enchantment.class.getField(legacyField);
      var v = f.get(null);
      if (v instanceof Enchantment e) return e;
    } catch (Throwable ignored) {
    }
    try {
      var f = Enchantment.class.getField(altField);
      var v = f.get(null);
      if (v instanceof Enchantment e) return e;
    } catch (Throwable ignored) {
    }
    try {
      Class<?> regClz = Class.forName("org.bukkit.Registry");
      var f = regClz.getField("ENCHANTMENT");
      var registry = f.get(null);
      var get = registry.getClass().getMethod("get", NamespacedKey.class);
      var v = get.invoke(registry, NamespacedKey.minecraft(key));
      if (v instanceof Enchantment e) return e;
    } catch (Throwable ignored) {
    }
    try {
      return Enchantment.getByName(legacyField);
    } catch (Throwable ignored) {
    }
    throw new IllegalStateException(
        "Impossible de résoudre l'enchant: " + legacyField + "/" + key);
  }

  static final class PlayerData {
    PickTier pickTier = PickTier.T0;
    AxeTier axeTier = AxeTier.T0;
    boolean hasShears = false;
  }

  public ToolsService(BedwarsPlugin plugin) {
    this.plugin = plugin;
    ConfigurationSection root = plugin.getConfig().getConfigurationSection("tools.pickaxe");
    if (root == null) root = plugin.getConfig().createSection("tools.pickaxe");
    this.pickRequireSequential = root.getBoolean("require_sequential", true);
    this.pickDowngradeOnDeath = root.getBoolean("downgrade_on_death", false);
    this.pickUnbreakable = root.getBoolean("unbreakable", true);
    this.pickNoDrop = root.getBoolean("no_drop", true);
    ConfigurationSection tiers = root.getConfigurationSection("tiers");
    if (tiers != null) {
      for (String k : tiers.getKeys(false)) {
        int idx;
        try { idx = Integer.parseInt(k); } catch (Exception ex) { continue; }
        ConfigurationSection t = tiers.getConfigurationSection(k);
        if (t == null) continue;
        Material mat = Material.matchMaterial(t.getString("material", "WOODEN_PICKAXE"));
        int eff = t.getInt("efficiency", 1);
        ConfigurationSection priceSec = t.getConfigurationSection("price");
        Currency cur = Currency.IRON;
        int cost = 0;
        if (priceSec != null && !priceSec.getKeys(false).isEmpty()) {
          String cKey = priceSec.getKeys(false).iterator().next();
          try { cur = Currency.valueOf(cKey.toUpperCase(Locale.ROOT)); } catch (Exception ignore) {}
          cost = priceSec.getInt(cKey);
        }
        PickTier req = PickTier.T0;
        int reqI = t.getInt("requires", 0);
        if (reqI >=0 && reqI < PickTier.values().length) req = PickTier.values()[reqI];
        specs.put(PickTier.values()[idx], new PickSpec(mat, eff, cur, cost, req));
      }
    }

    ConfigurationSection axeRoot = plugin.getConfig().getConfigurationSection("tools.axe");
    if (axeRoot == null) axeRoot = plugin.getConfig().createSection("tools.axe");
    this.axeRequireSequential = axeRoot.getBoolean("require_sequential", true);
    this.axeDowngradeOnDeath = axeRoot.getBoolean("downgrade_on_death", false);
    this.axeUnbreakable = axeRoot.getBoolean("unbreakable", true);
    this.axeNoDrop = axeRoot.getBoolean("no_drop", true);
    ConfigurationSection axeTiers = axeRoot.getConfigurationSection("tiers");
    if (axeTiers != null) {
      for (String k : axeTiers.getKeys(false)) {
        int idx;
        try { idx = Integer.parseInt(k); } catch (Exception ex) { continue; }
        ConfigurationSection t = axeTiers.getConfigurationSection(k);
        if (t == null) continue;
        Material mat = Material.matchMaterial(t.getString("material", "WOODEN_AXE"));
        int eff = t.getInt("efficiency", 1);
        ConfigurationSection priceSec = t.getConfigurationSection("price");
        Currency cur = Currency.IRON;
        int cost = 0;
        if (priceSec != null && !priceSec.getKeys(false).isEmpty()) {
          String cKey = priceSec.getKeys(false).iterator().next();
          try { cur = Currency.valueOf(cKey.toUpperCase(Locale.ROOT)); } catch (Exception ignore) {}
          cost = priceSec.getInt(cKey);
        }
        AxeTier req = AxeTier.T0;
        int reqI = t.getInt("requires", 0);
        if (reqI >=0 && reqI < AxeTier.values().length) req = AxeTier.values()[reqI];
        axeSpecs.put(AxeTier.values()[idx], new AxeSpec(mat, eff, cur, cost, req));
      }
    }
    this.dataDir = new File(plugin.getDataFolder(), "playerdata");
    dataDir.mkdirs();
  }

  private PlayerData data(Player p) {
    return data.computeIfAbsent(p.getUniqueId(), id -> loadData(id));
  }

  private PlayerData loadData(UUID id) {
    PlayerData d = new PlayerData();
    File f = new File(dataDir, id.toString() + ".json");
    if (f.exists()) {
      YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
      int pt = y.getInt("pickaxeTier", 0);
      if (pt >=0 && pt < PickTier.values().length) d.pickTier = PickTier.values()[pt];
      int at = y.getInt("axeTier", 0);
      if (at >=0 && at < AxeTier.values().length) d.axeTier = AxeTier.values()[at];
      d.hasShears = y.getBoolean("hasShears", false);
    }
    return d;
  }

  private void saveData(UUID id, PlayerData d) {
    File f = new File(dataDir, id.toString() + ".json");
    YamlConfiguration y = new YamlConfiguration();
    y.set("pickaxeTier", d.pickTier.ordinal());
    y.set("axeTier", d.axeTier.ordinal());
    y.set("hasShears", d.hasShears);
    try { y.save(f); } catch (IOException ex) { plugin.getLogger().warning("Save fail " + ex); }
  }

  public void load(Player p) { data(p); }
  public void save(Player p) { PlayerData d = data.get(p.getUniqueId()); if (d != null) saveData(p.getUniqueId(), d); }

  public PickTier next(PickTier cur){ return switch(cur){ case T0->PickTier.T1; case T1->PickTier.T2; case T2->PickTier.T3; case T3->PickTier.T4; default->PickTier.T4; }; }
  public AxeTier next(AxeTier cur){ return switch(cur){ case T0->AxeTier.T1; case T1->AxeTier.T2; case T2->AxeTier.T3; case T3->AxeTier.T4; default->AxeTier.T4; }; }

  private static String displayMat(Material m) {
    String n = m.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    return Character.toUpperCase(n.charAt(0)) + n.substring(1);
  }
  private static String displayCurrency(Currency c) {
    return switch (c) {
      case IRON -> "Fer"; case GOLD -> "Or"; case EMERALD -> "Émeraudes"; case DIAMOND -> "Diamants"; };
  }

  public ItemStack createPickaxeIcon(Player p) {
    PlayerData d = data(p);
    PickTier cur = d.pickTier;
    PickTier nxt = next(cur);
    ItemStack it;
    if (nxt == cur) {
      it = new ItemStack(Material.EMERALD);
    } else {
      PickSpec ns = specs.get(nxt);
      it = new ItemStack(ns != null ? ns.mat() : Material.WOODEN_PICKAXE);
    }
    ItemMeta im = it.getItemMeta();
    if (im != null) {
      im.setDisplayName(plugin.messages().get("shop.tools.pickaxe.title"));
      java.util.List<String> lore = new ArrayList<>();
      PickSpec cs = specs.get(cur);
      if (cs != null && cur != PickTier.T0) {
        lore.add(plugin.messages().format("shop.tools.pickaxe.current", Map.of("mat", displayMat(cs.mat()), "eff", cs.eff())));
      } else {
        lore.add(plugin.messages().format("shop.tools.pickaxe.current", Map.of("mat", "Aucune", "eff", 0)));
      }
      if (nxt == cur) {
        lore.add(plugin.messages().get("shop.tools.pickaxe.max"));
      } else {
        PickSpec ns = specs.get(nxt);
        boolean can = PurchaseService.count(p, ns.cur()) >= ns.cost();
        String price = (can ? ChatColor.GREEN : ChatColor.RED) + String.valueOf(ns.cost()) + " " + displayCurrency(ns.cur());
        lore.add(plugin.messages().format("shop.tools.pickaxe.next", Map.of("mat", displayMat(ns.mat()), "eff", ns.eff(), "price", price)));
      }
      lore.add(plugin.messages().get("shop.tools.pickaxe.permanent"));
      im.setLore(lore);
      it.setItemMeta(im);
    }
    return it;
  }

  public boolean buyNextPick(Player p) {
    PlayerData d = data(p);
    PickTier cur = d.pickTier;
    PickTier nxt = next(cur);
    if (nxt == cur) {
      p.sendMessage(plugin.messages().get("shop.tools.pickaxe.max"));
      return false;
    }
    PickSpec s = specs.get(nxt);
    if (requireSequential && s.requires() != cur) {
      p.sendMessage(plugin.messages().get("errors.pickaxe.seq"));
      return false;
    }
    if (!PurchaseService.tryBuy(p, s.cur(), s.cost())) {
      plugin.messages().send(p, "shop.need", Map.of("amount", s.cost(), "currency", s.cur().name()));
      return false;
    }
    d.pickTier = nxt;
    givePick(p, s);
    plugin.messages().send(p, "shop.tools.pickaxe.bought", Map.of("mat", displayMat(s.mat()), "eff", s.eff()));
    save(p);
    return true;
  }

  public void givePick(Player p) {
    PlayerData d = data(p);
    PickSpec s = specs.get(d.pickTier);
    if (s != null) givePick(p, s);
  }

  private void givePick(Player p, PickSpec s) {
    ItemStack it = new ItemStack(s.mat());
    ItemMeta m = it.getItemMeta();
    if (m != null) {
      // ignore level restriction so custom efficiency can exceed vanilla limits
      m.addEnchant(EFFICIENCY_ENCH, s.eff(), true);
      if (unbreakable) m.setUnbreakable(true);
      it.setItemMeta(m);
    }
    Inventory inv = p.getInventory();
    int slot = -1;
    for (int i = 0; i < inv.getSize(); i++) {
      ItemStack cur = inv.getItem(i);
      if (cur != null && cur.getType().name().endsWith("_PICKAXE")) { slot = i; break; }
    }
    if (slot >= 0) inv.setItem(slot, it); else inv.addItem(it);
  }

  public ItemStack createAxeIcon(Player p) {
    PlayerData d = data(p);
    AxeTier cur = d.axeTier;
    AxeTier nxt = next(cur);
    ItemStack it;
    if (nxt == cur) {
      it = new ItemStack(Material.EMERALD);
    } else {
      AxeSpec ns = axeSpecs.get(nxt);
      it = new ItemStack(ns != null ? ns.mat() : Material.WOODEN_AXE);
    }
    ItemMeta im = it.getItemMeta();
    if (im != null) {
      im.setDisplayName(plugin.messages().get("shop.tools.axe.title"));
      java.util.List<String> lore = new ArrayList<>();
      AxeSpec cs = axeSpecs.get(cur);
      if (cs != null && cur != AxeTier.T0) {
        lore.add(plugin.messages().format("shop.tools.axe.current", Map.of("mat", displayMat(cs.mat()), "eff", cs.eff())));
      } else {
        lore.add(plugin.messages().format("shop.tools.axe.current", Map.of("mat", "Aucune", "eff", 0)));
      }
      if (nxt == cur) {
        lore.add(plugin.messages().get("shop.tools.axe.max"));
      } else {
        AxeSpec ns = axeSpecs.get(nxt);
        boolean can = PurchaseService.count(p, ns.cur()) >= ns.cost();
        String price = (can ? ChatColor.GREEN : ChatColor.RED) + String.valueOf(ns.cost()) + " " + displayCurrency(ns.cur());
        lore.add(plugin.messages().format("shop.tools.axe.next", Map.of("mat", displayMat(ns.mat()), "eff", ns.eff(), "price", price)));
      }
      lore.add(plugin.messages().get("shop.tools.axe.permanent"));
      im.setLore(lore);
      it.setItemMeta(im);
    }
    return it;
  }

  public boolean buyNextAxe(Player p) {
    PlayerData d = data(p);
    AxeTier cur = d.axeTier;
    AxeTier nxt = next(cur);
    if (nxt == cur) {
      p.sendMessage(plugin.messages().get("shop.tools.axe.max"));
      return false;
    }
    AxeSpec s = axeSpecs.get(nxt);
    if (axeRequireSequential && s.requires() != cur) {
      p.sendMessage(plugin.messages().get("errors.axe.seq"));
      return false;
    }
    if (!PurchaseService.tryBuy(p, s.cur(), s.cost())) {
      plugin.messages().send(p, "shop.need", Map.of("amount", s.cost(), "currency", s.cur().name()));
      return false;
    }
    d.axeTier = nxt;
    giveAxe(p, s);
    plugin.messages().send(p, "shop.tools.axe.bought", Map.of("mat", displayMat(s.mat()), "eff", s.eff()));
    save(p);
    return true;
  }

  public void giveAxe(Player p) {
    PlayerData d = data(p);
    AxeSpec s = axeSpecs.get(d.axeTier);
    if (s != null) giveAxe(p, s);
  }

  private void giveAxe(Player p, AxeSpec s) {
    ItemStack it = new ItemStack(s.mat());
    ItemMeta m = it.getItemMeta();
    if (m != null) {
      m.addEnchant(EFFICIENCY_ENCH, s.eff(), true);
      if (axeUnbreakable) m.setUnbreakable(true);
      it.setItemMeta(m);
    }
    Inventory inv = p.getInventory();
    int slot = -1;
    for (int i = 0; i < inv.getSize(); i++) {
      ItemStack cur = inv.getItem(i);
      if (cur != null && cur.getType().name().endsWith("_AXE")) { slot = i; break; }
    }
    if (slot >= 0) inv.setItem(slot, it); else inv.addItem(it);
  }

  public void onRespawn(Player p) { givePick(p); giveAxe(p); }

  public void onDeath(Player p, java.util.List<ItemStack> drops) {
    PlayerData d = data(p);
    if (pickNoDrop) drops.removeIf(is -> is.getType().name().endsWith("_PICKAXE"));
    if (axeNoDrop) drops.removeIf(is -> is.getType().name().endsWith("_AXE"));
    if (pickDowngradeOnDeath && d.pickTier.ordinal() > 1) {
      d.pickTier = PickTier.values()[d.pickTier.ordinal() - 1];
    }
    if (axeDowngradeOnDeath && d.axeTier.ordinal() > 1) {
      d.axeTier = AxeTier.values()[d.axeTier.ordinal() - 1];
    }
  }

  public boolean canDrop(Player p, ItemStack it) {
    if ((pickNoDrop && it.getType().name().endsWith("_PICKAXE"))) {
      PlayerData d = data(p);
      return d.pickTier == PickTier.T0;
    }
    if ((axeNoDrop && it.getType().name().endsWith("_AXE"))) {
      PlayerData d = data(p);
      return d.axeTier == AxeTier.T0;
    }
    return true;
  }
}

