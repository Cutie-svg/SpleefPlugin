package org.examplef.spleef.instance;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.examplef.spleef.GameState;
import org.examplef.spleef.Spleef;
import org.examplef.spleef.manager.ConfigManager;

public class CountDown extends BukkitRunnable {

    private final Spleef spleef;
    private Arena arena;

    private int countdownSeconds;;

    public CountDown(Spleef spleef, Arena arena) {
        this.spleef = spleef;
        this.arena = arena;
        this.countdownSeconds = ConfigManager.getCountDownSeconds();
    }

    public void start() {
        arena.setState(GameState.COUNTDOWN);
        runTaskTimer(spleef, 0L, 20L);
    }

    @Override
    public void run() {
        if (countdownSeconds == 0) {
            cancel();
            //arena start
            arena.start();
            return;
        }
        if (countdownSeconds <= 5 || countdownSeconds % 15 == 0) {
            arena.sendMessage(ChatColor.GREEN + "Game will start in " + countdownSeconds + " second" + (countdownSeconds == 1 ? "" : "s") + ".");
        }
        arena.sendTitle(ChatColor.GREEN.toString() + countdownSeconds + " second" + (countdownSeconds == 1 ? "" : "s"), ChatColor.GRAY + "until game starts");
        countdownSeconds--;

    }
    public void setCountdownSeconds(int seconds) { spleef.getConfig().set("countdown-seconds", seconds); }

}

