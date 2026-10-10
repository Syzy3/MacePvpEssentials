package dev.macepvpessentials.optimize;

import dev.macepvpessentials.config.Config;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Rocket Optimization. A rocket boost normally starts only once the server's rocket reaches
 * you, one ping after you use it. This starts the boost the moment you use the rocket with a
 * stand-in rocket (same push as vanilla). When the server's real rocket arrives it takes over,
 * and it skips as many boost ticks as the stand-in already gave, so the total boost is the
 * same as vanilla: it just starts sooner. The launch sound plays right away too.
 */
public final class FireworkOptimizer {

    /** A stand-in is removed if the real rocket hasn't shown up after this long (1 second). */
    private static final int MAX_AGE_TICKS = 20;
    private static final int SOUND_WINDOW_TICKS = 40;

    private static final Deque<FireworkRocketEntity> standIns = new ArrayDeque<>();
    /** Real rockets and how many boost ticks they still skip. */
    private static final Map<FireworkRocketEntity, Integer> skipTicks = new WeakHashMap<>();
    private static final Deque<Vec3> soundPositions = new ArrayDeque<>();
    private static final Deque<Integer> soundTicks = new ArrayDeque<>();
    private static int nextId = Integer.MIN_VALUE + 500_000;

    private FireworkOptimizer() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            LocalPlayer me = client.player;
            while (!standIns.isEmpty()) {
                FireworkRocketEntity oldest = standIns.peekFirst();
                boolean stillFlying = me != null && me.isFallFlying();
                if (oldest.isRemoved() || oldest.tickCount > MAX_AGE_TICKS || oldest.level() != client.level || !stillFlying) {
                    remove(standIns.pollFirst());
                } else {
                    break;
                }
            }
        });
    }

    /** Whether using this item right now should start a boost early. */
    public static boolean handles(Player player, ItemStack stack) {
        return Config.get().rocketOptimization && stack.is(Items.FIREWORK_ROCKET) && player.isFallFlying();
    }

    public static boolean isStandIn(Entity entity) {
        return entity.getId() < 0 && entity instanceof FireworkRocketEntity && standIns.contains(entity);
    }

    /** Called right after you use a rocket while gliding, on your side. */
    public static void onUse(Player player, ItemStack stack) {
        if (!handles(player, stack) || !(player instanceof LocalPlayer) || !(player.level() instanceof ClientLevel level)) {
            return;
        }
        FireworkRocketEntity rocket = new FireworkRocketEntity(level, stack.copyWithCount(1), player);
        rocket.setSilent(true);
        rocket.setId(nextId++);
        level.addEntity(rocket);
        standIns.addLast(rocket);

        level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH,
                SoundSource.AMBIENT, 3.0F, 1.0F, false);
        soundPositions.addLast(player.position());
        soundTicks.addLast(player.tickCount);
    }

    /** Called when the server adds an entity: your real rocket replaces the oldest stand-in. */
    public static void onEntityAdded(Entity entity) {
        if (standIns.isEmpty() || entity.getId() < 0 || !(entity instanceof FireworkRocketEntity rocket)) {
            return;
        }
        LocalPlayer me = Minecraft.getInstance().player;
        if (me == null || rocket.getOwner() != me) {
            return;
        }
        FireworkRocketEntity standIn = standIns.pollFirst();
        // The stand-in boosted once per tick it was alive; the real rocket skips that many.
        skipTicks.put(rocket, standIn.tickCount);
        remove(standIn);
    }

    /** True if this rocket's boost this tick should be skipped (the stand-in already gave it). */
    public static boolean skipBoost(FireworkRocketEntity rocket) {
        Integer left = skipTicks.get(rocket);
        if (left == null) {
            return false;
        }
        if (left <= 1) {
            skipTicks.remove(rocket);
        } else {
            skipTicks.put(rocket, left - 1);
        }
        return true;
    }

    /** True if this sound is the server's copy of a launch sound already played. */
    public static boolean skipServerSound(SoundEvent sound, double x, double y, double z) {
        LocalPlayer me = Minecraft.getInstance().player;
        if (me == null || sound != SoundEvents.FIREWORK_ROCKET_LAUNCH || soundTicks.isEmpty()) {
            return false;
        }
        while (!soundTicks.isEmpty() && me.tickCount - soundTicks.peekFirst() > SOUND_WINDOW_TICKS) {
            soundTicks.pollFirst();
            soundPositions.pollFirst();
        }
        // You fly fast, so the server's copy can be a bit further from where you used it.
        if (soundTicks.isEmpty() || soundPositions.peekFirst().distanceToSqr(x, y, z) > 100.0) {
            return false;
        }
        soundTicks.pollFirst();
        soundPositions.pollFirst();
        return true;
    }

    private static void remove(FireworkRocketEntity rocket) {
        if (rocket != null && !rocket.isRemoved() && rocket.level() instanceof ClientLevel level) {
            level.removeEntity(rocket.getId(), Entity.RemovalReason.DISCARDED);
        }
    }
}
