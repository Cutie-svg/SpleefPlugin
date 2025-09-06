package org.examplef.spleef.instance;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.examplef.spleef.GameState;
import org.examplef.spleef.Spleef;

public class Game implements Listener {

    private final Arena arena;
    private final Spleef spleef;

    public Game(Spleef spleef, Arena arena) {
        this.spleef = spleef;
        this.arena = arena;

        Bukkit.getPluginManager().registerEvents(this, spleef);
    }

    public void start() {
        arena.sendMessage(ChatColor.AQUA + "GAME HAS STARTED!");
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (arena.getState() == GameState.LIVE && arena.getAlivePlayers().contains(player)) {
            if (player.getLocation().getY() < spleef.getConfig().getDouble("arenas." + arena.getMap() + ".y")) {
                arena.eliminatePlayer(player);
            }
        }
    }
}