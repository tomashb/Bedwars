package com.example.bedwars.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Configs {
    private static final Map<String,String> messages = new HashMap<>();
    public static void loadMessages(JavaPlugin plugin){
        File f = new File(plugin.getDataFolder(),"messages.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        for (String k : cfg.getKeys(true)) if (cfg.isString(k)) messages.put(k, cfg.getString(k));
    }
    public static String msg(String path){ return C.color(messages.getOrDefault(path,path)); }
    public static String msg(String path, String k, Object v){ String s=messages.getOrDefault(path,path); return C.color(s.replace("{"+k+"}", String.valueOf(v))); }
    public static String msgRaw(String path, Object... kv){ String s=messages.getOrDefault(path,path); for(int i=0;i+1<kv.length;i+=2) s=s.replace("{"+kv[i]+"}", String.valueOf(kv[i+1])); return C.color(s); }
}
