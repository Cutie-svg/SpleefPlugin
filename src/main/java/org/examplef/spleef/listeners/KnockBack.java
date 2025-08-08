package org.examplef.spleef.listeners;

import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;
import org.examplef.spleef.Spleef;
import org.examplef.spleef.manager.KnockBackManager;

public class KnockBack implements Listener {

    private final KnockBackManager kbManager;

    public KnockBack(Spleef spleef) {
        this.kbManager = spleef.getKnockBackManager();
    }

    @EventHandler
    public void onSnowballHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Snowball snowball)) return;
        if (!(e.getHitEntity() instanceof Player player)) return;
        if (!kbManager.isEnabled()) return;

        Vector playerPos = player.getLocation().toVector();
        Vector snowballPos = snowball.getLocation().toVector();
        Vector pushDirection = playerPos.subtract(snowballPos).normalize();
        Vector snowballMomentum = snowball.getVelocity().normalize().multiply(0.3);
        Vector finalDirection = pushDirection.add(snowballMomentum).normalize();

        applyKnockBack(player, finalDirection);
    }

    private void applyKnockBack(Player player, Vector direction) {
        double baseStrength = kbManager.getBaseStrength();
        double horizontal = baseStrength * kbManager.getHorizontalMultiplier();
        double vertical = baseStrength * kbManager.getVerticalMultiplier();

        Vector velocity = new Vector(
                direction.getX() * horizontal,
                (direction.getY() * 0.5 + 0.15) * Math.min(vertical, 0.7),
                direction.getZ() * horizontal
        );

        Vector currentVelocity = player.getVelocity();
        Vector finalVelocity = currentVelocity.multiply(0.2).add(velocity.multiply(0.8));

        player.setVelocity(finalVelocity);
    }
}