package org.examplef.spleef.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.examplef.spleef.Spleef;
import org.examplef.spleef.instance.Arena;
import org.examplef.spleef.manager.ConfigManager;

import java.util.UUID;

public class ConnectListener implements Listener {

    private final Spleef spleef;

    public ConnectListener(Spleef spleef) { this.spleef = spleef; }

    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().teleport(ConfigManager.getLobbySpawn());
    }
    public void onQuit(PlayerQuitEvent e) {
        Arena arena = spleef.getArenaManager().getArena(e.getPlayer());

        if (arena != null) {
           arena.removePlayer(e.getPlayer());
        }
    }
}
