package com.example.bedwars.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;

/**
 * Loads and formats messages from the plugin's messages.yml file.
 * Supports basic placeholder replacement using {placeholder} syntax.
 */
public class MessageManager {

    private final JavaPlugin plugin;
    private YamlConfiguration messages;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * Reload messages.yml from disk.
     */
    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.messages = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Fetch a message and apply color codes and placeholders.
     *
     * @param path         path in messages.yml
     * @param placeholders map of placeholder -> value
     * @return formatted message with prefix
     */
    public String get(String path, Map<String, String> placeholders) {
        String prefix = messages.getString("prefix", "");
        String msg = messages.getString(path, path);
        if (!prefix.isEmpty()) {
            msg = prefix + msg;
        }
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    /**
     * Convenience overload without placeholders.
     */
    public String get(String path) {
        return get(path, Map.of());
    }
}
