package dev.macepvpessentials.optimize;

import dev.macepvpessentials.config.Config;
import java.util.ArrayDeque;
import java.util.Deque;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;

/**
 * Pearl Optimization. Normally your pearl only shows up once the server's copy reaches you,
 * one ping after you throw. This shows a stand-in pearl the moment you throw, flying the same
 * way, and swaps it out as soon as the server's real pearl arrives. The server still controls
 * the real pearl: where it goes and where you land are unchanged. The throw sound also plays
 * right away, and the server's late copy of it is skipped.
 */
public final class PearlOptimizer {

    /** Stand-ins are removed if the real pearl hasn't shown up after this long (1.5 seconds). */
    private static final int MAX_AGE_TICKS = 30;

    private static final Deque<ThrownEnderpearl> standIns = new ArrayDeque<>();
    private static int nextId = Integer.MIN_VALUE + 1000;
    /** Throw sounds already played here, waiting for the server's copy: position and tick. */
    private static final Deque<Vec3> soundPositions = new ArrayDeque<>();
    private static final Deque<Integer> soundTicks = new ArrayDeque<>();
    private static final int SOUND_WINDOW_TICKS = 40;

    private PearlOptimizer() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (!standIns.isEmpty()) {
                ThrownEnderpearl oldest = standIns.peekFirst();
                if (oldest.isRemoved() || oldest.tickCount > MAX_AGE_TICKS || oldest.level() != client.level) {
                    remove(standIns.pollFirst());
                } else {
                    break;
                }
            }
        });
    }

    public static boolean isStandIn(Entity entity) {
        return entity instanceof ThrownEnderpearl && entity.getId() < 0 && standIns.contains(entity);
    }

    /** Called right after you throw a pearl on your side. */
    public static void onThrow(Player player, ItemStack stack) {
        if (!Config.get().pearlOptimization || !(player instanceof LocalPlayer)) {
            return;
        }
        if (!(player.level() instanceof ClientLevel level)) {
            return;
        }
        // Same as the server: from your eyes, at your look direction, speed 1.5, plus your motion.
        ThrownEnderpearl pearl = new ThrownEnderpearl(level, player, stack.copyWithCount(1));
        pearl.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 0.0F);
        pearl.setId(nextId++);
        level.addEntity(pearl);
        standIns.addLast(pearl);

        // The throw sound, as the server plays it.
        float pitch = 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F);
        level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_PEARL_THROW,
                SoundSource.NEUTRAL, 0.5F, pitch, false);
        soundPositions.addLast(player.position());
        soundTicks.addLast(player.tickCount);
    }

    /** Called when the server adds an entity: your real pearl replaces the oldest stand-in. */
    public static void onEntityAdded(Entity entity) {
        if (standIns.isEmpty() || !(entity instanceof ThrownEnderpearl pearl) || entity.getId() < 0) {
            return;
        }
        LocalPlayer me = Minecraft.getInstance().player;
        if (me != null && pearl.getOwner() == me) {
            remove(standIns.pollFirst());
        }
    }

    private static void remove(ThrownEnderpearl pearl) {
        if (pearl != null && !pearl.isRemoved() && pearl.level() instanceof ClientLevel level) {
            level.removeEntity(pearl.getId(), Entity.RemovalReason.DISCARDED);
        }
    }

    /** True if this sound is the server's copy of a throw sound already played. */
    public static boolean skipServerSound(SoundEvent sound, double x, double y, double z) {
        LocalPlayer me = Minecraft.getInstance().player;
        if (soundTicks.isEmpty() || me == null || sound != SoundEvents.ENDER_PEARL_THROW) {
            return false;
        }
        // Forget throws whose copy never came.
        while (!soundTicks.isEmpty() && me.tickCount - soundTicks.peekFirst() > SOUND_WINDOW_TICKS) {
            soundTicks.pollFirst();
            soundPositions.pollFirst();
        }
        if (soundTicks.isEmpty() || soundPositions.peekFirst().distanceToSqr(x, y, z) > 16.0) {
            return false;
        }
        soundTicks.pollFirst();
        soundPositions.pollFirst();
        return true;
    }
}
