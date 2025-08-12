package com.example.bedwars.arena;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.generator.GeneratorType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Simplified arena model. Only handles joining, leaving and a very
 * small state machine. Real gameplay such as generators or shops is
 * intentionally left out for brevity.
 */
public class Arena {

    private final String name;
    private final BedwarsPlugin plugin;

    // configuration
    private String world;
    private Location lobby;
    private final Set<TeamColor> enabledTeams = EnumSet.noneOf(TeamColor.class);
    private final Map<TeamColor, TeamData> teams = new EnumMap<>(TeamColor.class);
    private final List<Location> itemShops = new ArrayList<>();
    private final List<Location> upgradeShops = new ArrayList<>();
    private final List<GeneratorSpec> generators = new ArrayList<>();

    // runtime
    private final Set<UUID> players = new HashSet<>();
    private GameState state = GameState.WAITING;
    private int countdown = 10; // seconds
    private boolean eventsEnabled = true;

    public Arena(BedwarsPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public Location getLobby() {
        return lobby;
    }

    public void setLobby(Location lobby) {
        this.lobby = lobby;
    }

    public Set<TeamColor> getEnabledTeams() {
        return enabledTeams;
    }

    public void enableTeam(TeamColor team) {
        enabledTeams.add(team);
    }

    public void setTeamSpawn(TeamColor team, Location loc) {
        teams.computeIfAbsent(team, t -> new TeamData()).spawn = loc;
    }

    public Location getTeamSpawn(TeamColor team) {
        TeamData data = teams.get(team);
        return data != null ? data.spawn : null;
    }

    public void setTeamBed(TeamColor team, Location block) {
        TeamData data = teams.computeIfAbsent(team, t -> new TeamData());
        BedData bed = new BedData();
        bed.block = block;
        data.bed = bed;
    }

    public BedData getTeamBed(TeamColor team) {
        TeamData data = teams.get(team);
        return data != null ? data.bed : null;
    }

    public void addItemShop(Location loc) {
        itemShops.add(loc);
    }

    public List<Location> getItemShops() {
        return itemShops;
    }

    public void addUpgradeShop(Location loc) {
        upgradeShops.add(loc);
    }

    public List<Location> getUpgradeShops() {
        return upgradeShops;
    }

    public void addGenerator(GeneratorType type, Location loc, int tier) {
        generators.add(new GeneratorSpec(type, loc, tier));
    }

    public List<GeneratorSpec> getGenerators() {
        return generators;
    }

    public GameState getState() {
        return state;
    }

    public boolean isEventsEnabled() {
        return eventsEnabled;
    }

    public void setEventsEnabled(boolean eventsEnabled) {
        this.eventsEnabled = eventsEnabled;
    }

    /**
     * Returns the number of players currently in the arena.
     * This is primarily used for simple diagnostics output.
     *
     * @return current player count
     */
    public int getPlayerCount() {
        return players.size();
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
        player.sendMessage(plugin.messages().get("arena.join", Map.of("arena", name)));
        if (state == GameState.WAITING && players.size() >= 2) {
            startCountdown();
        }
        plugin.scoreboards().update(this);
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        player.sendMessage(plugin.messages().get("arena.leave", Map.of("arena", name)));
        if (players.isEmpty() && state != GameState.WAITING) {
            reset();
        }
        plugin.scoreboards().update(this);
    }

    private void startCountdown() {
        state = GameState.STARTING;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (countdown <= 0) {
                    state = GameState.RUNNING;
                    broadcast(plugin.messages().get("arena.started", Map.of("arena", name)));
                    plugin.generators().onArenaStart(name);
                    cancel();
                    return;
                }
                broadcast(plugin.messages().get("start.countdown", Map.of("seconds", String.valueOf(countdown))));
                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void reset() {
        state = GameState.WAITING;
        countdown = 10;
        eventsEnabled = true;
    }

    /**
     * Expose current players for scoreboard updates.
     */
    public Set<UUID> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    private void broadcast(String msg) {
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendMessage(msg);
            }
        }
    }

    /** team data container */
    private static class TeamData {
        Location spawn;
        BedData bed;
    }

    /** bed data container */
    public static class BedData {
        public Location block;
    }

    /** generator spec container */
    public static class GeneratorSpec {
        public final GeneratorType type;
        public final Location loc;
        public int tier;

        public GeneratorSpec(GeneratorType type, Location loc, int tier) {
            this.type = type;
            this.loc = loc;
            this.tier = tier;
        }
    }
}
