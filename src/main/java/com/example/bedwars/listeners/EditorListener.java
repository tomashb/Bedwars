package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.gen.GeneratorType;
import com.example.bedwars.gui.BWMenuHolder;
import com.example.bedwars.gui.AdminView;
import com.example.bedwars.gui.editor.*;
import com.example.bedwars.shop.NpcType;
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;
import java.util.Map;

public final class EditorListener implements Listener {
  private final BedwarsPlugin plugin;

  public EditorListener(BedwarsPlugin plugin){ this.plugin = plugin; }

  @EventHandler
  public void onClick(InventoryClickEvent e){
    Inventory top = e.getView().getTopInventory();
    if(!(top.getHolder() instanceof BWMenuHolder holder)) return;
    e.setCancelled(true);
    if(!(e.getWhoClicked() instanceof Player p)) return;
    if(!p.hasPermission("bedwars.admin")) { p.sendMessage(plugin.messages().get("admin.no-perm")); return; }
    if(holder.view != AdminView.ARENA_EDITOR || holder.editorView == null) return;
    if(e.getClickedInventory() != top) return;
    int slot = e.getRawSlot();
    String id = holder.arenaId;
    switch(holder.editorView){
      case ARENA -> handleArena(slot, p, id);
      case TEAM -> handleTeam(slot, p, id);
      case GEN -> handleGen(slot, p, id);
      case NPC -> handleNpc(slot, p, id);
    }
  }

  private void handleArena(int slot, Player p, String id){
    switch(slot){
      case ArenaEditorMenu.SLOT_LOBBY -> {
        if(!ensureWorld(p, id)) return;
        plugin.arenas().setLobby(id, p.getLocation());
        p.sendMessage(plugin.messages().get("editor.set-lobby"));
        plugin.menus().openEditor(EditorView.ARENA, p, id);
      }
      case ArenaEditorMenu.SLOT_TEAMS -> plugin.menus().openEditor(EditorView.TEAM, p, id);
      case ArenaEditorMenu.SLOT_NPC -> plugin.menus().openEditor(EditorView.NPC, p, id);
      case ArenaEditorMenu.SLOT_GENS -> plugin.menus().openEditor(EditorView.GEN, p, id);
      case ArenaEditorMenu.SLOT_SAVE -> {
        plugin.arenas().save(id);
        p.sendMessage(plugin.messages().get("editor.saved"));
      }
      case ArenaEditorMenu.SLOT_RELOAD -> {
        plugin.arenas().load(id);
        plugin.menus().openEditor(EditorView.ARENA, p, id);
        p.sendMessage(plugin.messages().get("editor.reloaded"));
      }
      case ArenaEditorMenu.SLOT_DELETE -> {
        plugin.prompts().start(p, com.example.bedwars.setup.EditorActions.CONFIRM_DELETE, id, 20*30);
        p.closeInventory();
      }
      case ArenaEditorMenu.SLOT_BACK -> plugin.menus().open(AdminView.ARENAS, p, null);
    }
  }

  private void handleTeam(int slot, Player p, String id){
    TeamColor[] colors = TeamColor.values();
    if(slot >=10 && slot <18){
      TeamColor c = colors[slot-10];
      Arena a = plugin.arenas().get(id).orElseThrow();
      if(a.enabledTeams().contains(c)) plugin.arenas().disableTeam(id, c); else plugin.arenas().enableTeam(id, c);
      plugin.menus().openEditor(EditorView.TEAM, p, id);
      return;
    }
    if(slot >=19 && slot <27){
      TeamColor c = colors[slot-19];
      if(!ensureWorld(p, id)) return;
      plugin.arenas().setTeamSpawn(id, c, p.getLocation());
      p.sendMessage(plugin.messages().format("editor.set-spawn", Map.of("team", c.display)));
      plugin.menus().openEditor(EditorView.TEAM, p, id);
      return;
    }
    if(slot >=28 && slot <36){
      TeamColor c = colors[slot-28];
      if(!ensureWorld(p, id)) return;
      Location head = findBedHead(p);
      if(head == null){
        p.sendMessage(plugin.messages().get("editor.not-looking-bed"));
        return;
      }
      plugin.arenas().setTeamBed(id, c, head);
      p.sendMessage(plugin.messages().format("editor.set-bed", Map.of("team", c.display)));
      plugin.menus().openEditor(EditorView.TEAM, p, id);
      return;
    }
    if(slot == TeamEditorMenu.SLOT_BACK){ plugin.menus().openEditor(EditorView.ARENA, p, id); }
  }

  private void handleNpc(int slot, Player p, String id){
    if(slot == NpcEditorMenu.SLOT_ADD_ITEM){
      if(!ensureWorld(p, id)) return;
      LivingEntity e = (LivingEntity)p.getWorld().spawnEntity(p.getLocation(), EntityType.VILLAGER);
      e.setAI(false); e.setInvulnerable(true); e.setCollidable(false); e.setRemoveWhenFarAway(false);
      e.getPersistentDataContainer().set(plugin.keys().ARENA_ID(), PersistentDataType.STRING, id);
      e.getPersistentDataContainer().set(plugin.keys().NPC_KIND(), PersistentDataType.STRING, "item");
      e.setCustomName(ChatColor.GREEN + "Objets");
      e.setCustomNameVisible(true);
      plugin.arenas().addNpc(id, NpcType.ITEM, e.getLocation());
      p.sendMessage(plugin.messages().get("editor.npc-item-added"));
    } else if(slot == NpcEditorMenu.SLOT_ADD_UPGRADE){
      if(!ensureWorld(p, id)) return;
      LivingEntity e = (LivingEntity)p.getWorld().spawnEntity(p.getLocation(), EntityType.VILLAGER);
      e.setAI(false); e.setInvulnerable(true); e.setCollidable(false); e.setRemoveWhenFarAway(false);
      e.getPersistentDataContainer().set(plugin.keys().ARENA_ID(), PersistentDataType.STRING, id);
      e.getPersistentDataContainer().set(plugin.keys().NPC_KIND(), PersistentDataType.STRING, "upgrade");
      e.setCustomName(ChatColor.AQUA + "Améliorations");
      e.setCustomNameVisible(true);
      plugin.arenas().addNpc(id, NpcType.UPGRADE, e.getLocation());
      p.sendMessage(plugin.messages().get("editor.npc-upgrade-added"));
    } else if(slot == NpcEditorMenu.SLOT_BACK){
      plugin.menus().openEditor(EditorView.ARENA, p, id);
    }
  }

  private void handleGen(int slot, Player p, String id){
    GeneratorType type = null;
    if(slot == GeneratorsEditorMenu.SLOT_IRON) type = GeneratorType.TEAM_IRON;
    else if(slot == GeneratorsEditorMenu.SLOT_GOLD) type = GeneratorType.TEAM_GOLD;
    else if(slot == GeneratorsEditorMenu.SLOT_DIAMOND) type = GeneratorType.DIAMOND;
    else if(slot == GeneratorsEditorMenu.SLOT_EMERALD) type = GeneratorType.EMERALD;
    else if(slot == GeneratorsEditorMenu.SLOT_BACK) { plugin.menus().openEditor(EditorView.ARENA, p, id); return; }
    if(type != null){
      if(!ensureWorld(p, id)) return;
      var g = plugin.arenas().addGenerator(id, type, p.getLocation(), 1);
      ArmorStand as = (ArmorStand)p.getWorld().spawnEntity(p.getLocation(), EntityType.ARMOR_STAND);
      as.setInvisible(true); as.setMarker(true); as.setCustomNameVisible(true);
      as.setCustomName(type.name());
      as.getPersistentDataContainer().set(plugin.keys().ARENA_ID(), PersistentDataType.STRING, id);
      as.getPersistentDataContainer().set(plugin.keys().GEN_MARKER(), PersistentDataType.STRING, "1");
      as.getPersistentDataContainer().set(plugin.keys().GEN_KIND(), PersistentDataType.STRING, type.name());
      p.sendMessage(plugin.messages().format("editor.gen-added", Map.of("type", type.name(), "tier", String.valueOf(g.tier()))));
    }
  }

  private boolean ensureWorld(Player p, String id){
    String arenaWorld = plugin.arenas().get(id).map(a -> a.world().name()).orElse(null);
    if(arenaWorld == null) return false;
    if(!p.getWorld().getName().equals(arenaWorld)){
      p.sendMessage(plugin.messages().get("arena.world-required"));
      return false;
    }
    return true;
  }

  private Location findBedHead(Player p){
    Block target = p.getTargetBlockExact(6, FluidCollisionMode.NEVER);
    if(target == null) return null;
    BlockData data = target.getBlockData();
    if(!(data instanceof Bed bed)) return null;
    Block headBlock = target;
    if(bed.getPart() != Bed.Part.HEAD){
      org.bukkit.block.BlockFace facing = bed.getFacing();
      headBlock = target.getRelative(facing);
      BlockData d2 = headBlock.getBlockData();
      if(!(d2 instanceof Bed b2) || b2.getPart() != Bed.Part.HEAD) return null;
    }
    return headBlock.getLocation();
  }
}
