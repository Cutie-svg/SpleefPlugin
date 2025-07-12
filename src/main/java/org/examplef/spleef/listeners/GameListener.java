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

public class GameListener implements Listener {

    private final Spleef spleef;

    public GameListener(Spleef spleef) {
        this.spleef = spleef;

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();

        Material block = e.getBlock().getType();
        Arena arena = spleef.getArenaManager().getArena(player);


        if (arena != null && arena.getState().equals(GameState.LIVE)) {

            if (block == Material.SNOW_BLOCK) {
                ItemStack snow = new ItemStack(Material.SNOWBALL, 4);
                player.getInventory().addItem(snow);
                player.updateInventory();

            }
            if (block != Material.SNOW_BLOCK) {
                e.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot break this block.");
            }
        }
    }

}
