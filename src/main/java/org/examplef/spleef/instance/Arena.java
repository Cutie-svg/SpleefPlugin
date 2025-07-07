package org.examplef.spleef.instance;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.examplef.spleef.GameState;
import org.examplef.spleef.Spleef;
import org.examplef.spleef.manager.ConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Arena {

    private final Spleef spleef;

    private int id;
    private Location spawn;

    private List<UUID> players;

    private Game game;
    private GameState state;
    private CountDown countdown;

    public Arena(Spleef spleef, int id, Location spawn) {
        this.spleef = spleef;

        this.id = id;
        this.spawn = spawn;

        players = new ArrayList<>();
    }

    /* GAME */

    public void start() {
        game = new Game(spleef, this);
        game.start();
    }

    public void reset() {
        if (state == GameState.LIVE) {
            Location loc = ConfigManager.getLobbySpawn();
            for (UUID uuid : players) {
                Player player = Bukkit.getPlayer(uuid);
                player.teleport(loc);
            }
            players.clear();
        }
        sendTitle("", "");

        state = GameState.RECRUITING;
        countdown = new CountDown(spleef, this);
    }

    /* TOOLS */

    public void sendMessage(String message) {
        for (UUID uuid : players) {
            Bukkit.getPlayer(uuid).sendMessage(message);
        }
    }

    public void sendTitle(String title, String subtitle) {
        for (UUID uuid : players) {
            Bukkit.getPlayer(uuid).sendTitle(title, subtitle);
        }
    }

    /* PLAYER MANAGEMENT */

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());

        player.teleport(spawn);

        if (state.equals(GameState.RECRUITING) && players.size() >= ConfigManager.getRequiredPlayers()) {
            setState(GameState.COUNTDOWN);
            countdown.start();
        }
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());

        player.teleport(ConfigManager.getLobbySpawn());
        player.sendTitle("", "");

        if (state == GameState.COUNTDOWN && players.size() < ConfigManager.getRequiredPlayers()) {
            sendMessage(ChatColor.RED + "Not enough players to start");
            reset();
            return;
        }

        if (state == GameState.LIVE && players.size() < ConfigManager.getRequiredPlayers()) {
            sendMessage(ChatColor.AQUA + "The game has ended as too many players have left the game.");
            reset();
        }
    }

    /* GETTERS AND SETTERS */

    public List<UUID> getPlayers() { return players; }
    public int getId() { return id; }
    public void setState(GameState state) { this.state = state; }
    public GameState getState() { return state; }
}
