package org.examplef.spleef.events;

import org.bukkit.Bukkit;
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

    private static class Pending {
        Vector horiz;
        double vySum;
        int hits;

        Pending(Vector horiz) {
            this.horiz = horiz;
            this.vySum = 0;
            this.hits = 0;
        }
    }

    private final Map<UUID, Pending> pendingByPlayer = new HashMap<>();
    private boolean flushQueued = false;

    public KnockBack(Spleef spleef) {
        this.spleef = spleef;
        this.kbManager = spleef.getKnockBackManager();
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(e.getHitEntity() instanceof Player)) return;
        if (!(e.getEntity() instanceof Snowball)) return;

        Player victim = (Player) e.getHitEntity();
        Snowball snowball = (Snowball) e.getEntity();

        // 1) Direction
        Vector dir = snowball.getVelocity().clone();
        dir.setY(0);
        if (dir.lengthSquared() < 1e-6) {
            dir = victim.getLocation().toVector()
                    .subtract(snowball.getLocation().toVector())
                    .setY(0);
        }
        if (dir.lengthSquared() < 1e-6) return;
        dir.normalize();

        // 2) Magnitude
        double speedScale = Math.min(Math.max(snowball.getVelocity().length(), 0.9), 1.25);
        double baseHorizontalKb = kbManager.getBaseHorizontalKb();
        Vector push = dir.multiply(baseHorizontalKb * speedScale);

        // 3) Damping
        if (!isActuallyOnGround(victim)) push.multiply(kbManager.getAirborneHorizDampen());
        if (victim.isSprinting()) push.multiply(kbManager.getSprintingKbDampen());

        // 4) Vertical pop
        double addVy;
        if (victim.getVelocity().getY() > 0) {
            addVy = kbManager.getExtraHorizontalKb(); // tiny bump if rising
        } else if (isActuallyOnGround(victim)) {
            addVy = kbManager.getGroundVerticalKb();
        } else {
            addVy = kbManager.getAirborneVertDampen();
        }

        // 5) Accumulate per tick
        UUID key = victim.getUniqueId();
        Pending pending = pendingByPlayer.getOrDefault(key, new Pending(new Vector(0,0,0)));
        int hitIndex = pending.hits;
        double w = weightFor(hitIndex);

        pending.horiz.add(push.clone().multiply(w));
        pending.vySum += addVy * w;
        pending.hits += 1;
        pendingByPlayer.put(key, pending);

        pending.horiz = capLength(pending.horiz, baseHorizontalKb * 1.3);

        // 6) Flush at end of tick
        if (!flushQueued) {
            flushQueued = true;
            Bukkit.getScheduler().runTask(spleef, () -> {
                flushQueued = false;
                applyAllPending();
            });
        }
    }

    private void applyAllPending() {
        Iterator<Map.Entry<UUID, Pending>> it = pendingByPlayer.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Pending> entry = it.next();
            it.remove();

            UUID uuid = entry.getKey();
            Pending p = entry.getValue();
            Player victim = Bukkit.getPlayer(uuid);
            if (victim == null || !victim.isOnline()) continue;

            Vector current = victim.getVelocity().clone();
            boolean grounded = isActuallyOnGround(victim);

            Vector horizDir = (p.horiz.lengthSquared() > 1e-9)
                    ? p.horiz.clone().setY(0).normalize()
                    : new Vector(0,0,0);

            double desiredHorizMag = grounded ? kbManager.getBaseHorizontalKb()
                    : kbManager.getBaseHorizontalKb() * kbManager.getAirborneHorizDampen();

            if (victim.isSprinting()) desiredHorizMag *= kbManager.getSprintingKbDampen();

            desiredHorizMag = Math.min(desiredHorizMag, kbManager.getMaxJumpY());

            Vector newVel = current.clone();
            newVel.setX(horizDir.getX() * desiredHorizMag);
            newVel.setZ(horizDir.getZ() * desiredHorizMag);

            double risingTinyPop = kbManager.getExtraHorizontalKb();
            double targetVy = current.getY() > 0 ? Math.max(current.getY(), risingTinyPop)
                    : grounded ? kbManager.getGroundVerticalKb()
                    : kbManager.getAirborneVertDampen();

            newVel.setY(Math.min(targetVy, kbManager.getMaxJumpY()));

            if (current.getY() <= 0 && p.vySum > 0 && newVel.getY() < 0.12) newVel.setY(0.12);

            victim.setVelocity(newVel);

            for (int i = 0; i < p.hits; i++) {
                int delay = i;
                Bukkit.getScheduler().runTaskLater(spleef, () -> {
                    if (!victim.isValid()) return;
                    victim.setNoDamageTicks(0);
                    victim.damage(0.001);
                }, delay);
            }
        }
    }

    private double weightFor(int hitIndex) {
        switch(hitIndex) {
            case 0: return 1.0;
            case 1: return 0.55;
            default: return 0.35;
        }
    }

    private Vector capLength(Vector vec, double max) {
        double len = vec.length();
        if (len > max && len > 1e-9) {
            vec.multiply(max / len);
        }
        return vec;
    }

    private boolean isActuallyOnGround(Player player) {
        return !player.getLocation().clone().subtract(0, 0.1, 0).getBlock().isPassable();
    }
}
