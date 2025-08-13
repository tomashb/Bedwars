package com.example.bedwars.game;

import com.example.bedwars.arena.Arena;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Simple spectator toggles. */
public final class SpectatorService {
  public void toSpectator(Player p, Arena a) {
    p.setGameMode(GameMode.SPECTATOR);
    if (a.lobby() != null) p.teleport(a.lobby());
    p.setCollidable(false);
    p.setInvisible(true);
    p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
  }

  public void fromSpectator(Player p) {
    p.setGameMode(GameMode.SURVIVAL);
    p.setCollidable(true);
    p.setInvisible(false);
    p.removePotionEffect(PotionEffectType.NIGHT_VISION);
  }
}
