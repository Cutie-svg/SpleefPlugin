package org.examplef.spleef.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.examplef.spleef.Spleef;

import java.io.File;
import java.io.IOException;

public class KnockBackManager {

    private final Spleef spleef;
    private FileConfiguration knockbackConfig;
    private final File configFile;

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
            spleef.saveResource("knockback.yml", false);
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
            e.printStackTrace();
        }
    }

    public double getMaxJumpY() { return knockbackConfig.getDouble("max-jump-y"); }
    public double getGroundVerticalKb() { return knockbackConfig.getDouble("ground-vertical-kb"); }
    public double getBaseHorizontalKb() { return knockbackConfig.getDouble("base-horizontal-kb"); }
    public double getExtraHorizontalKb() { return knockbackConfig.getDouble("extra-horizontal-kb"); }
    public double getAirborneHorizDampen() { return knockbackConfig.getDouble("airborne-horiz-dampen"); }
    public double getAirborneVertDampen() { return knockbackConfig.getDouble("airborne-vert-dampen"); }
    public double getSprintingKbDampen() { return knockbackConfig.getDouble("sprinting-kb-dampen"); }
}
