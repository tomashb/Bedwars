package com.example.bedwars.setup;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.WorldRef;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class PromptService implements Listener {
  private static final Pattern ID = Pattern.compile("^[a-z0-9_-]{3,32}$");
  private final BedwarsPlugin plugin;
  private final Map<UUID, PendingAction> pending = new ConcurrentHashMap<>();

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
        int attempts = pa.payload() instanceof Integer i ? i : 0;
        if(!ID.matcher(msg).matches()){
          if(attempts >= 1){
            p.sendMessage(plugin.messages().get("editor.create-id-invalid"));
            pending.remove(p.getUniqueId());
            return;
          }
          p.sendMessage(plugin.messages().get("editor.create-id-invalid"));
          start(p, EditorActions.CREATE_ARENA_ID, attempts+1, 20*30);
          return;
        }
        if(plugin.arenas().get(msg).isPresent()){
          if(attempts >= 1){
            p.sendMessage(plugin.messages().get("editor.create-id-exists"));
            pending.remove(p.getUniqueId());
            return;
          }
          p.sendMessage(plugin.messages().get("editor.create-id-exists"));
          start(p, EditorActions.CREATE_ARENA_ID, attempts+1, 20*30);
          return;
        }
        pending.remove(p.getUniqueId());
        Arena a = plugin.arenas().create(msg, new WorldRef(p.getWorld().getName()));
        p.sendMessage(plugin.messages().get("editor.created").replace("{arena}", a.id()));
        plugin.menus().openEditor(com.example.bedwars.gui.editor.EditorView.ARENA, p, a.id());
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
