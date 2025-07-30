package org.examplef.spleef.instance;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
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
import org.examplef.spleef.gui.SpleefUI;
import org.examplef.spleef.manager.ConfigManager;

import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Arena {

    private final Spleef spleef;

    private final int id;
    private Location spawn;

    private final List<UUID> players;
    private final List<Player>  alivePlayers;

    private Game game;
    private GameState state;
    private CountDown countdown;

    private final AdvancedSlimePaperAPI api;

    public Arena(Spleef spleef, int id, Location spawn) {
        this.spleef = spleef;
        this.id = id;
        this.spawn = spawn;

        players = new ArrayList<>();
        alivePlayers = new ArrayList<>();

        state = GameState.RECRUITING;

        api = AdvancedSlimePaperAPI.instance();
    }

    public void start() {
        game = new Game(spleef, this);
        alivePlayers.clear();

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) alivePlayers.add(player);
        }

        game.start();
    }

    public void reset() {
        Location lobby = ConfigManager.getLobbySpawn();

        for (UUID uuid : new ArrayList<>(players)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) player.teleport(lobby);
        }

        if (countdown != null) countdown.cancel();

        players.clear();
        alivePlayers.clear();
        sendTitle("", "");
        state = GameState.RECRUITING;
    }

    public void end() {
        Location lobby = ConfigManager.getLobbySpawn();
        World world = lobby.getWorld();

        if (alivePlayers.size() == 1) {
            Player winner = alivePlayers.get(0);
            sendMessage(ChatColor.AQUA + "The game has ended! The winner was " + winner.getName());
            spleef.getPlayerManager().incrementWins(winner.getUniqueId());
        } else {
            sendMessage(ChatColor.GREEN + "NO winners this round.");
        }

        countdown.setCountdownSeconds(0);

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.getInventory().clear();

                player.teleport(lobby);

                player.getInventory().setItem(0, SpleefUI.createCompass(player));
            }

        }

        world.setDifficulty(Difficulty.PEACEFUL);

        reset();
        resetArena();
        setState(GameState.RECRUITING);
    }

    public void resetArena() {
        World arenaWorld = Bukkit.getWorld("spleef_arena");
        if (arenaWorld != null) Bukkit.unloadWorld(arenaWorld, false);

        File file = new File("slime_worlds");
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

            Bukkit.getScheduler().runTask(spleef, () -> {
                try {
                    SlimeWorldInstance instance = api.loadWorld(slimeWorld, true);
                    World newWorld = instance.getBukkitWorld();
                    spawn = new Location(newWorld, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getYaw(), spawn.getPitch());

                    for (UUID uuid : players) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) player.teleport(spawn);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

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

    public void addPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (players.contains(uuid)) {
            player.sendMessage(ChatColor.RED + "You are already in an arena!");
            return;
        }

        if (isFull()) {
            player.sendMessage(ChatColor.RED + "This arena is full.");
            return;
        }

        players.add(uuid);
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
        player.sendTitle("", "");

        player.getInventory().clear();
        player.getInventory().setItem(0, SpleefUI.createCompass(player));

        if (state == GameState.COUNTDOWN && players.size() < ConfigManager.getRequiredPlayers()) {
            sendMessage(ChatColor.RED + "Not enough players to start");
            countdown.cancel();
            reset();
        }

        if (state == GameState.LIVE && players.size() < ConfigManager.getRequiredPlayers()) {
            sendMessage(ChatColor.AQUA + "The game has ended as too many players have left the game.");
            countdown.cancel();
            reset();
        }
    }

    public void eliminatePlayer(Player player) {
        if (alivePlayers.remove(player)) {
            players.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "You fell!");
            player.teleport(ConfigManager.getLobbySpawn());

            player.getInventory().clear();
            player.getInventory().setItem(0, SpleefUI.createCompass(player));

            if (alivePlayers.size() == 1) end();
        }
    }


    public List<UUID> getPlayers() { return players; }
    public int getId() { return id; }
    public void setState(GameState state) { this.state = state; }
    public GameState getState() { return state; }
    public World getWorld() { return spawn.getWorld(); }
    public List<Player> getAlivePlayers() { return alivePlayers; }
    public boolean isFull() { return players.size() >= 2; }
}
