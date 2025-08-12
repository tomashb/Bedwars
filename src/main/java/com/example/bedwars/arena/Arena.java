package com.example.bedwars.arena;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Simplified arena model. Only handles joining, leaving and a very
 * small state machine. Real gameplay such as generators or shops is
 * intentionally left out for brevity.
 */
public class Arena {

    private final String name;
    private final Set<UUID> players = new HashSet<>();
    private final BedwarsPlugin plugin;
    private GameState state = GameState.WAITING;
    private int countdown = 10; // seconds

    public Arena(BedwarsPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public GameState getState() {
        return state;
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
        player.sendMessage(plugin.getMessages().get("arena.join", Map.of("arena", name)));
        if (state == GameState.WAITING && players.size() >= 2) {
            startCountdown();
        }
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        player.sendMessage(plugin.getMessages().get("arena.leave", Map.of("arena", name)));
        if (players.isEmpty() && state != GameState.WAITING) {
            reset();
        }
    }

    private void startCountdown() {
        state = GameState.STARTING;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (countdown <= 0) {
                    state = GameState.RUNNING;
                    broadcast(plugin.getMessages().get("arena.started", Map.of("arena", name)));
                    cancel();
                    return;
                }
                broadcast(plugin.getMessages().get("start.countdown", Map.of("seconds", String.valueOf(countdown))));
                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void reset() {
        state = GameState.WAITING;
        countdown = 10;
    }

    private void broadcast(String msg) {
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendMessage(msg);
            }
        }
    }
}
