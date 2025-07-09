package org.examplef.spleef;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.A;
import org.examplef.spleef.commands.ArenaCommand;
import org.examplef.spleef.instance.Arena;
import org.examplef.spleef.instance.Game;
import org.examplef.spleef.listeners.ConnectListener;
import org.examplef.spleef.listeners.GameListener;
import org.examplef.spleef.manager.ArenaManager;
import org.examplef.spleef.manager.ConfigManager;

public final class Spleef extends JavaPlugin {

    private ArenaManager arenaManager;

    @Override
    public void onEnable() {
        ConfigManager.setConfig(this);
        arenaManager = new ArenaManager(this);

        Bukkit.getPluginManager().registerEvents(new ConnectListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GameListener(this), this);

        getCommand("arena").setExecutor(new ArenaCommand(this));
    }

    public ArenaManager getArenaManager() { return arenaManager; }
}
