package org.examplef.spleef.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.examplef.spleef.Spleef;

import java.io.File;
import java.io.IOException;

public class KnockBackManager {

    private final Spleef spleef;
    private FileConfiguration knockbackConfig;
    private File configFile;

    public KnockBackManager(Spleef spleef) {
        this.spleef = spleef;
        this.configFile = new File(spleef.getDataFolder(), "knockback.yml");
        setupConfig();
    }

    private void setupConfig() {

        if (!spleef.getDataFolder().exists()) {
            spleef.getDataFolder().mkdirs();
        }
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        reloadConfig();
    }

    public void reloadConfig() {
        knockbackConfig = YamlConfiguration.loadConfiguration(configFile);

        setDefaults();
        saveConfig();
    }

    private void setDefaults() {
        knockbackConfig.addDefault("enabled", true);
        knockbackConfig.addDefault("base-strength", 1.5);
        knockbackConfig.addDefault("horizontal-multiplier", 1.0);
        knockbackConfig.addDefault("vertical-multiplier", 0.5);
        knockbackConfig.addDefault("cooldown-ms", 500);
        knockbackConfig.addDefault("require-permission", false);
        knockbackConfig.addDefault("permission-node", "spleef.snowball.kb");

        knockbackConfig.options().copyDefaults(true);
    }

    public void saveConfig() {
        try {
            knockbackConfig.save(configFile);
        } catch (IOException e) {
            spleef.getLogger().severe("Could not save knockback.yml: " + e.getMessage());
        }
    }
    public boolean isEnabled() {
        return knockbackConfig.getBoolean("enabled", true);
    }
    public double getBaseStrength() {
        return knockbackConfig.getDouble("base-strength", 1.5);
    }
    public double getHorizontalMultiplier() {
        return knockbackConfig.getDouble("horizontal-multiplier", 1.0);
    }
    public double getVerticalMultiplier() {
        return knockbackConfig.getDouble("vertical-multiplier", 0.5);
    }
}
