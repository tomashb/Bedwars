package com.example.bedwars.hud;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.ArenaMode;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.game.PlayerContextService;
import com.example.bedwars.util.ScoreboardConfig;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Renders the sidebar scoreboard for BedWars games without flicker.
 */
public final class ScoreboardManager {
  private final BedwarsPlugin plugin;
  private final PlayerContextService players;
  private final Map<UUID, PlayerBoard> boards = new HashMap<>();
  private final ScoreboardConfig cfg;
  private final TipRotator tips;
  private static final String[] KEYS = {
      "\u00a71","\u00a72","\u00a73","\u00a74","\u00a75","\u00a76","\u00a77","\u00a78",
      "\u00a79","\u00a7a","\u00a7b","\u00a7c","\u00a7d","\u00a7e","\u00a7f"};

  public ScoreboardManager(BedwarsPlugin plugin) {
    this.plugin = plugin;
    this.players = plugin.contexts();
    this.cfg = plugin.scoreboardCfg();
    this.tips = new TipRotator(cfg.tipsPool(), cfg.tipsRotateSeconds());
  }

  private String msg(String key) { return plugin.messages().get(key); }

  /** Attach scoreboard to player if not already. */
  public void attach(Player p) {
    if (boards.containsKey(p.getUniqueId())) return;
    Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
    Objective o = sb.registerNewObjective("bw", org.bukkit.scoreboard.Criteria.DUMMY, "");
    o.setDisplaySlot(DisplaySlot.SIDEBAR);
    for (int i = 0; i < KEYS.length; i++) {
      Team t = sb.registerNewTeam("L" + i);
      t.addEntry(KEYS[i]);
      o.getScore(KEYS[i]).setScore(15 - i);
    }
    p.setScoreboard(sb);
    boards.put(p.getUniqueId(), new PlayerBoard(p, sb));
  }

  /** Detach scoreboard from a single player. */
  public void detach(Player p) {
    boards.remove(p.getUniqueId());
    p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
  }

  /** Detach scoreboard from all players. */
  public void clear() {
    boards.keySet().forEach(id -> {
      Player p = Bukkit.getPlayer(id);
      if (p != null) p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    });
    boards.clear();
  }

  /** Tick rendering for all arenas and players. */
  public void tick() {
    Set<UUID> seen = new HashSet<>();
    tips.tick();
    for (Arena a : plugin.arenas().all()) {
      if (a.state() != GameState.WAITING && a.state() != GameState.STARTING && a.state() != GameState.RUNNING) continue;
      for (Player p : players.playersInArena(a.id())) {
        attach(p);
        if (a.state() == GameState.RUNNING) {
          renderRunning(p, a);
        } else {
          renderWaiting(p, a);
        }
        seen.add(p.getUniqueId());
      }
    }
    Iterator<Map.Entry<UUID, PlayerBoard>> it = boards.entrySet().iterator();
    while (it.hasNext()) {
      var e = it.next();
      if (!seen.contains(e.getKey())) {
        Player p = Bukkit.getPlayer(e.getKey());
        if (p != null) p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        it.remove();
      }
    }
  }

  private void renderRunning(Player p, Arena a) {
    Scoreboard sb = boards.get(p.getUniqueId()).board();
    Objective o = sb.getObjective(DisplaySlot.SIDEBAR);
    if (o != null) o.setDisplayName(color(msg("scoreboard.title")));
    int i = 0;
    set(sb, i++, color("&7Carte: &f" + a.id()));
    set(sb, i++, " ");
    for (TeamColor tc : a.activeTeams()) {
      boolean bed = a.team(tc).bedBlock() != null;
      String sym = bed ? msg("scoreboard.bed_ok") : msg("scoreboard.bed_broken");
      int alive = players.aliveCount(a.id(), tc);
      String line = tc.color + tc.display + " &7" + sym + " &f(" + alive + ")";
      set(sb, i++, color(line));
      if (i >= 10) break;
    }
    set(sb, i++, "  ");
    int dTier = plugin.generators().diamondTier(a.id());
    int eTier = plugin.generators().emeraldTier(a.id());
    boolean showDrop = plugin.getConfig().getBoolean("scoreboard.show_next_drop", false);
    boolean tiersOnly = plugin.getConfig().getBoolean("scoreboard.show_tiers_only", false);
    if (showDrop && !tiersOnly) {
      int dSec = plugin.generators().nextDiamondDropSeconds(a.id());
      int eSec = plugin.generators().nextEmeraldDropSeconds(a.id());
      set(sb, i++, color(msg("scoreboard.diamond_drop_line").replace("{tier}", roman(dTier)).replace("{sec}", String.valueOf(dSec))));
      set(sb, i++, color(msg("scoreboard.emerald_drop_line").replace("{tier}", roman(eTier)).replace("{sec}", String.valueOf(eSec))));
    } else {
      set(sb, i++, color(msg("scoreboard.diamond_line").replace("{tier}", roman(dTier))));
      set(sb, i++, color(msg("scoreboard.emerald_line").replace("{tier}", roman(eTier))));
    }
    set(sb, i++, "   ");
    set(sb, i++, color("&7" + plugin.messages().get("brand.line")));
    while (i < KEYS.length) set(sb, i++, "");
  }

  private void renderWaiting(Player p, Arena a) {
    Scoreboard sb = boards.get(p.getUniqueId()).board();
    Objective o = sb.getObjective(DisplaySlot.SIDEBAR);
    String modeName = modeName(a.mode());
    int online = players.countPlayers(a.id());
    int max = a.mode().teams * a.maxTeamSize();
    int sec = plugin.game().countdownRemaining(a.id());
    String title = cfg.waitingTitle().replace("{mode_name}", modeName);
    if (o != null) o.setDisplayName(color(title));
    int i = 0;
    for (String raw : cfg.waitingLines()) {
      String line = raw
          .replace("{players}", String.valueOf(online))
          .replace("{max_players}", String.valueOf(max))
          .replace("{start_seconds}", String.valueOf(sec))
          .replace("{mode_name}", modeName)
          .replace("{map_name}", a.id())
          .replace("{tip_line_1}", tips.line1())
          .replace("{tip_line_2}", tips.line2())
          .replace("{footer}", cfg.footer());
      set(sb, i++, color(line));
    }
    while (i < KEYS.length) set(sb, i++, "");
  }

  private static String modeName(ArenaMode m) {
    return switch (m) {
      case EIGHT_X1 -> "Solo";
      case EIGHT_X2 -> "Doubles";
      case FOUR_X3 -> "3v3";
      case FOUR_X4 -> "4v4";
    };
  }

  private void set(Scoreboard sb, int line, String text) {
    Team t = sb.getTeam("L" + line);
    if (t != null) setSplit(t, text);
  }

  private void setSplit(Team team, String text) {
    String[] parts = split16(text);
    team.setPrefix(parts[0]);
    team.setSuffix(parts[1]);
  }

  private static String[] split16(String s) {
    if (s == null) s = "";
    if (s.length() <= 16) return new String[] {s, ""};
    return new String[] {s.substring(0, 16), s.substring(16, Math.min(32, s.length()))};
  }

  private static String color(String s) {
    return ChatColor.translateAlternateColorCodes('&', s);
  }

  private static String roman(int n) {
    return switch (n) {
      case 1 -> "I";
      case 2 -> "II";
      case 3 -> "III";
      default -> "-";
    };
  }

  private static final class TipRotator {
    private final List<String> pool;
    private final int rotate;
    private int index = 0;
    private long last = System.currentTimeMillis();

    TipRotator(List<String> pool, int rotate) {
      this.pool = pool;
      this.rotate = rotate;
    }

    void tick() {
      if (pool.isEmpty()) return;
      long now = System.currentTimeMillis();
      if (now - last >= rotate * 1000L) {
        index = (index + 1) % pool.size();
        last = now;
      }
    }

    String line1() {
      if (pool.isEmpty()) return "";
      return pool.get(index);
    }

    String line2() {
      if (pool.isEmpty()) return "";
      return pool.get((index + 1) % pool.size());
    }
  }

  private record PlayerBoard(Player p, Scoreboard board) {}
}
