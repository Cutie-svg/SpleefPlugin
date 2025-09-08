package org.examplef.spleef.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
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
        Arena arena = spleef.getArenaManager().getArena(player);

        if (arena == null) return;

        if (arena.getState() == GameState.COUNTDOWN || arena.getState() == GameState.RECRUITING) {
            e.setCancelled(true);
            return;
        }

        if (arena.getState() == GameState.LIVE) {
            Material block = e.getBlock().getType();

            if (block == Material.SNOW_BLOCK) {
                Bukkit.getScheduler().runTaskLater(spleef, () -> {
                    if (player.isOnline()) {
                        player.getInventory().addItem(new ItemStack(Material.SNOWBALL, 4));
                    }
                }, 3L);
            } else {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        Projectile projectile = e.getEntity();

        if (!(projectile instanceof Snowball)) return;

        Block hitBlock = e.getHitBlock();
        if (hitBlock == null) return;

        if (hitBlock.getType() == Material.SNOW_BLOCK) {
            Bukkit.getScheduler().runTaskLater(spleef, () -> {
                hitBlock.breakNaturally();
            }, 1L);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Arena arena = spleef.getArenaManager().getArena(player);

        if (arena == null) return;

        if (arena.getState() == GameState.LIVE && arena.getAlivePlayers().contains(player)) {
            double yLimit = spleef.getConfig().getDouble("arenas." + arena.getMap() + ".y");
            if (player.getLocation().getY() < yLimit) {
                arena.eliminatePlayer(player);
            }
        }
    }
}
