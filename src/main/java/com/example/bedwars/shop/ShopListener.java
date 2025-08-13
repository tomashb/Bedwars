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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import com.example.bedwars.shop.UpgradeService.UpgradeDef;
import com.example.bedwars.shop.UpgradeService.TrapDef;
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
    String arenaId = le.getPersistentDataContainer().get(plugin.keys().ARENA_ID(), PersistentDataType.STRING);
    String kind = le.getPersistentDataContainer().get(plugin.keys().NPC_KIND(), PersistentDataType.STRING);
    if (arenaId == null || kind == null) return;

    Player p = e.getPlayer();
    String ar = ctx.getArena(p);
    TeamColor tm = ctx.getTeam(p);
    if (ar == null || tm == null || !ar.equals(arenaId)) { p.sendMessage(plugin.messages().get("shop.no-context")); return; }
    if (kind.equalsIgnoreCase("item")) {
      itemMenu.open(p, arenaId, tm, ShopCategory.BLOCKS);
    } else if (kind.equalsIgnoreCase("upgrade")) {
      upgradesMenu.open(p, arenaId, tm);
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

    if (top.getHolder() instanceof TeamUpgradesMenu.Holder uh) {
      e.setCancelled(true);
      if (!(e.getWhoClicked() instanceof Player p)) return;
      TeamData td = plugin.arenas().get(uh.arenaId).map(a->a.team(uh.team)).orElse(null);
      if (td == null) return;
      TeamUpgradesState st = td.upgrades();
      int slot = e.getRawSlot();
      if (slot == TeamUpgradesMenu.SLOT_SHARP) {
        UpgradeService.UpgradeDef def = plugin.upgrades().def(UpgradeType.SHARPNESS);
        if (st.sharpness()) { p.sendMessage(plugin.messages().get("shop.maxed")); return; }
        int d = def.costDiamond;
        if (plugin.upgrades().tryBuyDiamonds(p, d)) {
          st.setSharpness(true);
          plugin.upgrades().applySharpness(uh.arenaId, uh.team);
          p.sendMessage(plugin.messages().format("upgrades.bought", Map.of("name", def.name)));
          upgradesMenu.open(p, uh.arenaId, uh.team);
        }
      } else if (slot == TeamUpgradesMenu.SLOT_PROT) {
        UpgradeService.UpgradeDef def = plugin.upgrades().def(UpgradeType.PROTECTION);
        int lvl = st.protection();
        if (lvl >= def.max) { p.sendMessage(plugin.messages().get("shop.maxed")); return; }
        int d = def.perLevel ? def.costDiamond * (lvl + 1) : def.costDiamond;
        if (plugin.upgrades().tryBuyDiamonds(p, d)) {
          st.setProtection(lvl+1);
          plugin.upgrades().applyProtection(uh.arenaId, uh.team, st.protection());
          String name = def.name.replace("{level}", String.valueOf(st.protection()));
          p.sendMessage(plugin.messages().format("upgrades.bought", Map.of("name", name)));
          upgradesMenu.open(p, uh.arenaId, uh.team);
        }
      } else if (slot == TeamUpgradesMenu.SLOT_HASTE) {
        UpgradeService.UpgradeDef def = plugin.upgrades().def(UpgradeType.MANIC_MINER);
        int lvl = st.manicMiner();
        if (lvl >= def.max) { p.sendMessage(plugin.messages().get("shop.maxed")); return; }
        int d = def.perLevel ? def.costDiamond * (lvl + 1) : def.costDiamond;
        if (plugin.upgrades().tryBuyDiamonds(p, d)) {
          st.setManicMiner(lvl+1);
          plugin.upgrades().applyManicMiner(uh.arenaId, uh.team, st.manicMiner());
          String name = def.name.replace("{level}", String.valueOf(st.manicMiner()));
          p.sendMessage(plugin.messages().format("upgrades.bought", Map.of("name", name)));
          upgradesMenu.open(p, uh.arenaId, uh.team);
        }
      } else if (slot == TeamUpgradesMenu.SLOT_FORGE) {
        UpgradeService.UpgradeDef def = plugin.upgrades().def(UpgradeType.FORGE);
        int lvl = st.forge();
        if (lvl >= def.max) { p.sendMessage(plugin.messages().get("shop.maxed")); return; }
        int d = def.perLevel ? def.costDiamond * (lvl + 1) : def.costDiamond;
        if (plugin.upgrades().tryBuyDiamonds(p, d)) {
          st.setForge(lvl+1);
          plugin.upgrades().applyForge(uh.arenaId, uh.team, st.forge());
          String name = def.name.replace("{level}", String.valueOf(st.forge()));
          p.sendMessage(plugin.messages().format("upgrades.bought", Map.of("name", name)));
          upgradesMenu.open(p, uh.arenaId, uh.team);
        }
      } else if (slot == TeamUpgradesMenu.SLOT_TRAP) {
        UpgradeService.TrapDef def = plugin.upgrades().trapDef(TrapType.ALARM);
        if (st.trapQueue().size() >= 3) { p.sendMessage(plugin.messages().get("shop.maxed")); return; }
        int d = def.costDiamond;
        if (plugin.upgrades().tryBuyDiamonds(p, d)) {
          st.trapQueue().add(TrapType.ALARM);
          p.sendMessage(plugin.messages().format("upgrades.bought", Map.of("name", def.name)));
          upgradesMenu.open(p, uh.arenaId, uh.team);
        }
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
