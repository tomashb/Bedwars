package com.example.bedwars.arena;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.gen.Generator;
import com.example.bedwars.gen.GeneratorType;
import com.example.bedwars.util.Loc;
import com.example.bedwars.util.Keys;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ArenaManager {
    private final BedwarsPlugin plugin;
    private final Map<String, Arena> arenas = new HashMap<>();

    public ArenaManager(BedwarsPlugin plugin){ this.plugin=plugin; loadAll(); }

    public Arena get(String n){ return arenas.get(n.toLowerCase()); }
    public Collection<Arena> all(){ return arenas.values(); }

    public Arena create(String name, String world){
        Arena a = new Arena(name, world);
        // default enabled teams from config
        List<String> defs = plugin.getConfig().getStringList("teams-enabled");
        if (defs.isEmpty()) for (TeamColor t:TeamColor.values()) a.getEnabledTeams().add(t);
        else for (String s : defs){ try{ a.getEnabledTeams().add(TeamColor.valueOf(s)); }catch(Exception ignored){} }
        arenas.put(name.toLowerCase(), a);
        return a;
    }

    public void delete(String name){
        Arena removed = arenas.remove(name.toLowerCase());
        if (removed != null){
            for (org.bukkit.World w : org.bukkit.Bukkit.getWorlds()){
                for (org.bukkit.entity.Entity ent : w.getEntities()){
                    var pdc = ent.getPersistentDataContainer();
                    if (pdc.has(Keys.ARENA, org.bukkit.persistence.PersistentDataType.STRING)){
                        String tag = pdc.get(Keys.ARENA, org.bukkit.persistence.PersistentDataType.STRING);
                        if (name.equalsIgnoreCase(tag)) ent.remove();
                    }
                }
            }
        }
        File f = new File(plugin.getDataFolder(), "arenas/"+name+".yml");
        if (f.exists()) f.delete();
    }

    public void loadAll(){
        File dir = new File(plugin.getDataFolder(), "arenas");
        if (!dir.exists()) dir.mkdirs();
        File[] files = dir.listFiles((d,n)->n.endsWith(".yml"));
        if (files==null) return;
        for (File f : files){
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
            String name = cfg.getString("name", f.getName().replace(".yml",""));
            String world = cfg.getString("world", plugin.getConfig().getString("lobby-world","world"));
            Arena a = new Arena(name, world);
            a.setState(GameState.valueOf(cfg.getString("state", "DISABLED")));
            for (String s : cfg.getStringList("enabled-teams")){
                try{ a.getEnabledTeams().add(TeamColor.valueOf(s)); }catch(Exception ignored){}
            }
            // legacy: if none set, use config defaults
            if (a.getEnabledTeams().isEmpty()){
                for (String s : plugin.getConfig().getStringList("teams-enabled")){
                    try{ a.getEnabledTeams().add(TeamColor.valueOf(s)); }catch(Exception ignored){}
                }
                if (a.getEnabledTeams().isEmpty()) for (TeamColor t:TeamColor.values()) a.getEnabledTeams().add(t);
            }
            if (cfg.isConfigurationSection("lobby")) a.setLobby(Loc.read(cfg.getConfigurationSection("lobby")));
            if (cfg.isConfigurationSection("teams")){
                ConfigurationSection tsec = cfg.getConfigurationSection("teams");
                for (String key : tsec.getKeys(false)){
                    TeamColor t = TeamColor.valueOf(key);
                    ConfigurationSection sec = tsec.getConfigurationSection(key);
                    if (sec.isConfigurationSection("spawn")) a.setSpawn(t, Loc.read(sec.getConfigurationSection("spawn")));
                    if (sec.isConfigurationSection("bed")) a.setBed(t, Loc.read(sec.getConfigurationSection("bed")));
                }
            }
            // generators (flat)
            for (Map<?,?> m : cfg.getMapList("generators.all")){
                Location loc = Loc.read(m);
                GeneratorType type = GeneratorType.valueOf(String.valueOf(m.get("type")));
                Object to = m.containsKey("tier")? m.get("tier"):1; int tier = (to instanceof Number)? ((Number)to).intValue(): Integer.parseInt(String.valueOf(to));
                a.getGenerators().add(new Generator(type, loc, tier));
            }
            // shops
            if (cfg.isConfigurationSection("shops")){
                ConfigurationSection s = cfg.getConfigurationSection("shops");
                if (s.isConfigurationSection("item")) a.setItemShop(Loc.read(s.getConfigurationSection("item")));
                if (s.isConfigurationSection("upgrade")) a.setUpgradeShop(Loc.read(s.getConfigurationSection("upgrade")));
            }
            arenas.put(name.toLowerCase(), a);
        }
        plugin.getLogger().info("Chargé " + arenas.size() + " arènes.");
        // once all arenas are loaded spawn NPCs and generator markers
        Bukkit.getScheduler().runTask(plugin, this::spawnDecorationsAll);
    }

    public void save(Arena a){
        File f = new File(plugin.getDataFolder(), "arenas/"+a.getName()+".yml");
        FileConfiguration cfg = new YamlConfiguration();
        cfg.set("name", a.getName());
        cfg.set("world", a.getWorldName());
        cfg.set("state", a.getState().name());
        if (a.getLobby()!=null) cfg.set("lobby", Loc.write(a.getLobby()));
        Map<String,Object> teams = new LinkedHashMap<>();
        for (TeamColor t : TeamColor.values()){
            Map<String,Object> sec = new LinkedHashMap<>();
            if (a.getSpawn(t)!=null) sec.put("spawn", Loc.write(a.getSpawn(t)));
            if (a.getBed(t)!=null) sec.put("bed", Loc.write(a.getBed(t)));
            if (!sec.isEmpty()) teams.put(t.name(), sec);
        }
        cfg.set("teams", teams);
        // enabled teams
        List<String> enabled = new ArrayList<>(); for (TeamColor t : a.getEnabledTeams()) enabled.add(t.name());
        cfg.set("enabled-teams", enabled);
        // shops
        Map<String,Object> shops = new LinkedHashMap<>();
        if (a.getItemShop()!=null) shops.put("item", Loc.write(a.getItemShop()));
        if (a.getUpgradeShop()!=null) shops.put("upgrade", Loc.write(a.getUpgradeShop()));
        cfg.set("shops", shops);
        // generators
        List<Map<String,Object>> gens = new ArrayList<>();
        for (Generator g : a.getGenerators()){
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("type", g.getType().name()); m.put("tier", g.getTier()); m.putAll(Loc.write(g.getLocation())); gens.add(m);
        }
        cfg.set("generators.all", gens);
        try{ cfg.save(f); }catch(IOException e){ e.printStackTrace(); }
    }

    public void shutdown(){ for (Arena a : arenas.values()) save(a); }

    /** Spawns shop NPCs and generator markers for all arenas. */
    public void spawnDecorationsAll(){ arenas.values().forEach(this::spawnDecorations); }

    private void spawnDecorations(Arena a){
        var w = a.getWorld(); if (w==null) return;
        // spawn item shop
        var item = a.getItemShop();
        if (item != null && item.getWorld()!=null){
            cleanupAround(item, Keys.NPC);
            org.bukkit.entity.Villager v = item.getWorld().spawn(item, org.bukkit.entity.Villager.class);
            v.setAI(false); v.setInvulnerable(true); v.setSilent(true); v.setCollidable(false);
            v.setCustomName("§bBoutique"); v.setCustomNameVisible(true);
            var pdc = v.getPersistentDataContainer();
            pdc.set(Keys.NPC, org.bukkit.persistence.PersistentDataType.STRING, "item");
            pdc.set(Keys.ARENA, org.bukkit.persistence.PersistentDataType.STRING, a.getName());
            v.teleport(item);
        }
        var up = a.getUpgradeShop();
        if (up != null && up.getWorld()!=null){
            cleanupAround(up, Keys.NPC);
            org.bukkit.entity.Villager v = up.getWorld().spawn(up, org.bukkit.entity.Villager.class);
            v.setAI(false); v.setInvulnerable(true); v.setSilent(true); v.setCollidable(false);
            v.setCustomName("§eAméliorations"); v.setCustomNameVisible(true);
            var pdc = v.getPersistentDataContainer();
            pdc.set(Keys.NPC, org.bukkit.persistence.PersistentDataType.STRING, "upgrade");
            pdc.set(Keys.ARENA, org.bukkit.persistence.PersistentDataType.STRING, a.getName());
            v.teleport(up);
        }
        for (Generator g : a.getGenerators()){
            Location loc = g.getLocation();
            if (loc.getWorld()==null) continue;
            cleanupAround(loc, Keys.GEN);
            org.bukkit.entity.ArmorStand as = loc.getWorld().spawn(loc.clone().add(0,0.1,0), org.bukkit.entity.ArmorStand.class);
            as.setInvisible(true); as.setMarker(true); as.setGravity(false);
            as.setCustomName("§bGEN §7: §f" + g.getType().name()); as.setCustomNameVisible(true);
            var pdc = as.getPersistentDataContainer();
            pdc.set(Keys.GEN, org.bukkit.persistence.PersistentDataType.STRING, g.getType().name());
            pdc.set(Keys.ARENA, org.bukkit.persistence.PersistentDataType.STRING, a.getName());
        }
    }

    private void cleanupAround(Location loc, org.bukkit.NamespacedKey key){
        for (var ent : loc.getWorld().getNearbyEntities(loc,1,1,1)){
            var pdc = ent.getPersistentDataContainer();
            if (pdc.has(key, org.bukkit.persistence.PersistentDataType.STRING)) ent.remove();
        }
    }

    public void startArena(Arena a){
        if (a==null) return;
        a.setState(GameState.STARTING);
        int seconds = plugin.getConfig().getInt("countdown-seconds", 20);
        a.broadcast(com.example.bedwars.util.C.msgRaw("start.countdown","seconds",seconds));
        new org.bukkit.scheduler.BukkitRunnable(){
            int s = seconds;
            @Override public void run(){
                s--;
                if (s<=0){
                    a.setState(GameState.RUNNING);
                    plugin.generators().resetArena(a);
                    a.broadcast(com.example.bedwars.util.C.msg("start.go"));
                    for (UUID id : a.getAllPlayers()){
                        org.bukkit.entity.Player pl = Bukkit.getPlayer(id);
                        if (pl==null) continue;
                        com.example.bedwars.arena.TeamColor team = a.getTeamOf(id);
                        org.bukkit.Location spawn = team!=null? a.getSpawn(team): null;
                        if (spawn==null || spawn.getWorld()==null){
                            pl.sendMessage(com.example.bedwars.util.C.color("&cSpawn manquant pour votre équipe."));
                            continue;
                        }
                        pl.teleport(spawn);
                        plugin.boards().applyTo(pl, a);
                    }
                    cancel();
                    return;
                }
                if (s%5==0 || s<=5){
                    a.broadcast(com.example.bedwars.util.C.msgRaw("start.countdown","seconds",s));
                }
            }
        }.runTaskTimer(plugin,20L,20L);
    }
}
