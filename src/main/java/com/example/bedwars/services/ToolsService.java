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
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Manages permanent tool tiers for players (pickaxe progression).
 */
public final class ToolsService {
  public enum PickTier { T0, T1, T2, T3, T4 }
  public record PickSpec(Material mat, int eff, Currency cur, int cost, PickTier requires) {}

  private final BedwarsPlugin plugin;
  private final Map<PickTier, PickSpec> specs = new EnumMap<>(PickTier.class);
  private final Map<UUID, PlayerData> data = new HashMap<>();
  private final File dataDir;
  private final boolean requireSequential;
  private final boolean downgradeOnDeath;
  private final boolean unbreakable;
  private final boolean noDrop;

  static final class PlayerData {
    PickTier pickTier = PickTier.T0;
    int axeTier = 0;
    boolean hasShears = false;
  }

  public ToolsService(BedwarsPlugin plugin) {
    this.plugin = plugin;
    ConfigurationSection root = plugin.getConfig().getConfigurationSection("tools.pickaxe");
    if (root == null) root = plugin.getConfig().createSection("tools.pickaxe");
    this.requireSequential = root.getBoolean("require_sequential", true);
    this.downgradeOnDeath = root.getBoolean("downgrade_on_death", false);
    this.unbreakable = root.getBoolean("unbreakable", true);
    this.noDrop = root.getBoolean("no_drop", true);
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
      d.axeTier = y.getInt("axeTier", 0);
      d.hasShears = y.getBoolean("hasShears", false);
    }
    return d;
  }

  private void saveData(UUID id, PlayerData d) {
    File f = new File(dataDir, id.toString() + ".json");
    YamlConfiguration y = new YamlConfiguration();
    y.set("pickaxeTier", d.pickTier.ordinal());
    y.set("axeTier", d.axeTier);
    y.set("hasShears", d.hasShears);
    try { y.save(f); } catch (IOException ex) { plugin.getLogger().warning("Save fail " + ex); }
  }

  public void load(Player p) { data(p); }
  public void save(Player p) { PlayerData d = data.get(p.getUniqueId()); if (d != null) saveData(p.getUniqueId(), d); }

  public PickTier next(PickTier cur){ return switch(cur){ case T0->PickTier.T1; case T1->PickTier.T2; case T2->PickTier.T3; case T3->PickTier.T4; default->PickTier.T4; }; }

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
      m.addEnchant(Enchantment.DIG_SPEED, s.eff(), true);
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

  public void onRespawn(Player p) { givePick(p); }

  public void onDeath(Player p, java.util.List<ItemStack> drops) {
    PlayerData d = data(p);
    if (noDrop) drops.removeIf(is -> is.getType().name().endsWith("_PICKAXE"));
    if (downgradeOnDeath && d.pickTier.ordinal() > 1) {
      d.pickTier = PickTier.values()[d.pickTier.ordinal() - 1];
    }
  }

  public boolean canDrop(Player p, ItemStack it) {
    if (!noDrop) return true;
    if (it.getType().name().endsWith("_PICKAXE")) {
      PlayerData d = data(p);
      return d.pickTier == PickTier.T0;
    }
    return true;
  }
}

