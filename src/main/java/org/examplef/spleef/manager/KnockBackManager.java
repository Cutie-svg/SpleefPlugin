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

        saveConfig();
    }


    public void saveConfig() {
        try {
            knockbackConfig.save(configFile);
        } catch (IOException e) {
            e.getStackTrace();
        }
    }
    public boolean isEnabled() {
        return knockbackConfig.getBoolean("enabled", true);
    }
    public double getBaseStrength() {
        return knockbackConfig.getDouble("base-strength");
    }
    public double getHorizontalMultiplier() {
        return knockbackConfig.getDouble("horizontal-multiplier");
    }
    public double getVerticalMultiplier() {
        return knockbackConfig.getDouble("vertical-multiplier");
    }
}
