package org.examplef.spleef.instance;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.loaders.file.FileLoader;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.examplef.spleef.GameState;
import org.examplef.spleef.Spleef;
import org.examplef.spleef.manager.ConfigManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Arena {

    private final Spleef spleef;

    private int id;
    private Location spawn;

    private List<UUID> players;
    private List<Player> alivePlayers;

    private Game game;
    private GameState state;
    private CountDown countdown;

    private AdvancedSlimePaperAPI api;

    public Arena(Spleef spleef, int id, Location spawn) {
        this.spleef = spleef;

        this.id = id;
        this.spawn = spawn;
        this.state = GameState.RECRUITING;

        players = new ArrayList<>();
        alivePlayers = new ArrayList<>();
        countdown = new CountDown(spleef, this);

        api = AdvancedSlimePaperAPI.instance();
    }

    /* GAME */

    public void start() {
        game = new Game(spleef, this);

        alivePlayers.clear();

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                alivePlayers.add(player);
            }
        }
        game.start();

    }

    public void reset() {
        if (state == GameState.LIVE) {
            Location loc = ConfigManager.getLobbySpawn();
            for (UUID uuid : players) {
                Player player = Bukkit.getPlayer(uuid);
                player.teleport(loc);
            }
        }
        players.clear();
        alivePlayers.clear();
        sendTitle("", "");

        state = GameState.RECRUITING;

    }

    public void end() {
        if (alivePlayers.size() == 1) {
            Player winner = alivePlayers.get(0);
            sendMessage(ChatColor.AQUA + "The game has ended! The winner was " + winner.getName());
            reset();
            resetArena();
            setState(GameState.RECRUITING);
        } else if (alivePlayers.size() == 0) {
            sendMessage(ChatColor.GREEN + "NO winners this round.");
            resetArena();
        }
    }

    public void resetArena() {
        World arenaWorld = Bukkit.getWorld("spleef_arena");

        for (UUID uuid : players) {

            Player player = Bukkit.getPlayer(uuid);
            player.teleport(spawn);
        }
        Bukkit.unloadWorld(arenaWorld, false);

        File file = new File(spleef.getDataFolder(), "slime_worlds");
        SlimeLoader loader = new FileLoader(file);

        SlimePropertyMap properties = new SlimePropertyMap();

        properties.setValue(SlimeProperties.DIFFICULTY, "peaceful");
        properties.setValue(SlimeProperties.ENVIRONMENT, "normal");
        properties.setValue(SlimeProperties.ALLOW_MONSTERS, false);
        properties.setValue(SlimeProperties.ALLOW_ANIMALS, false);

        CompletableFuture.supplyAsync(() -> {
            try {
                return api.readWorld(loader, "spleef_arena", false, properties);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAcceptAsync(slimeWorld -> {
            if (slimeWorld == null) return;

            Bukkit.getScheduler().runTask(spleef, () ->{
                try {
                    api.loadWorld(slimeWorld, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        });
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
        if (players.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already in an arena!");
            return;
        }

        players.add(player.getUniqueId());
        player.teleport(spawn);
        player.getInventory().clear();

        ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL, 1);
        player.getInventory().addItem(shovel);

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

    public void eliminatePlayer(Player player) {
        if (alivePlayers.contains(player)) {
            alivePlayers.remove(player);
            player.sendMessage(ChatColor.RED + "You fell!");
            player.teleport(ConfigManager.getLobbySpawn());

            end();
        }
    }

    /* GETTERS AND SETTERS */

    public List<UUID> getPlayers() { return players; }
    public int getId() { return id; }
    public void setState(GameState state) { this.state = state; }
    public GameState getState() { return state; }
    public World getWorld() { return spawn.getWorld(); }
    public List<Player> getAlivePlayers() { return alivePlayers; }


}
