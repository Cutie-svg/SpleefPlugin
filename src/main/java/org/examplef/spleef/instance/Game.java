package org.examplef.spleef.instance;

import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

public class Game implements Listener {

    private final Arena arena;

    public Game(Arena arena) {
        this.arena = arena;
    }
    public void start() {
        arena.sendMessage(ChatColor.AQUA + "GAME HAS STARTED!");
    }
}