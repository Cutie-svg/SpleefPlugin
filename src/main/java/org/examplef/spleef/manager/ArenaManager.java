package org.examplef.spleef.manager;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.examplef.spleef.Spleef;
import org.examplef.spleef.instance.Arena;

import java.util.ArrayList;
import java.util.List;

public class ArenaManager {

    private List<Arena> arenas = new ArrayList<>();
    private final Spleef spleef;

    public ArenaManager(Spleef spleef) {
        this.spleef = spleef;
    }

    public void loadArenas() {
        arenas.clear();

        FileConfiguration config = spleef.getConfig();

        if (config.getConfigurationSection("arenas") == null) return;

        for (String key : config.getConfigurationSection("arenas").getKeys(false)) {
            arenas.add(new Arena(
                    spleef,
                    key,
                    new Location(
                            Bukkit.getWorld(config.getString("arenas." + key + ".world")),
                            config.getDouble("arenas." + key + ".x"),
                            config.getDouble("arenas." + key + ".y"),
                            config.getDouble("arenas." + key + ".z"),
                            (float) config.getDouble("arenas." + key + ".yaw"),
                            (float) config.getDouble("arenas." + key + ".pitch"))
            ));
        }
    }

    public void reload() {
        ConfigManager.setConfig(spleef);
        loadArenas();

        for (Arena arena : arenas) {
            if (arena.getWorld() != null) {
                arena.getWorld().setDifficulty(Difficulty.PEACEFUL);
                arena.getWorld().setSpawnFlags(false, false);
                arena.getWorld().setStorm(false);
                arena.getWorld().setThundering(false);
            }
        }
    }

    public List<Arena> getArenas() {
        return arenas;
    }

    public Arena getArena(Player player) {
        for (Arena arena : arenas) {
            if (arena.getPlayers().contains(player.getUniqueId())) {
                return arena;
            }
        }
        return null;
    }

    public Arena getArena(String map) {
        for (Arena arena : arenas) {
            if (arena.getMap().equalsIgnoreCase(map)) {
                return arena;
            }
        }
        return null;
    }
}
