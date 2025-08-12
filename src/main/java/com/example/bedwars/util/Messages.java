package com.example.bedwars.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;

/**
 * Loads messages from messages.yml and provides simple placeholder
 * replacement.
 */
public final class Messages {

    private final FileConfiguration cfg;

    public Messages(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.cfg = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Return a message translated with Bukkit color codes.
     */
    public String get(String path) {
        return ChatColor.translateAlternateColorCodes('&', cfg.getString(path, path));
    }

    /**
     * Return a formatted message replacing placeholders of the form
     * {key} with the provided values.
     */
    public String get(String path, Map<String, ?> vars) {
        String msg = get(path);
        for (Map.Entry<String, ?> e : vars.entrySet()) {
            msg = msg.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
        }
        return msg;
    }
}

