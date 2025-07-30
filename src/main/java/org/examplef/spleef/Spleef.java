package org.examplef.spleef;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.examplef.spleef.commands.admin.Terminate;
import org.examplef.spleef.commands.user.ArenaCommand;
import org.examplef.spleef.commands.user.ReloadCommand;
import org.examplef.spleef.gui.SpleefUI;
import org.examplef.spleef.listeners.ConnectListener;
import org.examplef.spleef.listeners.GameListener;
import org.examplef.spleef.listeners.ServerLoadListener;
import org.examplef.spleef.manager.ArenaManager;
import org.examplef.spleef.manager.ConfigManager;
import org.examplef.spleef.manager.database.MongoManager;
import org.examplef.spleef.manager.database.PlayerManager;

public final class Spleef extends JavaPlugin {

    private ArenaManager arenaManager;
    private MongoManager mongoManager;
    private PlayerManager playerManager;

    @Override
    public void onEnable() {
        ConfigManager.setConfig(this);

        arenaManager = new ArenaManager(this);
        mongoManager = new MongoManager();
        playerManager = new PlayerManager(this, mongoManager);

        arenaManager.loadArenas();

        Bukkit.getPluginManager().registerEvents(new ConnectListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GameListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ServerLoadListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SpleefUI(arenaManager), this);

        getCommand("arena").setExecutor(new ArenaCommand(this));
        getCommand("areload").setExecutor(new ReloadCommand(this));
        getCommand("terminate").setExecutor(new Terminate(this));
    }

    @Override
    public void onDisable() {
        mongoManager.disconnect();

    }
    public ArenaManager getArenaManager() { return arenaManager;}
    public PlayerManager getPlayerManager() {return playerManager; }
}
