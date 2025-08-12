package com.example.bedwars.shop;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.util.C;
import com.example.bedwars.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class ShopManager {
    private final BedwarsPlugin plugin; private final Map<String, Category> categories = new LinkedHashMap<>();
    public ShopManager(BedwarsPlugin plugin){ this.plugin=plugin; load(); }

    public void load(){
        categories.clear();
        File f = new File(plugin.getDataFolder(), "shops/itemshop.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        ConfigurationSection cats = cfg.getConfigurationSection("categories"); if (cats==null) return;
        for (String name : cats.getKeys(false)){
            ConfigurationSection sec = cats.getConfigurationSection(name);
            Category cat = new Category(name, Material.valueOf(sec.getString("icon","CHEST")));
            for (Map<?,?> im : sec.getMapList("items")){ ItemTemplate t = ItemTemplate.fromMap(im); cat.items.add(t); }
            categories.put(name, cat);
        }
    }

    public void open(Player p){
        Inventory inv = Bukkit.createInventory(null, 54, C.color("&8Boutique"));
        int slot = 0; for (Category c : categories.values()){
            inv.setItem(slot++, new ItemBuilder(c.icon).name("§e"+c.name).lore("§7Clique pour ouvrir").build());
        }
        p.openInventory(inv);
    }

    public void handleClick(InventoryClickEvent e){
        if (e.getView().getTitle().equals(C.color("&8Boutique"))){
            e.setCancelled(true); if (e.getCurrentItem()==null) return;
            int slot = e.getSlot(); int index = 0;
            for (Category c : categories.values()){ if (slot==index){ openCategory((Player)e.getWhoClicked(), c); return; } index++; }
        } else if (e.getView().getTitle().startsWith(C.color("&8Catégorie: "))){
            e.setCancelled(true); if (e.getCurrentItem()==null) return;
            ItemStack it = e.getCurrentItem(); ItemMeta meta = it.getItemMeta(); if (meta==null) return;
            String key = meta.getPersistentDataContainer().get(ItemTemplate.KEY, org.bukkit.persistence.PersistentDataType.STRING); if (key==null) key="";
            ItemTemplate tpl = ItemTemplate.registry.get(key); if (tpl==null) return;
            Player p = (Player)e.getWhoClicked(); if (!tpl.pay(p)){ p.sendMessage(C.msg("shop.not_enough")); return; }
            tpl.give(p, plugin); p.sendMessage(C.msg("shop.bought","item",tpl.name));
        }
    }

    private void openCategory(Player p, Category c){
        Inventory inv = Bukkit.createInventory(null, 54, C.color("&8Catégorie: " + c.name));
        int i=0; for (ItemTemplate t : c.items) inv.setItem(i++, t.icon());
        p.openInventory(inv);
    }

    public static class Category { final String name; final Material icon; final List<ItemTemplate> items = new ArrayList<>(); public Category(String name, Material icon){ this.name=name; this.icon=icon; } }

    public static class ItemTemplate {
        static final java.util.Map<String, ItemTemplate> registry = new java.util.HashMap<>();
        public static final org.bukkit.NamespacedKey KEY = new org.bukkit.NamespacedKey(BedwarsPlugin.get(), "shop_key");

        final String id; final String name; final Material material; final int amount;
        final Map<Material,Integer> price; final Map<Enchantment,Integer> enchants; final String armorTier;
        final boolean teamColored;

        ItemTemplate(String id, String name, Material material, int amount, Map<Material,Integer> price, Map<Enchantment,Integer> enchants, String armorTier, boolean teamColored){
            this.id=id; this.name=name; this.material=material; this.amount=amount; this.price=price; this.enchants=enchants; this.armorTier=armorTier; this.teamColored=teamColored; registry.put(id,this);
        }

        @SuppressWarnings("unchecked")
        static ItemTemplate fromMap(Map<?,?> m){
            String name = String.valueOf(m.containsKey("name")? m.get("name"): "Item");
            String id = name.toLowerCase(java.util.Locale.ROOT).replace(" ", "_");
            String matName = String.valueOf(m.containsKey("material")? m.get("material"): "AIR");

            boolean teamColored = false;
            Object teamFlag = m.get("team-colored");
            if (teamFlag instanceof Boolean b) teamColored = b;
            else if (teamFlag != null) teamColored = Boolean.parseBoolean(String.valueOf(teamFlag));

            Material mat;
            if ("TEAM_WOOL".equalsIgnoreCase(matName)){ teamColored=true; mat = Material.WHITE_WOOL; }
            else mat = Material.valueOf(matName);

            Object amountObj = m.containsKey("amount")? m.get("amount"): 1;
            int amount = (amountObj instanceof Number)? ((Number)amountObj).intValue(): Integer.parseInt(String.valueOf(amountObj));

            Map<Material, Integer> price = new java.util.HashMap<>();
            Object priceObj = m.get("price");
            if (priceObj instanceof java.util.Map<?,?> pr) {
                for (java.util.Map.Entry<?,?> e : pr.entrySet()) {
                    String k = String.valueOf(e.getKey());
                    Material res = org.bukkit.Material.matchMaterial(k);
                    if (res == null) {
                        if (k.equalsIgnoreCase("IRON"))    res = Material.IRON_INGOT;
                        else if (k.equalsIgnoreCase("GOLD"))   res = Material.GOLD_INGOT;
                        else if (k.equalsIgnoreCase("DIAMOND"))res = Material.DIAMOND;
                        else if (k.equalsIgnoreCase("EMERALD"))res = Material.EMERALD;
                    }
                    if (res != null) {
                        Object v = e.getValue();
                        int amt = (v instanceof Number) ? ((Number) v).intValue()
                                                        : Integer.parseInt(String.valueOf(v));
                        price.put(res, amt);
                    }
                }
            }

            Map<org.bukkit.enchantments.Enchantment, Integer> ench = new java.util.HashMap<>();
            Object enchObj = m.get("enchantments");
            if (enchObj instanceof java.util.Map<?,?> er) {
                for (java.util.Map.Entry<?,?> e : er.entrySet()) {
                    String key = String.valueOf(e.getKey()).toUpperCase(java.util.Locale.ROOT);
                    org.bukkit.enchantments.Enchantment en = org.bukkit.enchantments.Enchantment.getByName(key);
                    if (en != null) {
                        Object v = e.getValue();
                        int lvl = (v instanceof Number) ? ((Number) v).intValue()
                                                        : Integer.parseInt(String.valueOf(v));
                        ench.put(en, lvl);
                    }
                }
            }

            String armorTier = (String)(m.containsKey("armor-tier")? m.get("armor-tier"): null);
            return new ItemTemplate(id, name, mat, amount, price, ench, armorTier, teamColored);
        }

        public ItemStack icon(){
            ItemBuilder ib = new ItemBuilder(material).name("§e"+name);
            java.util.List<String> lore = new java.util.ArrayList<>(); if (!price.isEmpty()) lore.add("§7Prix:");
            for (Map.Entry<Material,Integer> e : price.entrySet()) lore.add("§f- "+e.getValue()+" "+e.getKey().name());
            if (!enchants.isEmpty()) lore.add("§7Enchantements inclus");
            ib.lore(lore);
            ItemStack it = ib.build(); ItemMeta meta = it.getItemMeta();
            meta.getPersistentDataContainer().set(KEY, org.bukkit.persistence.PersistentDataType.STRING, id); it.setItemMeta(meta);
            return it;
        }

        public boolean pay(Player p){
            for (Map.Entry<Material,Integer> e : price.entrySet()) if (!p.getInventory().containsAtLeast(new ItemStack(e.getKey()), e.getValue())) return false;
            for (Map.Entry<Material,Integer> e : price.entrySet()) removeItems(p, e.getKey(), e.getValue()); return true;
        }
        private void removeItems(Player p, Material m, int amount){
            int left = amount; for (ItemStack it : p.getInventory().getContents()){ if (it==null||it.getType()!=m) continue; int take=Math.min(left,it.getAmount()); it.setAmount(it.getAmount()-take); left-=take; if (left<=0) break; }
        }
        public void give(Player p, BedwarsPlugin plugin){
            if (armorTier!=null){
                switch(armorTier){
                    case "CHAINMAIL" -> { p.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS)); p.getInventory().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS)); }
                    case "IRON" -> { p.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS)); p.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS)); }
                }
                plugin.upgrades().applyTo(p);
                return;
            }
            Material mat = material;
            if (teamColored){
                Arena a = plugin.arenas().arenaOf(p);
                if (a!=null){
                    TeamColor t = a.getTeamOf(p.getUniqueId());
                    if (t!=null) mat = t.wool();
                }
            }
            ItemStack it = new ItemStack(mat, amount);
            for (Map.Entry<Enchantment,Integer> e : enchants.entrySet()) it.addUnsafeEnchantment(e.getKey(), e.getValue());
            p.getInventory().addItem(it);
            plugin.upgrades().applyTo(p);
        }
    }
}
