package org.examplef.spleef.instance;

import org.bukkit.ChatColor;
import org.examplef.spleef.GameState;
import org.examplef.spleef.Spleef;

public class Game {

    private Arena arena;
    private final Spleef spleef;

    public Game(Spleef spleef, Arena arena) {
        this.spleef = spleef;
        this.arena = arena;
    }
    public void start() {
        arena.setState(GameState.LIVE);
        arena.sendMessage(ChatColor.AQUA + "GAME HAS STARTED!");
    }
}
