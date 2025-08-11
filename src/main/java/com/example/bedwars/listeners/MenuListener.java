
package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.ArenaManager;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.gen.Generator;
import com.example.bedwars.gen.GeneratorType;
import com.example.bedwars.ui.MenuManager;
import com.example.bedwars.util.C;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;
import java.util.UUID;

public class MenuListener implements Listener {
    private static final org.bukkit.NamespacedKey NPC_KEY = new org.bukkit.NamespacedKey(BedwarsPlugin.get(), "bw_npc");
    private static final org.bukkit.NamespacedKey GEN_KEY = new org.bukkit.NamespacedKey(BedwarsPlugin.get(), "bw_gen");

    private final BedwarsPlugin plugin;
    private final ArenaManager arenas;
    private final MenuManager menus;
    private final Random rnd = new Random();

    public MenuListener(BedwarsPlugin plugin, ArenaManager arenas, MenuManager menus) {
        this.plugin = plugin;
        this.arenas = arenas;
        this.menus = menus;
    }

    private boolean isMenu(String title) {
        return title != null && (title.startsWith(ChatColor.DARK_GRAY + "BedWars"));
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        String title = e.getView().getTitle();
        if (!isMenu(title)) return;
        e.setCancelled(true);

        ItemStack it = e.getCurrentItem();
        if (it == null) return;
        ItemMeta meta = it.getItemMeta();
        if (meta == null) return;

        String act = meta.getPersistentDataContainer().get(MenuManager.ACT, PersistentDataType.STRING);
        if (act == null) return;
        String arenaName = meta.getPersistentDataContainer().get(MenuManager.ARENA, PersistentDataType.STRING);
        String teamName = meta.getPersistentDataContainer().get(MenuManager.TEAM, PersistentDataType.STRING);
        String genType = meta.getPersistentDataContainer().get(MenuManager.GENTYPE, PersistentDataType.STRING);

        Player p = (Player) e.getWhoClicked();

        switch (act) {
            case "JOIN_LIST" -> menus.openJoinList(p);
            case "LEAVE" -> {
                UUID id = p.getUniqueId();
                arenas.all().forEach(a -> a.removePlayer(id));
                p.getInventory().clear();
                p.sendMessage(C.msg("arena.leave"));
            }
            case "OPEN_SHOP" -> plugin.shops().open(p);
            case "ADMIN_LIST" -> menus.openAdminList(p);
            case "BACK_MAIN" -> menus.openMain(p);
            case "BACK_ADMIN" -> menus.openAdminList(p);
            case "BACK_EDITOR" -> { if (arenaName != null) menus.openArenaEditor(p, arenaName); }
            case "ARENA_CREATE_AUTO" -> {
                String name = "arena-" + (System.currentTimeMillis() / 1000);
                Arena a = arenas.create(name, p.getWorld().getName());
                a.setLobby(p.getLocation());
                arenas.save(a);
                p.sendMessage(C.msg("arena.created", "arena", name));
                menus.openAdminList(p);
            }
            case "JOIN" -> {
                Arena a = arenas.get(arenaName);
                if (a == null) { p.sendMessage(C.msg("error.no_arena")); return; }
                if (a.getState() == GameState.DISABLED) { p.sendMessage(C.color("&cArène désactivée.")); return; }
                TeamColor team = TeamColor.values()[ rnd.nextInt(TeamColor.values().length) ];
                a.addPlayer(team, p);
                p.sendMessage(C.msg("arena.join", "arena", a.getName()));
                plugin.boards().applyTo(p, a);
            }
            case "ARENA_EDIT" -> { if (arenaName != null) menus.openArenaEditor(p, arenaName); }
            case "SET_LOBBY" -> {
                Arena a = arenas.get(arenaName);
                if (a == null) return;
                a.setLobby(p.getLocation());
                arenas.save(a);
                p.sendMessage(C.color("&aLobby défini."));
                menus.openArenaEditor(p, arenaName);
            }
            case "SET_SPAWN_TEAM" -> menus.openTeamSelect(p, arenaName, "SET_SPAWN");
            case "SET_BED_TEAM" -> menus.openTeamSelect(p, arenaName, "SET_BED");
            case "TOGGLE_TEAMS" -> menus.openTeamSelect(p, arenaName, "TOGGLE_TEAM");
            case "SET_SPAWN" -> {
                Arena a = arenas.get(arenaName);
                TeamColor t = MenuManager.parseTeam(teamName);
                if (a == null || t == null) return;
                a.setSpawn(t, p.getLocation());
                arenas.save(a);
                p.sendMessage(C.color("&aSpawn défini pour " + t.name()));
                menus.openArenaEditor(p, arenaName);
            }
            case "SET_BED" -> {
                Arena a = arenas.get(arenaName);
                TeamColor t = MenuManager.parseTeam(teamName);
                if (a == null || t == null) return;
                a.setBed(t, p.getLocation().getBlock().getLocation());
                arenas.save(a);
                p.sendMessage(C.color("&aLit défini pour " + t.name()));
                menus.openArenaEditor(p, arenaName);
            }
            case "ADD_GENERATOR" -> menus.openGenSelect(p, arenaName);
            case "SET_GEN_TYPE" -> {
                Arena a = arenas.get(arenaName);
                GeneratorType gt = MenuManager.parseGen(genType);
                if (a == null || gt == null) return;
                Location gl = p.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
                a.getGenerators().add(new Generator(gt, gl, 1));
                arenas.save(a);
                ArmorStand as = gl.getWorld().spawn(gl.clone().add(0, 0.1, 0), ArmorStand.class);
                as.setInvisible(true); as.setMarker(true); as.setGravity(false);
                as.setCustomName("§bGEN §7: §f" + gt.name()); as.setCustomNameVisible(true);
                as.getPersistentDataContainer().set(GEN_KEY, PersistentDataType.STRING, gt.name());
                p.sendMessage(C.color("&aGénérateur " + gt.name() + " ajouté."));
                menus.openArenaEditor(p, arenaName);
            }
            case "SET_SHOP_ITEM" -> {
                Arena a = arenas.get(arenaName);
                if (a == null) return;
                Location loc = p.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
                a.setItemShop(loc);
                arenas.save(a);
                Villager v = loc.getWorld().spawn(loc, Villager.class);
                v.setAI(false); v.setInvulnerable(true); v.setSilent(true); v.setCollidable(false);
                v.setCustomName("§bBoutique"); v.setCustomNameVisible(true);
                v.getPersistentDataContainer().set(NPC_KEY, PersistentDataType.STRING, "item");
                p.sendMessage(C.color("&aBoutique d'objets définie et PNJ posé."));
                menus.openArenaEditor(p, arenaName);
            }
            case "SET_SHOP_UPGRADE" -> {
                Arena a = arenas.get(arenaName);
                if (a == null) return;
                Location loc = p.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
                a.setUpgradeShop(loc);
                arenas.save(a);
                Villager v = loc.getWorld().spawn(loc, Villager.class);
                v.setAI(false); v.setInvulnerable(true); v.setSilent(true); v.setCollidable(false);
                v.setCustomName("§eAméliorations"); v.setCustomNameVisible(true);
                v.getPersistentDataContainer().set(NPC_KEY, PersistentDataType.STRING, "upgrade");
                p.sendMessage(C.color("&aBoutique améliorations définie et PNJ posé."));
                menus.openArenaEditor(p, arenaName);
            }
            case "CLEAN_NPCS" -> {
                int removed = 0;
                for (Entity ent : p.getNearbyEntities(8, 8, 8)) {
                    if (ent instanceof Villager v) {
                        String tag = v.getPersistentDataContainer().get(NPC_KEY, PersistentDataType.STRING);
                        if (tag != null || true) { v.remove(); removed++; }
                    }
                }
                p.sendMessage(C.color("§ePNJ retirés à proximité: §a" + removed));
                if (arenaName != null) menus.openArenaEditor(p, arenaName);
            }
            case "SAVE" -> {
                Arena a = arenas.get(arenaName);
                if (a == null) return;
                arenas.save(a);
                p.sendMessage(C.msg("arena.saved", "arena", a.getName()));
            }
            case "START" -> {
                Arena a = arenas.get(arenaName);
                if (a == null) return;
                a.setState(GameState.STARTING);
                int seconds = plugin.getConfig().getInt("countdown-seconds", 20);
                a.broadcast(C.msgRaw("start.countdown", "seconds", seconds));
                final int[] s = new int[]{seconds};
                org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
                    @Override public void run() {
                        s[0]--;
                        if (s[0] <= 0) {
                            a.setState(GameState.RUNNING);
                            a.broadcast(C.msg("start.go"));
                        } else if (s[0] % 5 == 0 || s[0] <= 5) {
                            a.broadcast(C.msgRaw("start.countdown", "seconds", s[0]));
                        }
                    }
                }, 20L, 20L);
            }
            case "STOP" -> {
                Arena a = arenas.get(arenaName);
                if (a == null) return;
                a.setState(GameState.ENDING);
                a.broadcast(C.msgRaw("arena.stopped", "arena", a.getName()));
            }
            case "DELETE" -> {
                if (arenaName == null) return;
                arenas.delete(arenaName);
                p.sendMessage(C.color("&cArène supprimée: " + arenaName));
                menus.openAdminList(p);
            }
            case "TOGGLE_TEAM" -> {
                Arena a = arenas.get(arenaName);
                TeamColor t = MenuManager.parseTeam(teamName);
                if (a == null || t == null) return;
                boolean now = !a.isTeamEnabled(t);
                a.setTeamEnabled(t, now);
                arenas.save(a);
                p.sendMessage(C.color("&eÉquipe " + t.name() + (now ? " &aactivée" : " &cdésactivée")));
                menus.openArenaEditor(p, arenaName);
            }
        }
    }
}
