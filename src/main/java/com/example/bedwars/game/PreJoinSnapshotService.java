package com.example.bedwars.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Stores a snapshot of a player's state before joining an arena. */
public final class PreJoinSnapshotService {
  private final Map<UUID, Snapshot> snapshots = new HashMap<>();

  private record Snapshot(ItemStack[] contents, ItemStack[] armor, GameMode mode) {}

  public void capture(Player p) {
    snapshots.put(p.getUniqueId(),
        new Snapshot(p.getInventory().getContents(),
            p.getInventory().getArmorContents(), p.getGameMode()));
  }

  public void restore(Player p) {
    Optional.ofNullable(snapshots.remove(p.getUniqueId())).ifPresentOrElse(s -> {
      p.getInventory().setContents(s.contents());
      p.getInventory().setArmorContents(s.armor());
      p.setGameMode(s.mode());
    }, () -> {
      p.getInventory().clear();
      p.getInventory().setArmorContents(null);
    });
  }
}
