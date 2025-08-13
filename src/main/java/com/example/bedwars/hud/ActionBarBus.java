package com.example.bedwars.hud;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Simple bus to send transient action bar messages with TTL.
 */
public final class ActionBarBus {
  private static final class Msg {
    final BaseComponent[] comp;
    int ttl;
    Msg(BaseComponent[] c, int t) { comp = c; ttl = t; }
  }

  private final Map<UUID, Msg> current = new HashMap<>();

  /** Push a new message for a player lasting a number of seconds. */
  public void push(Player p, String legacyColorMsg, int seconds) {
    current.put(p.getUniqueId(), new Msg(
        TextComponent.fromLegacyText(org.bukkit.ChatColor.translateAlternateColorCodes('&', legacyColorMsg)),
        seconds));
  }

  /** Starts the repeating task to dispatch messages each second. */
  public void start(Plugin plugin) {
    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      Iterator<Map.Entry<UUID, Msg>> it = current.entrySet().iterator();
      while (it.hasNext()) {
        var e = it.next();
        Player p = Bukkit.getPlayer(e.getKey());
        if (p == null) { it.remove(); continue; }
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, e.getValue().comp);
        if (--e.getValue().ttl <= 0) it.remove();
      }
    }, 0L, 20L);
  }
}
