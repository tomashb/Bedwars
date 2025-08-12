package com.example.bedwars.gui;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.generator.GeneratorType;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.function.Consumer;

/**
 * Central registry for admin menus with click routing and simple
 * chat prompts used by the arena creation wizard.
 */
public class MenuListener implements Listener {

    private final BedwarsPlugin plugin;
    private final Map<AdminView, BWMenu> menus = new EnumMap<>(AdminView.class);
    private final Map<UUID, String> arenaContext = new HashMap<>();
    private final Map<UUID, Prompt> prompts = new HashMap<>();

    private record Prompt(Consumer<String> handler, long expire) {}

    public MenuListener(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(BWMenu menu) {
        menus.put(menu.id(), menu);
    }

    public void open(Player player, AdminView view, String arenaId, Object... args) {
        if (arenaId != null) {
            arenaContext.put(player.getUniqueId(), arenaId);
        } else {
            arenaContext.remove(player.getUniqueId());
        }
        BWMenu menu = menus.get(view);
        if (menu != null) {
            menu.open(player, args);
        }
    }

    public void openRoot(Player player) {
        open(player, AdminView.ROOT, null);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getView().getTopInventory().getHolder() instanceof BWMenuHolder holder) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null || !item.hasItemMeta()) {
                return;
            }
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            String action = pdc.get(plugin.actionKey(), PersistentDataType.STRING);
            if (action == null) {
                return;
            }
            switch (action) {
                case "ARENAS" -> open(player, AdminView.ARENAS, null);
                case "ARENA_CREATE" -> startCreate(player);
                case "ARENA_OPEN" -> {
                    String arena = pdc.get(plugin.arenaKey(), PersistentDataType.STRING);
                    if (arena != null) {
                        open(player, AdminView.ARENA_EDITOR, arena, arena);
                    }
                }
                case "RULES" -> open(player, AdminView.RULES_EVENTS, null);
                case "NPC" -> open(player, AdminView.NPC_SHOPS, null);
                case "ROTATION" -> open(player, AdminView.ROTATION, null);
                case "RESET" -> open(player, AdminView.RESET, null);
                case "DIAGNOSTICS" -> open(player, AdminView.DIAGNOSTICS, null);
                case "BACK" -> open(player, AdminView.ARENAS, null);
                case "SET_LOBBY" -> {
                    String arenaId = arenaContext.get(player.getUniqueId());
                    if (arenaId != null) {
                        plugin.arenas().setArenaSpawn(arenaId, player.getLocation());
                        player.sendMessage(plugin.messages().get("wizard.lobby-set"));
                        open(player, AdminView.ARENA_EDITOR, arenaId, arenaId);
                    }
                }
                case "TEAM_SPAWN" -> {
                    String arenaId = arenaContext.get(player.getUniqueId());
                    String teamStr = pdc.get(plugin.teamKey(), PersistentDataType.STRING);
                    if (arenaId != null && teamStr != null) {
                        try {
                            TeamColor color = TeamColor.valueOf(teamStr);
                            plugin.arenas().setTeamSpawn(arenaId, color, player.getLocation());
                            player.sendMessage(plugin.messages().get("wizard.spawn-set", Map.of("team", color.name())));
                            open(player, AdminView.ARENA_EDITOR, arenaId, arenaId);
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }
                case "TEAM_BED" -> {
                    String arenaId = arenaContext.get(player.getUniqueId());
                    String teamStr = pdc.get(plugin.teamKey(), PersistentDataType.STRING);
                    if (arenaId != null && teamStr != null) {
                        var block = player.getTargetBlockExact(5);
                        if (block != null && block.getType().name().endsWith("_BED")) {
                            try {
                                TeamColor color = TeamColor.valueOf(teamStr);
                                plugin.arenas().setTeamBed(arenaId, color, block.getLocation());
                                player.sendMessage(plugin.messages().get("wizard.bed-set", Map.of("team", color.name())));
                                open(player, AdminView.ARENA_EDITOR, arenaId, arenaId);
                            } catch (IllegalArgumentException ignored) {
                            }
                        } else {
                            player.sendMessage(plugin.messages().get("wizard.no-bed"));
                        }
                    }
                }
                case "GEN_ADD" -> {
                    String arenaId = arenaContext.get(player.getUniqueId());
                    String typeStr = pdc.get(plugin.genTypeKey(), PersistentDataType.STRING);
                    if (arenaId != null && typeStr != null) {
                        try {
                            GeneratorType type = GeneratorType.valueOf(typeStr);
                            plugin.arenas().addGenerator(arenaId, type, player.getLocation(), 1);
                            player.sendMessage(plugin.messages().get("wizard.gen-added", Map.of("type", type.name())));
                            spawnGenMarker(player, arenaId);
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }
                case "NPC_ITEM" -> {
                    String arenaId = arenaContext.get(player.getUniqueId());
                    if (arenaId != null) {
                        plugin.arenas().addItemShop(arenaId, player.getLocation());
                        spawnNpc(player, arenaId, "item");
                        player.sendMessage(plugin.messages().get("wizard.npc-item"));
                    }
                }
                case "NPC_UPGRADE" -> {
                    String arenaId = arenaContext.get(player.getUniqueId());
                    if (arenaId != null) {
                        plugin.arenas().addUpgradeShop(arenaId, player.getLocation());
                        spawnNpc(player, arenaId, "upgrade");
                        player.sendMessage(plugin.messages().get("wizard.npc-upgrade"));
                    }
                }
                case "SAVE" -> {
                    String arenaId = arenaContext.get(player.getUniqueId());
                    if (arenaId != null) {
                        plugin.arenas().save(arenaId);
                        player.sendMessage(plugin.messages().get("wizard.saved"));
                    }
                }
                case "RELOAD" -> {
                    String arenaId = arenaContext.get(player.getUniqueId());
                    if (arenaId != null) {
                        plugin.arenas().reload(arenaId);
                        player.sendMessage(plugin.messages().get("wizard.reloaded"));
                        open(player, AdminView.ARENA_EDITOR, arenaId, arenaId);
                    }
                }
                default -> {
                }
            }
        }
    }

    private void spawnGenMarker(Player player, String arenaId) {
        ArmorStand stand = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.getPersistentDataContainer().set(plugin.arenaKey(), PersistentDataType.STRING, arenaId);
        stand.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "bw_gen_marker"), PersistentDataType.BYTE, (byte) 1);
    }

    private void spawnNpc(Player player, String arenaId, String type) {
        Villager villager = player.getWorld().spawn(player.getLocation(), Villager.class);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setCollidable(false);
        villager.getPersistentDataContainer().set(plugin.arenaKey(), PersistentDataType.STRING, arenaId);
        villager.getPersistentDataContainer().set(plugin.npcKey(), PersistentDataType.STRING, type);
    }

    private void startCreate(Player player) {
        player.closeInventory();
        prompt(player, plugin.messages().get("wizard.enter-id"), input -> {
            String id = input.trim().toLowerCase(Locale.ROOT);
            if (!id.matches("[a-z0-9_-]+")) {
                player.sendMessage(plugin.messages().get("wizard.invalid-id"));
                return;
            }
            if (plugin.arenas().exists(id)) {
                player.sendMessage(plugin.messages().get("wizard.id-taken"));
                return;
            }
            plugin.arenas().create(id, player.getWorld().getName());
            player.sendMessage(plugin.messages().get("wizard.created", Map.of("arena", id)));
            open(player, AdminView.ARENA_EDITOR, id, id);
        });
    }

    private void prompt(Player player, String message, Consumer<String> handler) {
        player.sendMessage(message);
        prompts.put(player.getUniqueId(), new Prompt(handler, System.currentTimeMillis() + 30000));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Prompt prompt = prompts.get(event.getPlayer().getUniqueId());
        if (prompt != null) {
            event.setCancelled(true);
            prompts.remove(event.getPlayer().getUniqueId());
            if (System.currentTimeMillis() > prompt.expire) {
                Bukkit.getScheduler().runTask(plugin, () -> event.getPlayer().sendMessage(plugin.messages().get("wizard.timeout")));
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> prompt.handler.accept(event.getMessage()));
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        // nothing for now
    }
}
