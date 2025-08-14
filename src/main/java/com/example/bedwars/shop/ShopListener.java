package com.example.bedwars.shop;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.arena.TeamData;
import com.example.bedwars.game.PlayerContextService;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import com.example.bedwars.shop.UpgradesService.UpgradeDef;
import com.example.bedwars.shop.UpgradesService.TrapDef;
import com.example.bedwars.shop.TeamUpgradesState;
import com.example.bedwars.shop.UpgradeType;
import com.example.bedwars.shop.TrapType;
import com.example.bedwars.shop.PurchaseService;

/**
 * Handles opening shop menus and processing clicks.
 */
public final class ShopListener implements Listener {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;
  private final ItemShopMenu itemMenu;
  private final UpgradesGui upgradesMenu;

  public ShopListener(BedwarsPlugin plugin, PlayerContextService ctx) {
    this.plugin = plugin;
    this.ctx = ctx;
    this.itemMenu = new ItemShopMenu(plugin);
    this.upgradesMenu = new UpgradesGui(plugin);
  }

  @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
  public void onNpc(PlayerInteractEntityEvent e) {
    if (handleNpcClick(e.getPlayer(), e.getRightClicked())) e.setCancelled(true);
  }

  @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
  public void onNpc(PlayerInteractAtEntityEvent e) {
    if (handleNpcClick(e.getPlayer(), e.getRightClicked())) e.setCancelled(true);
  }

  private boolean handleNpcClick(Player p, org.bukkit.entity.Entity clicked) {
    if (!(clicked instanceof LivingEntity le)) return false;
    String arenaId = le.getPersistentDataContainer().get(plugin.keys().ARENA_ID(), PersistentDataType.STRING);
    String kind = le.getPersistentDataContainer().get(plugin.keys().NPC_KIND(), PersistentDataType.STRING);
    if (arenaId == null || kind == null) return false;

    String ar = ctx.getArena(p);
    TeamColor tm = ctx.getTeam(p);
    if (ar == null || tm == null || !ar.equals(arenaId)) { p.sendMessage(plugin.messages().get("shop.no-context")); return true; }
    if (kind.equalsIgnoreCase("item")) {
      if (!p.hasPermission("bedwars.menu.shop")) return true;
      itemMenu.open(p, arenaId, tm, ShopCategory.BLOCKS);
    } else if (kind.equalsIgnoreCase("upgrade")) {
      if (!p.hasPermission("bedwars.menu.upgrades")) return true;
      upgradesMenu.open(p, arenaId, tm);
    } else {
      return false;
    }
    return true;
  }

  @EventHandler
  public void onClick(InventoryClickEvent e) {
    Inventory top = e.getView().getTopInventory();
    if (top.getHolder() instanceof ItemShopMenu.Holder ih) {
      e.setCancelled(true);
      if (!(e.getWhoClicked() instanceof Player p)) return;
      int slot = e.getRawSlot();
      if (slot < 9) {
        if (slot == 0 || slot == 8) return; // border panes
        ShopCategory[] cats = ShopCategory.values();
        int idx = slot - 1;
        if (idx < 0 || idx >= cats.length) return;
        itemMenu.open(p, ih.arenaId, ih.team, cats[idx]);
        return;
      }
      int index = -1;
      int rel = slot - 19;
      if (rel >= 0) {
        int r = rel / 9;
        int c = rel % 9;
        if (c < 7 && r >=0 && r <4) index = r * 7 + c;
      }
      if (index < 0) return;
      java.util.List<ShopItem> list = plugin.shopConfig().items(ih.cat);
      if (ih.cat == ShopCategory.TOOLS) {
        if (index == 0) {
          plugin.tools().buyNextPick(p);
          itemMenu.open(p, ih.arenaId, ih.team, ih.cat);
          return;
        } else if (index == 1) {
          plugin.tools().buyNextAxe(p);
          // apply sharpness if team has upgrade
          plugin.arenas().get(ih.arenaId).ifPresent(arena -> {
            if (arena.team(ih.team).upgrades().sharpness()) {
              plugin.upgrades().applySharpness(ih.arenaId, ih.team);
            }
          });
          itemMenu.open(p, ih.arenaId, ih.team, ih.cat);
          return;
        }
        index -= 2;
      }
      if (index < 0 || index >= list.size()) return;
      ShopItem si = list.get(index);
      if (PurchaseService.tryBuy(p, si.currency, si.cost)) {
        Material mat = si.teamColored ? ih.team.wool : si.mat;
        ItemStack it = new ItemStack(mat, si.amount);
        si.enchants.forEach((en,l)-> it.addEnchantment(en,l));
        if (si.bwItem != null && !si.bwItem.isBlank()) {
          var meta = it.getItemMeta();
          meta.getPersistentDataContainer().set(plugin.keys().BW_ITEM(), PersistentDataType.STRING, si.bwItem);
          it.setItemMeta(meta);
        }
        if (mat.name().endsWith("_SWORD")) {
          for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack cur = p.getInventory().getItem(i);
            if (cur != null && cur.getType().name().endsWith("_SWORD")) {
              p.getInventory().setItem(i, null);
            }
          }
          p.getInventory().setItemInMainHand(it);
          plugin.arenas().get(ih.arenaId).ifPresent(arena -> {
            if (arena.team(ih.team).upgrades().sharpness()) {
              plugin.upgrades().applySharpness(ih.arenaId, ih.team);
            }
          });
        } else if (mat.name().endsWith("_BOOTS")) {
          applyArmorPurchase(p, mat);
        } else {
          p.getInventory().addItem(it);
        }
        String name = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', si.name.replace("{team}", ih.team.display)));
        p.sendMessage(plugin.messages().format("shop.bought", Map.of("item", name)));
      } else {
        String cur = si.currency.name();
        p.sendMessage(plugin.messages().format("shop.need", Map.of("amount", si.cost, "currency", cur)));
      }
      return;
    }

    if (top.getHolder() instanceof UpgradesGui.Holder uh) {
      e.setCancelled(true);
      if (!(e.getWhoClicked() instanceof Player p)) return;
      TeamData td = plugin.arenas().get(uh.arenaId).map(a->a.team(uh.team)).orElse(null);
      if (td == null) return;
      TeamUpgradesState st = td.upgrades();
      int slot = e.getRawSlot();

      if (slot == UpgradesGui.SLOT_SHARP) {
        UpgradeDef def = plugin.upgrades().def(UpgradeType.SHARPNESS);
        if (def == null) return;
        if (st.sharpness()) { p.sendMessage(plugin.messages().get("shop.maxed")); return; }
        int d = def.costForLevel(td.maxPlayers(), 1);
        if (plugin.upgrades().tryBuyDiamonds(p, d)) {
          st.setSharpness(true);
          plugin.upgrades().applySharpness(uh.arenaId, uh.team);
          p.sendMessage(plugin.messages().format("upgrades.bought", Map.of("name", def.name)));
          upgradesMenu.open(p, uh.arenaId, uh.team);
        }
      } else if (slot == UpgradesGui.SLOT_PROT) {
        UpgradeDef def = plugin.upgrades().def(UpgradeType.PROTECTION);
        if (def == null) return;
        int lvl = st.protection();
        if (lvl >= def.maxLevel()) { p.sendMessage(plugin.messages().get("shop.maxed")); return; }
        int d = def.costForLevel(td.maxPlayers(), lvl + 1);
        if (plugin.upgrades().tryBuyDiamonds(p, d)) {
          st.setProtection(lvl + 1);
          plugin.upgrades().applyProtection(uh.arenaId, uh.team, st.protection());
          String name = def.name.replace("{level}", String.valueOf(st.protection()));
          p.sendMessage(plugin.messages().format("upgrades.bought", Map.of("name", name)));
          upgradesMenu.open(p, uh.arenaId, uh.team);
        }
      } else if (slot == UpgradesGui.SLOT_HASTE) {
        UpgradeDef def = plugin.upgrades().def(UpgradeType.MANIC_MINER);
        if (def == null) return;
        int lvl = st.manicMiner();
        if (lvl >= def.maxLevel()) { p.sendMessage(plugin.messages().get("shop.maxed")); return; }
        int d = def.costForLevel(td.maxPlayers(), lvl + 1);
        if (plugin.upgrades().tryBuyDiamonds(p, d)) {
          st.setManicMiner(lvl + 1);
          plugin.upgrades().applyManicMiner(uh.arenaId, uh.team, st.manicMiner());
          String name = def.name.replace("{level}", String.valueOf(st.manicMiner()));
          p.sendMessage(plugin.messages().format("upgrades.bought", Map.of("name", name)));
          upgradesMenu.open(p, uh.arenaId, uh.team);
        }
      } else if (slot == UpgradesGui.SLOT_FORGE) {
        UpgradeDef def = plugin.upgrades().def(UpgradeType.FORGE);
        if (def == null) return;
        int lvl = st.forge();
        if (lvl >= def.maxLevel()) { p.sendMessage(plugin.messages().get("shop.maxed")); return; }
        int d = def.costForLevel(td.maxPlayers(), lvl + 1);
        if (plugin.upgrades().tryBuyDiamonds(p, d)) {
          st.setForge(lvl + 1);
          plugin.upgrades().applyForge(uh.arenaId, uh.team, st.forge());
          String name = def.name.replace("{level}", String.valueOf(st.forge()));
          p.sendMessage(plugin.messages().format("upgrades.bought", Map.of("name", name)));
          upgradesMenu.open(p, uh.arenaId, uh.team);
        }
      } else if (slot == UpgradesGui.SLOT_HEAL) {
        UpgradeDef def = plugin.upgrades().def(UpgradeType.HEAL_POOL);
        if (def == null) return;
        if (st.healPool()) { p.sendMessage(plugin.messages().get("shop.maxed")); return; }
        int d = def.costForLevel(td.maxPlayers(), 1);
        if (plugin.upgrades().tryBuyDiamonds(p, d)) {
          st.setHealPool(true);
          plugin.upgrades().applyHealPool(uh.arenaId, uh.team, true);
          p.sendMessage(plugin.messages().format("upgrades.bought", Map.of("name", def.name)));
          upgradesMenu.open(p, uh.arenaId, uh.team);
        }
      } else if (slot == UpgradesGui.SLOT_TRAP_MF) {
        TrapDef def = plugin.upgrades().trapDef(TrapType.MINING_FATIGUE);
        if (def != null && st.trapQueue().size() < 3) {
          int cost = plugin.upgrades().trapCost(st.trapQueue().size());
          if (plugin.upgrades().tryBuyDiamonds(p, cost)) {
            st.trapQueue().add(TrapType.MINING_FATIGUE);
            p.sendMessage(plugin.messages().format("upgrades.bought", Map.of("name", def.name)));
            upgradesMenu.open(p, uh.arenaId, uh.team);
          }
        }
      } else if (slot == UpgradesGui.SLOT_TRAP_BS) {
        TrapDef def = plugin.upgrades().trapDef(TrapType.BLIND_SLOW);
        if (def != null && st.trapQueue().size() < 3) {
          int cost = plugin.upgrades().trapCost(st.trapQueue().size());
          if (plugin.upgrades().tryBuyDiamonds(p, cost)) {
            st.trapQueue().add(TrapType.BLIND_SLOW);
            p.sendMessage(plugin.messages().format("upgrades.bought", Map.of("name", def.name)));
            upgradesMenu.open(p, uh.arenaId, uh.team);
          }
        }
      } else if (slot == UpgradesGui.SLOT_TRAP1 || slot == UpgradesGui.SLOT_TRAP2 || slot == UpgradesGui.SLOT_TRAP3) {
        if (e.isRightClick()) {
          int idx = (slot == UpgradesGui.SLOT_TRAP1) ? 0 : (slot == UpgradesGui.SLOT_TRAP2 ? 1 : 2);
          if (st.trapQueue().size() > idx) {
            java.util.Iterator<TrapType> it = st.trapQueue().iterator();
            for (int i = 0; i <= idx && it.hasNext(); i++) {
              TrapType t = it.next();
              if (i == idx) { it.remove(); break; }
            }
            upgradesMenu.open(p, uh.arenaId, uh.team);
          }
        }
      } else if (slot == UpgradesGui.SLOT_BACK) {
        p.closeInventory();
      }
    }
  }

  private void applyArmorPurchase(Player p, Material bootsMat) {
    int tier = switch (bootsMat) {
      case CHAINMAIL_BOOTS -> 1;
      case IRON_BOOTS -> 2;
      case DIAMOND_BOOTS -> 3;
      default -> 0;
    };
    int current = ctx.getArmorTier(p);
    if (tier <= current) return;
    ctx.setArmorTier(p, tier);
    switch (tier) {
      case 3 -> {
        p.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        p.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
      }
      case 2 -> {
        p.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        p.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
      }
      case 1 -> {
        p.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        p.getInventory().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
      }
    }
  }
}
