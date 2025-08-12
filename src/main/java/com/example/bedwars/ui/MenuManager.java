package com.example.bedwars.ui;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.ArenaManager;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.gen.GeneratorType;
import com.example.bedwars.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MenuManager {
    private final BedwarsPlugin plugin; private final ArenaManager arenas;
    public static final NamespacedKey ACT = new NamespacedKey(BedwarsPlugin.get(), "bw_act");
    public static final NamespacedKey ARENA = new NamespacedKey(BedwarsPlugin.get(), "bw_arena");
    public static final NamespacedKey TEAM = new NamespacedKey(BedwarsPlugin.get(), "bw_team");
    public static final NamespacedKey GENTYPE = new NamespacedKey(BedwarsPlugin.get(), "bw_gentype");

    public MenuManager(BedwarsPlugin plugin, ArenaManager arenas){ this.plugin=plugin; this.arenas=arenas; }

    private ItemStack pane(){ ItemStack it=new ItemStack(Material.GRAY_STAINED_GLASS_PANE); ItemMeta m=it.getItemMeta(); m.setDisplayName(" "); it.setItemMeta(m); return it; }
    private Inventory createGui(int size, String title){ Inventory inv=Bukkit.createInventory(null,size,title); ItemStack f=pane(); for(int i=0;i<size;i++) inv.setItem(i,f); return inv; }
    private Material woolFor(com.example.bedwars.arena.TeamColor t){
        return switch (t){ case RED->Material.RED_WOOL; case BLUE->Material.BLUE_WOOL; case GREEN->Material.GREEN_WOOL; case YELLOW->Material.YELLOW_WOOL; case AQUA->Material.CYAN_WOOL; case WHITE->Material.WHITE_WOOL; case PINK->Material.PINK_WOOL; case GRAY->Material.GRAY_WOOL; };
    }
    private ItemStack actionItem(Material mat, String name, String act, String arena, String team, String gentype){ ItemStack it=new ItemBuilder(mat).name(name).build(); set(it,act,arena,team,gentype); return it; }
    private void set(ItemStack it, String act, String arena, String team, String gentype){
        ItemMeta meta=it.getItemMeta(); PersistentDataContainer pdc=meta.getPersistentDataContainer();
        pdc.set(ACT, PersistentDataType.STRING, act); if (arena!=null) pdc.set(ARENA,PersistentDataType.STRING,arena); if (team!=null) pdc.set(TEAM,PersistentDataType.STRING,team); if (gentype!=null) pdc.set(GENTYPE,PersistentDataType.STRING,gentype); it.setItemMeta(meta);
    }
    public static com.example.bedwars.arena.TeamColor parseTeam(String s){ try{ return com.example.bedwars.arena.TeamColor.valueOf(s.toUpperCase(java.util.Locale.ROOT)); }catch(Exception e){ return null; } }
    public static com.example.bedwars.gen.GeneratorType parseGen(String s){ try{ return com.example.bedwars.gen.GeneratorType.valueOf(s.toUpperCase(java.util.Locale.ROOT)); }catch(Exception e){ return null; } }

    public void openMain(Player p){
        boolean admin=p.hasPermission("bedwars.admin"); Inventory inv=createGui(27, ChatColor.DARK_GRAY+"BedWars — Menu principal");
        inv.setItem(11, actionItem(Material.DIAMOND_SWORD, "§aRejoindre une arène", "JOIN_LIST", null, null, null));
        inv.setItem(13, actionItem(Material.OAK_DOOR, "§eQuitter l'arène", "LEAVE", null, null, null));
        inv.setItem(15, actionItem(Material.CHEST, "§bOuvrir la boutique", "OPEN_SHOP", null, null, null));
        if (admin) inv.setItem(22, actionItem(Material.REDSTONE_BLOCK, "§cAdministration des arènes", "ADMIN_LIST", null, null, null));
        p.openInventory(inv);
    }

    public void openJoinList(Player p){
        java.util.List<Arena> list=new java.util.ArrayList<>(arenas.all()); Inventory inv=createGui(54, ChatColor.DARK_GRAY+"BedWars — Rejoindre une arène");
        int slot=10; for (Arena a : list){
            ItemBuilder ib=new ItemBuilder(Material.PAPER).name("§e"+a.getName()); java.util.List<String> lore=new java.util.ArrayList<>(); lore.add("§7Monde: §f"+a.getWorldName()); lore.add("§7État: §f"+a.getState()); ib.lore(lore);
            ItemStack it=ib.build(); set(it,"JOIN", a.getName(), null, null); inv.setItem(slot++, it); if (slot%9==8) slot+=3; if (slot>=44) break;
        }
        inv.setItem(49, actionItem(Material.ARROW, "§7Retour", "BACK_MAIN", null, null, null)); p.openInventory(inv);
    }

    public void openAdminList(Player p){
        java.util.List<Arena> list=new java.util.ArrayList<>(arenas.all()); Inventory inv=createGui(54, ChatColor.DARK_GRAY+"BedWars — Administration");
        int slot=10; for (Arena a : list){
            ItemBuilder ib=new ItemBuilder(Material.MAP).name("§d"+a.getName()); java.util.List<String> lore=new java.util.ArrayList<>(); lore.add("§7Monde: §f"+a.getWorldName()); lore.add("§7État: §f"+a.getState()); ib.lore(lore);
            ItemStack it=ib.build(); set(it,"ARENA_EDIT", a.getName(), null, null); inv.setItem(slot++, it); if (slot%9==8) slot+=3; if (slot>=44) break;
        }
        inv.setItem(45, actionItem(Material.ARROW, "§7Retour", "BACK_MAIN", null, null, null));
        inv.setItem(53, actionItem(Material.LIME_WOOL, "§aCréer une arène", "ARENA_CREATE_AUTO", null, null, null)); p.openInventory(inv);
    }

    public void openArenaEditor(Player p, String arena){
        Inventory inv=createGui(54, ChatColor.DARK_GRAY+"BedWars — Éditer : "+arena);
        inv.setItem(10, actionItem(Material.LODESTONE, "§eDéfinir le lobby (ici)", "SET_LOBBY", arena, null, null));
        inv.setItem(12, actionItem(Material.RED_BED, "§eDéfinir un lit (choisir équipe)", "SET_BED_TEAM", arena, null, null));
        inv.setItem(14, actionItem(Material.ARMOR_STAND, "§eDéfinir un spawn (choisir équipe)", "SET_SPAWN_TEAM", arena, null, null));
        inv.setItem(16, actionItem(Material.DROPPER, "§eAjouter un générateur", "ADD_GENERATOR", arena, null, null));
        inv.setItem(19, actionItem(Material.EMERALD, "§eBoutique §7(objets)", "SET_SHOP_ITEM", arena, null, null));
        inv.setItem(21, actionItem(Material.ANVIL, "§eBoutique §7(améliorations)", "SET_SHOP_UPGRADE", arena, null, null));
        inv.setItem(23, actionItem(Material.WRITABLE_BOOK, "§aSauvegarder l'arène", "SAVE", arena, null, null));
        inv.setItem(25, actionItem(Material.LEAD, "§6Retirer PNJ proches", "CLEAN_NPCS", arena, null, null));
        inv.setItem(28, actionItem(Material.WHITE_WOOL, "§eActiver/Désactiver équipes", "TOGGLE_TEAMS", arena, null, null));
        inv.setItem(30, actionItem(Material.LIME_DYE, "§aDémarrer", "START", arena, null, null));
        inv.setItem(32, actionItem(Material.RED_DYE, "§cArrêter", "STOP", arena, null, null));
        inv.setItem(45, actionItem(Material.ARROW, "§7Retour", "BACK_ADMIN", arena, null, null));
        inv.setItem(49, actionItem(Material.BARRIER, "§cSupprimer l'arène", "DELETE", arena, null, null)); p.openInventory(inv);
    }

    public void openTeamSelect(Player p, String arena, String act){
        Inventory inv = createGui(27, ChatColor.DARK_GRAY+"BedWars — Choisir une équipe ("+arena+")");
        int slot=10;
        for (TeamColor t : TeamColor.values()){
            boolean hide=false; Arena a=arenas.get(arena);
            if (a!=null){
                if (act.equals("SET_SPAWN") && a.hasSpawn(t)) hide=true;
                if (act.equals("SET_BED") && a.hasBed(t)) hide=true;
                if (act.equals("JOIN_TEAM") && (!a.isTeamEnabled(t) || a.getSpawn(t)==null)) hide=true;
            }
            if (!hide){
                ItemStack it=new ItemBuilder(woolFor(t)).name(t.chat()+t.display()).build();
                set(it, act, arena, t.name(), null);
                inv.setItem(slot++, it); if (slot==17) slot=19;
            }
        }
        String back = act.equals("JOIN_TEAM") ? "BACK_MAIN" : "BACK_EDITOR";
        inv.setItem(26, actionItem(Material.ARROW, "§7Retour", back, arena, null, null)); p.openInventory(inv);
    }

    public void openGenSelect(Player p, String arena){
        Inventory inv=createGui(27, ChatColor.DARK_GRAY+"BedWars — Choisir un générateur ("+arena+")"); int slot=11;
        for (GeneratorType gt : GeneratorType.values()){ ItemStack it=new ItemBuilder(Material.DROPPER).name("§e"+gt.name()).build(); set(it, "SET_GEN_TYPE", arena, null, gt.name()); inv.setItem(slot++, it); }
        inv.setItem(26, actionItem(Material.ARROW, "§7Retour", "BACK_EDITOR", arena, null, null)); p.openInventory(inv);
    }
}
