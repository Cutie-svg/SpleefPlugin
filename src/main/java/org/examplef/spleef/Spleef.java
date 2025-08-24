package org.examplef.spleef;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.examplef.spleef.commands.admin.*;
import org.examplef.spleef.commands.user.ArenaCommand;
import org.examplef.spleef.commands.user.StatsCommand;
import org.examplef.spleef.gui.SetStatsGUI;
import org.examplef.spleef.gui.SpleefUI;
import org.examplef.spleef.gui.StatsUI;
import org.examplef.spleef.events.ConnectListener;
import org.examplef.spleef.events.GameListener;
import org.examplef.spleef.events.KnockBack;
import org.examplef.spleef.events.ServerLoadListener;
import org.examplef.spleef.manager.ArenaManager;
import org.examplef.spleef.manager.ConfigManager;
import org.examplef.spleef.manager.KnockBackManager;
import org.examplef.spleef.manager.database.MongoManager;
import org.examplef.spleef.manager.database.PlayerManager;

public final class Spleef extends JavaPlugin {

    private ArenaManager arenaManager;
    private KnockBackManager knockBackManager;

    private MongoManager mongoManager;
    private PlayerManager playerManager;

    @Override
    public void onEnable() {
        ConfigManager.setConfig(this);

        arenaManager = new ArenaManager(this);
        knockBackManager = new KnockBackManager(this);

        mongoManager = new MongoManager();
        playerManager = new PlayerManager(this, mongoManager);

        arenaManager.loadArenas();

        Bukkit.getPluginManager().registerEvents(new ConnectListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GameListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ServerLoadListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SpleefUI(arenaManager), this);
        Bukkit.getPluginManager().registerEvents(new KnockBack(this), this);
        Bukkit.getPluginManager().registerEvents(new StatsUI(this), this);
        Bukkit.getPluginManager().registerEvents(new SetStatsGUI(this), this);


        getCommand("arena").setExecutor(new ArenaCommand(this));
        getCommand("areload").setExecutor(new ReloadCommand(this));
        getCommand("terminate").setExecutor(new Terminate(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("spleefreset").setExecutor(new ResetStats(this));
        getCommand("set").setExecutor(new SetStatsCommand(this));
        getCommand("spleef").setExecutor(new ManageArena(this));

    }

    @Override
    public void onDisable() {
        mongoManager.disconnect();

    }
    public ArenaManager getArenaManager() { return arenaManager;}
    public KnockBackManager getKnockBackManager() { return knockBackManager; }

    public PlayerManager getPlayerManager() {return playerManager; }
}