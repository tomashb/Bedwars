package com.example.bedwars.shop;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.arena.TeamData;
import com.example.bedwars.ops.Keys;
import com.example.bedwars.service.PlayerContextService;
import com.example.bedwars.service.PlayerContextService.Context;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/**
 * Handles opening shop menus and processing clicks.
 */
public final class ShopListener implements Listener {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;
  private final ItemShopMenu itemMenu;
  private final TeamUpgradesMenu upgradesMenu;

  public ShopListener(BedwarsPlugin plugin, PlayerContextService ctx) {
    this.plugin = plugin;
    this.ctx = ctx;
    this.itemMenu = new ItemShopMenu(plugin);
    this.upgradesMenu = new TeamUpgradesMenu(plugin);
  }

  @EventHandler
  public void onNpc(PlayerInteractEntityEvent e) {
    if (!(e.getRightClicked() instanceof LivingEntity le)) return;
    String arenaId = le.getPersistentDataContainer().get(Keys.ARENA_ID, PersistentDataType.STRING);
    String kind = le.getPersistentDataContainer().get(Keys.NPC_KIND, PersistentDataType.STRING);
    if (arenaId == null || kind == null) return;

    Player p = e.getPlayer();
    Context c = ctx.get(p).orElse(null);
    if (c == null) { p.sendMessage(plugin.messages().get("shop.no-context")); return; }
    if (kind.equalsIgnoreCase("item")) {
      itemMenu.open(p, arenaId, c.team, ShopCategory.BLOCKS);
    } else if (kind.equalsIgnoreCase("upgrade")) {
      upgradesMenu.open(p, arenaId, c.team);
    }
    e.setCancelled(true);
  }

  @EventHandler
  public void onClick(InventoryClickEvent e) {
    Inventory top = e.getView().getTopInventory();
    if (top.getHolder() instanceof ItemShopMenu.Holder ih) {
      e.setCancelled(true);
      if (!(e.getWhoClicked() instanceof Player p)) return;
      int slot = e.getRawSlot();
      if (slot < 9) {
        ShopCategory[] cats = ShopCategory.values();
        if (slot >= cats.length) return;
        itemMenu.open(p, ih.arenaId, ih.team, cats[slot]);
        return;
      }
      int index = slot - 9;
      java.util.List<ShopItem> list = plugin.shopConfig().items(ih.cat);
      if (index < 0 || index >= list.size()) return;
      ShopItem si = list.get(index);
      if (PriceUtil.hasAndRemove(p, si.price)) {
        Material mat = si.teamColored ? ih.team.wool : si.mat;
        ItemStack it = new ItemStack(mat, si.amount);
        si.enchants.forEach((en,l)-> it.addEnchantment(en,l));
        p.getInventory().addItem(it);
        String name = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', si.name.replace("{team}", ih.team.display)));
        p.sendMessage(plugin.messages().format("shop.bought", Map.of("item", name)));
      } else {
        p.sendMessage(plugin.messages().format("shop.not-enough", Map.of("cost", PriceUtil.formatCost(si.price))));
      }
      return;
    }

    if (top.getHolder() instanceof TeamUpgradesMenu.Holder uh) {
      e.setCancelled(true);
      if (!(e.getWhoClicked() instanceof Player p)) return;
      TeamData td = plugin.arenas().get(uh.arenaId).map(a->a.team(uh.team)).orElse(null);
      if (td == null) return;
      TeamUpgradesState st = td.upgrades();
      int slot = e.getRawSlot();
      if (slot == TeamUpgradesMenu.SLOT_SHARP) {
        ShopConfig.UpgradeDef def = plugin.shopConfig().upgrade(UpgradeType.SHARPNESS);
        if (st.sharpness()) { p.sendMessage(plugin.messages().get("shop.maxed")); return; }
        Map<Material,Integer> cost = def.costs.get(0);
        if (PriceUtil.hasAndRemove(p, cost)) {
          st.setSharpness(true);
          plugin.upgrades().applySharpness(uh.arenaId, uh.team);
          p.sendMessage(plugin.messages().format("shop.applied", Map.of("name", def.name)));
          upgradesMenu.open(p, uh.arenaId, uh.team);
        } else {
          p.sendMessage(plugin.messages().format("shop.not-enough", Map.of("cost", PriceUtil.formatCost(cost))));
        }
      } else if (slot == TeamUpgradesMenu.SLOT_PROT) {
        ShopConfig.UpgradeDef def = plugin.shopConfig().upgrade(UpgradeType.PROTECTION);
        int lvl = st.protection();
        if (lvl >= def.maxLevel) { p.sendMessage(plugin.messages().get("shop.maxed")); return; }
        Map<Material,Integer> cost = def.costs.get(lvl);
        if (PriceUtil.hasAndRemove(p, cost)) {
          st.setProtection(lvl+1);
          plugin.upgrades().applyProtection(uh.arenaId, uh.team, st.protection());
          String name = def.name.replace("{level}", String.valueOf(st.protection()));
          p.sendMessage(plugin.messages().format("shop.applied", Map.of("name", name)));
          upgradesMenu.open(p, uh.arenaId, uh.team);
        } else {
          p.sendMessage(plugin.messages().format("shop.not-enough", Map.of("cost", PriceUtil.formatCost(cost))));
        }
      } else if (slot == TeamUpgradesMenu.SLOT_HASTE) {
        ShopConfig.UpgradeDef def = plugin.shopConfig().upgrade(UpgradeType.MANIC_MINER);
        int lvl = st.manicMiner();
        if (lvl >= def.maxLevel) { p.sendMessage(plugin.messages().get("shop.maxed")); return; }
        Map<Material,Integer> cost = def.costs.get(lvl);
        if (PriceUtil.hasAndRemove(p, cost)) {
          st.setManicMiner(lvl+1);
          plugin.upgrades().applyManicMiner(uh.arenaId, uh.team, st.manicMiner());
          String name = def.name.replace("{level}", String.valueOf(st.manicMiner()));
          p.sendMessage(plugin.messages().format("shop.applied", Map.of("name", name)));
          upgradesMenu.open(p, uh.arenaId, uh.team);
        } else {
          p.sendMessage(plugin.messages().format("shop.not-enough", Map.of("cost", PriceUtil.formatCost(cost))));
        }
      } else if (slot == TeamUpgradesMenu.SLOT_HEAL) {
        ShopConfig.UpgradeDef def = plugin.shopConfig().upgrade(UpgradeType.HEAL_POOL);
        if (st.healPool()) { p.sendMessage(plugin.messages().get("shop.maxed")); return; }
        Map<Material,Integer> cost = def.costs.get(0);
        if (PriceUtil.hasAndRemove(p, cost)) {
          st.setHealPool(true);
          plugin.upgrades().applyHealPool(uh.arenaId, uh.team, true);
          p.sendMessage(plugin.messages().format("shop.applied", Map.of("name", def.name)));
          upgradesMenu.open(p, uh.arenaId, uh.team);
        } else {
          p.sendMessage(plugin.messages().format("shop.not-enough", Map.of("cost", PriceUtil.formatCost(cost))));
        }
      } else if (slot == TeamUpgradesMenu.SLOT_FORGE) {
        ShopConfig.UpgradeDef def = plugin.shopConfig().upgrade(UpgradeType.FORGE);
        int lvl = st.forge();
        if (lvl >= def.maxLevel) { p.sendMessage(plugin.messages().get("shop.maxed")); return; }
        Map<Material,Integer> cost = def.costs.get(lvl);
        if (PriceUtil.hasAndRemove(p, cost)) {
          st.setForge(lvl+1);
          plugin.upgrades().applyForge(uh.arenaId, uh.team, st.forge());
          String name = def.name.replace("{level}", String.valueOf(st.forge()));
          p.sendMessage(plugin.messages().format("shop.applied", Map.of("name", name)));
          upgradesMenu.open(p, uh.arenaId, uh.team);
        } else {
          p.sendMessage(plugin.messages().format("shop.not-enough", Map.of("cost", PriceUtil.formatCost(cost))));
        }
      } else if (slot == TeamUpgradesMenu.SLOT_TRAP) {
        ShopConfig.TrapDef def = plugin.shopConfig().trap(TrapType.ALARM);
        if (st.trapQueue().size() >= 3) { p.sendMessage(plugin.messages().get("shop.maxed")); return; }
        if (PriceUtil.hasAndRemove(p, def.cost)) {
          st.trapQueue().add(TrapType.ALARM);
          p.sendMessage(plugin.messages().format("shop.trap-added", Map.of("count", st.trapQueue().size())));
          upgradesMenu.open(p, uh.arenaId, uh.team);
        } else {
          p.sendMessage(plugin.messages().format("shop.not-enough", Map.of("cost", PriceUtil.formatCost(def.cost))));
        }
      }
    }
  }
}

