package org.examplef.spleef.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.examplef.spleef.Spleef;
import org.examplef.spleef.instance.Arena;
import org.examplef.spleef.manager.ConfigManager;

public class ConnectListener implements Listener {

    private final Spleef spleef;

    public ConnectListener(Spleef spleef) { this.spleef = spleef; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().teleport(ConfigManager.getLobbySpawn());
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Arena arena = spleef.getArenaManager().getArena(e.getPlayer());

        if (arena != null) {
           arena.removePlayer(e.getPlayer());
        }
    }
}
