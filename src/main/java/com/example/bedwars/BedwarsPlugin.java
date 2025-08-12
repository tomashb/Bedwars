package com.example.bedwars;

import com.example.bedwars.menu.MenuListener;
import com.example.bedwars.menu.MenuManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BedwarsPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        if (getCommand("bw") != null) {
            getCommand("bw").setExecutor((sender, command, label, args) -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Players only");
                    return true;
                }
                MenuManager.openRoot(player);
                return true;
            });
        }
        if (getCommand("bwadmin") != null) {
            getCommand("bwadmin").setExecutor((sender, command, label, args) -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Players only");
                    return true;
                }
                MenuManager.openRoot(player);
                return true;
            });
        }
    }
}
