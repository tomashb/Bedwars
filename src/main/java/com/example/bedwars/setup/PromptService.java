package com.example.bedwars.setup;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.WorldRef;
import com.example.bedwars.gui.AdminView;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class PromptService implements Listener {
  private static final Pattern ARENA_ID = Pattern.compile("^[a-z0-9](?:[a-z0-9_-]{0,31})$");
  private final BedwarsPlugin plugin;
  private final Map<UUID, PendingAction> pending = new ConcurrentHashMap<>();

  private static String normalizeId(String raw) {
    if (raw == null) return "";
    String s = Normalizer.normalize(raw, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    s = s.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "_");
    return s;
  }

  public PromptService(BedwarsPlugin plugin){
    this.plugin = plugin;
  }

  public void start(Player p, EditorActions action, Object payload, long timeoutTicks) {
    PendingAction pa = new PendingAction(action, payload, System.currentTimeMillis()+timeoutTicks*50);
    pending.put(p.getUniqueId(), pa);
    if(action == EditorActions.CREATE_ARENA_ID){
      p.sendMessage(plugin.messages().get("editor.create-id"));
    } else if(action == EditorActions.CONFIRM_DELETE && payload instanceof String id){
      p.sendMessage(plugin.messages().get("editor.confirm-delete").replace("{arena}", id));
    }
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      PendingAction removed = pending.remove(p.getUniqueId());
      if(removed != null){
        p.sendMessage(plugin.messages().get("editor.timeout"));
      }
    }, timeoutTicks);
  }

  @EventHandler
  public void onChat(AsyncPlayerChatEvent e){
    PendingAction pa = pending.get(e.getPlayer().getUniqueId());
    if(pa == null) return;
    e.setCancelled(true);
    Bukkit.getScheduler().runTask(plugin, () -> handle(e.getPlayer(), pa, e.getMessage()));
  }

  private void handle(Player p, PendingAction pa, String msg){
    if(System.currentTimeMillis() > pa.expiresAt()){ pending.remove(p.getUniqueId()); return; }
    switch(pa.action()){
      case CREATE_ARENA_ID -> {
        String id = normalizeId(msg);
        if (!ARENA_ID.matcher(id).matches()) {
          p.sendMessage(plugin.messages().get("editor.create-id-invalid"));
          p.sendMessage(plugin.messages().get("editor.create-id"));
          return;
        }
        if (plugin.arenas().get(id).isPresent()) {
          p.sendMessage(plugin.messages().format("editor.create-id-exists", Map.of("arena", id)));
          p.sendMessage(plugin.messages().get("editor.create-id"));
          return;
        }
        var a = plugin.arenas().create(id, new WorldRef(p.getWorld().getName()));
        p.sendMessage(plugin.messages().format("editor.created", Map.of("arena", a.id())));
        cancel(p);
        plugin.menus().open(AdminView.ARENA_EDITOR, p, a.id());
      }
      case CONFIRM_DELETE -> {
        pending.remove(p.getUniqueId());
        if(!"CONFIRM".equalsIgnoreCase(msg)) return;
        if(pa.payload() instanceof String id){
          plugin.arenas().delete(id);
          p.sendMessage(plugin.messages().get("editor.deleted").replace("{arena}", id));
        }
      }
    }
  }

  public void cancel(Player p){ pending.remove(p.getUniqueId()); }
}
