package org.examplef.spleef.instance;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.loaders.file.FileLoader;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.examplef.spleef.GameState;
import org.examplef.spleef.Spleef;
import org.examplef.spleef.events.gui.SpleefUI;
import org.examplef.spleef.manager.ConfigManager;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Arena {

    private final Spleef spleef;
    private final String map;
    private Location spawn;
    private final Set<UUID> players;
    private final List<Player> alivePlayers;

    private final Game game;
    private GameState state;
    private CountDown countdown;

    private final AdvancedSlimePaperAPI api;

    private SlimeWorld slimeWorld;

    public Arena(Spleef spleef, String map, Location spawn) {
        this.spleef = spleef;
        this.map = map;
        this.spawn = spawn;

        this.players = new HashSet<>();
        this.alivePlayers = new ArrayList<>();
        this.state = GameState.RECRUITING;

        this.game = new Game(this);

        api = spleef.getApi();
    }

    public void start() {
        if (state == GameState.LIVE) return;

        alivePlayers.clear();
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) alivePlayers.add(player);
        }

        setState(GameState.LIVE);
        game.start();
    }

    public void end() {
        if (state != GameState.LIVE && state != GameState.COUNTDOWN) return;

        for (UUID uuid : players) {
            spleef.getPlayerManager().incrementGamesPlayed(uuid);
        }

        if (alivePlayers.size() == 1) {
            Player winner = alivePlayers.get(0);
            spleef.getPlayerManager().incrementWins(winner.getUniqueId());

            for (UUID uuid : players) {
                if (!uuid.equals(winner.getUniqueId())) {
                    spleef.getPlayerManager().incrementLoses(uuid);
                }
            }
        }

        reset();
    }


    public void reset() {
        if (countdown != null) countdown.cancel();

        Location lobby = ConfigManager.getLobbySpawn();

        for (UUID uuid : new HashSet<>(players)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.getInventory().clear();
                player.teleport(lobby);
                player.getInventory().setItem(0, SpleefUI.createCompass(player));
                player.sendTitle("", "");
            }
        }

        players.clear();
        alivePlayers.clear();

        sendTitle("", "");

        resetArena();
    }

    public void resetArena() {
        if (spawn == null || spawn.getWorld() == null) return;

        String worldName = spawn.getWorld().getName();

        World arenaWorld = Bukkit.getWorld(worldName);
        if (arenaWorld != null) {
            Bukkit.unloadWorld(arenaWorld, false);
        }

        File file = new File(Bukkit.getWorldContainer(), "slime_worlds");
        SlimeLoader loader = new FileLoader(file);

        SlimePropertyMap properties = new SlimePropertyMap();
        properties.setValue(SlimeProperties.DIFFICULTY, "peaceful");
        properties.setValue(SlimeProperties.ENVIRONMENT, "normal");
        properties.setValue(SlimeProperties.ALLOW_MONSTERS, false);
        properties.setValue(SlimeProperties.ALLOW_ANIMALS, false);


        CompletableFuture.supplyAsync(() -> {
            try {
                slimeWorld = api.readWorld(loader, worldName, false, properties);
                return slimeWorld;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAcceptAsync(slimeWorld -> {
            if (slimeWorld == null) return;

            Bukkit.getScheduler().runTask(spleef, () -> {
                try {
                    SlimeWorldInstance instance = api.loadWorld(slimeWorld, true);
                    World newWorld = instance.getBukkitWorld();

                    spawn = new Location(newWorld, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getYaw(), spawn.getPitch());

                    Bukkit.getLogger().info("Arena " + map + " reset complete");
                    setState(GameState.RECRUITING);

                } catch (Exception e) {
                    Bukkit.getLogger().severe(e.getMessage());
                }
            });
        });
    }

    public void addPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (!players.add(uuid)) {
            player.sendMessage(ChatColor.RED + "You are already in an arena!");
            return;
        }

        if (isFull()) {
            player.sendMessage(ChatColor.RED + "This arena is full.");
            players.remove(uuid);
            return;
        }

        player.teleport(spawn);
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();

        ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL);
        ItemMeta meta = shovel.getItemMeta();
        meta.addEnchant(Enchantment.EFFICIENCY, 5, true);
        shovel.setItemMeta(meta);
        player.getInventory().addItem(shovel);

        if (state == GameState.RECRUITING && players.size() >= ConfigManager.getRequiredPlayers()) {
            setState(GameState.COUNTDOWN);
            if (countdown != null) countdown.cancel();

            countdown = new CountDown(spleef, this);
            countdown.start();
        }
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        alivePlayers.remove(player);

        player.teleport(ConfigManager.getLobbySpawn());
        player.getInventory().clear();
        player.getInventory().setItem(0, SpleefUI.createCompass(player));
        player.sendTitle("", "");

        if (state == GameState.COUNTDOWN && players.size() < ConfigManager.getRequiredPlayers()) {
            sendMessage(ChatColor.RED + "Not enough players to start");
            countdown.cancel();
            end();
        }

        if (state == GameState.LIVE && players.size() < ConfigManager.getRequiredPlayers()) {
            sendMessage(ChatColor.AQUA + "The game has ended as too many players have left.");
            end();
        }
    }

    public void eliminatePlayer(Player player) {
        alivePlayers.remove(player);
        players.remove(player.getUniqueId());

        spleef.getPlayerManager().incrementLoses(player.getUniqueId());
        spleef.getPlayerManager().incrementGamesPlayed(player.getUniqueId());

        player.sendMessage(ChatColor.RED + "You fell!");
        player.teleport(ConfigManager.getLobbySpawn());
        player.getInventory().clear();
        player.getInventory().setItem(0, SpleefUI.createCompass(player));

        if (alivePlayers.size() <= 1) {
            end();
        }
    }

    // --- Utilities ---

    public void sendMessage(String message) {
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) player.sendMessage(message);
        }
    }

    public void sendTitle(String title, String subtitle) {
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) player.sendTitle(title, subtitle);
        }
    }

    // --- Getters/Setters ---

    public void setState(GameState newState) { this.state = newState; }
    public GameState getState() { return state; }
    public String getMap() { return map; } //
    public Set<UUID> getPlayers() { return players; }
    public List<Player> getAlivePlayers() { return alivePlayers; }
    public boolean isFull() { return players.size() >= 2; }
    public World getWorld() { return spawn.getWorld(); }
}
