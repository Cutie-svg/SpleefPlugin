package org.examplef.spleef.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;
import org.examplef.spleef.Spleef;
import org.examplef.spleef.manager.KnockBackManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class KnockBack implements Listener {

    private final Spleef spleef;
    private final KnockBackManager kbManager;

    private final Map<UUID, Pending> pendingByPlayer = new HashMap<>();
    private boolean flushQueued = false;

    public KnockBack(Spleef spleef) {
        this.spleef = spleef;
        this.kbManager = spleef.getKnockBackManager();
    }

    private static class Pending {
        Vector accumulatedForce;
        double accumulatedVy;
        int hits;

        Pending() {
            this.accumulatedForce = new Vector();
            this.accumulatedVy = 0;
            this.hits = 0;
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(e.getHitEntity() instanceof Player victim)) return;
        if (!(e.getEntity() instanceof Snowball snowball)) return;

        // --- Calculate knockback direction ---
        Vector dir = snowball.getVelocity().clone().setY(0);
        if (dir.lengthSquared() < 1e-6) {
            dir = victim.getLocation().toVector().subtract(snowball.getLocation().toVector()).setY(0);
        }
        if (dir.lengthSquared() < 1e-6) return;
        dir.normalize();

        // --- Magnitude scaling based on speed ---
        double speedScale = Math.min(Math.max(snowball.getVelocity().length(), 0.9), 1.5);
        Vector horizontalPush = dir.multiply(kbManager.getBaseHorizontalKb() * speedScale);

        // --- Environmental damping ---
        if (!isGrounded(victim)) horizontalPush.multiply(kbManager.getAirborneHorizDampen());
        if (victim.isSprinting()) horizontalPush.multiply(kbManager.getSprintingKbDampen());

        // --- Vertical knockback ---
        double verticalPush = computeVertical(victim);

        // --- Accumulate hit ---
        UUID id = victim.getUniqueId();
        Pending pending = pendingByPlayer.getOrDefault(id, new Pending());
        double weight = weightForHit(pending.hits);
        pending.accumulatedForce.add(horizontalPush.multiply(weight));
        pending.accumulatedVy += verticalPush * weight;
        pending.hits++;
        pendingByPlayer.put(id, pending);

        pending.accumulatedForce = capVector(pending.accumulatedForce, kbManager.getBaseHorizontalKb() * 1.5);

        // --- Schedule flush at end of tick ---
        if (!flushQueued) {
            flushQueued = true;
            Bukkit.getScheduler().runTask(spleef, this::flushPending);
        }
    }

    private void flushPending() {
        Iterator<Map.Entry<UUID, Pending>> it = pendingByPlayer.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Pending> entry = it.next();
            it.remove();

            Player victim = Bukkit.getPlayer(entry.getKey());
            if (victim == null || !victim.isOnline()) continue;

            Pending p = entry.getValue();
            Vector currentVel = victim.getVelocity();

            // --- Smooth horizontal calculation ---
            Vector horizDir = p.accumulatedForce.clone().setY(0);
            if (horizDir.lengthSquared() > 1e-9) horizDir.normalize();
            double desiredHoriz = isGrounded(victim) ? kbManager.getBaseHorizontalKb() : kbManager.getBaseHorizontalKb() * kbManager.getAirborneHorizDampen();
            if (victim.isSprinting()) desiredHoriz *= kbManager.getSprintingKbDampen();

            Vector newVel = currentVel.clone();
            newVel.setX(horizDir.getX() * desiredHoriz);
            newVel.setZ(horizDir.getZ() * desiredHoriz);

            // --- Smooth vertical handling ---
            double targetVy = Math.min(p.accumulatedVy + currentVel.getY(), kbManager.getMaxJumpY());
            if (targetVy < 0.12 && currentVel.getY() <= 0 && p.accumulatedVy > 0) targetVy = 0.12;
            newVel.setY(targetVy);

            // --- Apply velocity ---
            victim.setVelocity(newVel);

            // --- Tiny damage to reset NoDamageTicks ---
            for (int i = 0; i < p.hits; i++) {
                int delay = i;
                Bukkit.getScheduler().runTaskLater(spleef, () -> {
                    if (!victim.isValid()) return;
                    victim.setNoDamageTicks(0);
                    victim.damage(0.001);
                }, delay);
            }
        }
        flushQueued = false;
    }

    private double computeVertical(Player victim) {
        if (victim.getVelocity().getY() > 0) return kbManager.getExtraHorizontalKb();
        else if (isGrounded(victim)) return kbManager.getGroundVerticalKb();
        else return kbManager.getAirborneVertDampen();
    }

    private double weightForHit(int hitIndex) {
        switch (hitIndex) {
            case 0: return 1.0;
            case 1: return 0.6;
            default: return 0.35;
        }
    }

    private Vector capVector(Vector vec, double max) {
        double len = vec.length();
        if (len > max && len > 1e-9) vec.multiply(max / len);
        return vec;
    }

    private boolean isGrounded(Player player) {
        Block below = player.getLocation().clone().subtract(0, 0.1, 0).getBlock();
        Material mat = below.getType();
        return !mat.isAir() && mat.isSolid();
    }
}
