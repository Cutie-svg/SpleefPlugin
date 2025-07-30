package org.examplef.spleef.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.examplef.spleef.GameState;
import org.examplef.spleef.Spleef;
import org.examplef.spleef.instance.Arena;
import org.examplef.spleef.instance.CountDown;

public class GameListener implements Listener {

    private final Spleef spleef;

    private Arena arena;
    private CountDown countDown;

    public GameListener(Spleef spleef) {
        this.spleef = spleef;

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Arena arena = spleef.getArenaManager().getArena(player);

        if (arena == null) return;

        if (arena.getState() == GameState.COUNTDOWN) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Wait for the game to start!");
            return;
        }

        if (arena.getState() == GameState.LIVE) {
            Material block = e.getBlock().getType();

            if (block == Material.SNOW_BLOCK) {
                player.getInventory().addItem(new ItemStack(Material.SNOWBALL, 4));
            } else {
                e.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot break this block.");
            }
        }
    }
}
