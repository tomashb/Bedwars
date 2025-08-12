package com.example.bedwars.shop;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.ArenaManager;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.util.C;
import com.example.bedwars.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Handles the team upgrade inventory and applying purchased upgrades to
 * players. This is intentionally lightweight and only implements a subset of
 * classic BedWars upgrades sufficient for testing.
 */
public class TeamUpgrades {
    public static final NamespacedKey KEY = new NamespacedKey(BedwarsPlugin.get(), "bw_upgrade");
    private final BedwarsPlugin plugin;
    private final ArenaManager arenas;

    public TeamUpgrades(BedwarsPlugin plugin, ArenaManager arenas){
        this.plugin = plugin; this.arenas = arenas;
    }

    public void open(Player p, Arena a){
        if (a==null){ return; }
        Inventory inv = Bukkit.createInventory(null, 27, C.color("&8Améliorations"));

        ItemStack sharp = new ItemBuilder(Material.IRON_SWORD)
                .name("§eSharpness")
                .lore("§7Coût: 4 Diamants")
                .build();
        mark(sharp, "sharpness"); inv.setItem(11, sharp);

        ItemStack armor = new ItemBuilder(Material.IRON_CHESTPLATE)
                .name("§eReinforced Armor")
                .lore("§7Coût: variable")
                .build();
        mark(armor, "armor"); inv.setItem(13, armor);

        ItemStack miner = new ItemBuilder(Material.GOLDEN_PICKAXE)
                .name("§eManic Miner")
                .lore("§7Coût: variable")
                .build();
        mark(miner, "miner"); inv.setItem(15, miner);

        ItemStack heal = new ItemBuilder(Material.BEACON)
                .name("§eHeal Pool")
                .lore("§7Coût: 3 Diamants")
                .build();
        mark(heal, "heal"); inv.setItem(22, heal);

        p.openInventory(inv);
    }

    private void mark(ItemStack it, String id){
        ItemMeta meta = it.getItemMeta();
        meta.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, id);
        it.setItemMeta(meta);
    }

    public void handleClick(InventoryClickEvent e){
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!e.getView().getTitle().equals(C.color("&8Améliorations"))) return;
        e.setCancelled(true);
        ItemStack it = e.getCurrentItem(); if (it==null) return;
        ItemMeta meta = it.getItemMeta(); if (meta==null) return;
        String id = meta.getPersistentDataContainer().get(KEY, PersistentDataType.STRING); if (id==null) return;
        Arena a = arenas.arenaOf(p); if (a==null) return;
        TeamColor team = a.getTeamOf(p.getUniqueId()); if (team==null) return;
        switch(id){
            case "sharpness" -> buySharpness(p,a,team);
            case "armor" -> buyArmor(p,a,team);
            case "miner" -> buyMiner(p,a,team);
            case "heal" -> buyHeal(p,a,team);
        }
    }

    private boolean payDiamonds(Player p, int amount){
        if (!p.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), amount)) return false;
        int left = amount;
        for (ItemStack it : p.getInventory().getContents()){
            if (it==null || it.getType()!=Material.DIAMOND) continue;
            int take = Math.min(left, it.getAmount());
            it.setAmount(it.getAmount()-take); left -= take; if (left<=0) break;
        }
        return true;
    }

    private void buySharpness(Player p, Arena a, TeamColor t){
        if (a.getSharpness(t) >= 1){ p.sendMessage(C.color("&cSharpness déjà débloqué.")); return; }
        if (!payDiamonds(p,4)){ p.sendMessage(C.msg("shop.not_enough")); return; }
        a.addSharpness(t);
        for (UUID id : a.getTeamPlayers(t)){
            Player pl = Bukkit.getPlayer(id); if (pl!=null) applyTo(pl, a);
        }
        p.sendMessage(C.color("&aSharpness I acheté."));
    }

    private void buyArmor(Player p, Arena a, TeamColor t){
        int level = a.getArmor(t);
        if (level >= 4){ p.sendMessage(C.color("&cArmure déjà au max.")); return; }
        int cost = switch(level){ case 0 -> 2; case 1 -> 4; case 2 -> 8; default -> 16; };
        if (!payDiamonds(p,cost)){ p.sendMessage(C.msg("shop.not_enough")); return; }
        a.addArmor(t);
        for (UUID id : a.getTeamPlayers(t)){
            Player pl = Bukkit.getPlayer(id); if (pl!=null) applyTo(pl, a);
        }
        p.sendMessage(C.color("&aArmure renforcée niveau " + a.getArmor(t) + "."));
    }

    private void buyMiner(Player p, Arena a, TeamColor t){
        int level = a.getMiner(t);
        if (level >= 2){ p.sendMessage(C.color("&cManic Miner déjà au max.")); return; }
        int cost = level==0?4:8;
        if (!payDiamonds(p,cost)){ p.sendMessage(C.msg("shop.not_enough")); return; }
        a.addMiner(t);
        for (UUID id : a.getTeamPlayers(t)){
            Player pl = Bukkit.getPlayer(id); if (pl!=null) applyTo(pl, a);
        }
        p.sendMessage(C.color("&aManic Miner niveau " + a.getMiner(t) + "."));
    }

    private void buyHeal(Player p, Arena a, TeamColor t){
        if (a.hasHeal(t)){ p.sendMessage(C.color("&cHeal Pool déjà acheté.")); return; }
        if (!payDiamonds(p,3)){ p.sendMessage(C.msg("shop.not_enough")); return; }
        a.setHeal(t);
        for (UUID id : a.getTeamPlayers(t)){
            Player pl = Bukkit.getPlayer(id); if (pl!=null) applyTo(pl, a);
        }
        p.sendMessage(C.color("&aHeal Pool activé."));
    }

    /** Applies current upgrades of the player's team. */
    public void applyTo(Player p){
        Arena a = arenas.arenaOf(p); if (a!=null) applyTo(p, a);
    }

    public void applyTo(Player p, Arena a){
        TeamColor t = a.getTeamOf(p.getUniqueId()); if (t==null) return;
        int sharp = a.getSharpness(t);
        if (sharp>0){
            for (ItemStack it : p.getInventory().getContents()){
                if (it==null || !it.getType().toString().endsWith("_SWORD")) continue;
                it.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, sharp);
            }
        }
        int arm = a.getArmor(t);
        if (arm>0){
            ItemStack[] armor = {p.getInventory().getHelmet(), p.getInventory().getChestplate(), p.getInventory().getLeggings(), p.getInventory().getBoots()};
            for (ItemStack piece : armor){
                if (piece!=null) piece.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, arm);
            }
        }
        int miner = a.getMiner(t);
        if (miner>0){
            p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, miner-1, true, false));
        }
        if (a.hasHeal(t)){
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, true, false));
        }
    }
}
