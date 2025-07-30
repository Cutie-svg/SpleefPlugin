package org.examplef.spleef.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.examplef.spleef.Spleef;

public class ConfigManager {

    private static FileConfiguration config;

    public static void setConfig(Spleef spleef) {
        ConfigManager.config = spleef.getConfig();
        spleef.saveDefaultConfig();
    }

    public static int getRequiredPlayers() { return config.getInt("required-players"); }
    public static int getCountDownSeconds() { return config.getInt("countdown-seconds"); }

    public static Location getLobbySpawn() {
        return new Location(
                Bukkit.getWorld(config.getString("lobby-spawn.world")),
                config.getDouble("lobby-spawn.x"),
                config.getDouble("lobby-spawn.y"),
                config.getDouble("lobby-spawn.z"),
                (float) config.getDouble("lobby-spawn.yaw"),
                (float) config.getDouble("lobby-spawn.pitch"));
    }

}
