package dev.macepvpessentials.optimize;

import dev.macepvpessentials.config.Config;
import java.util.ArrayDeque;
import java.util.Deque;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Pearl and Wind Charge Optimization. Normally what you throw only shows up once the server's
 * copy reaches you, one ping after you throw. This shows a stand-in the moment you throw,
 * flying the same way, and swaps it out as soon as the server's real one arrives. The throw
 * sound plays right away too, and the server's late copy of it is skipped. The server still
 * controls the real pearl / wind charge: where it goes, where you land and how far the wind
 * pushes you are unchanged.
 */
public final class ThrowOptimizer {

    /** What can be thrown, with how the server throws it. */
    private enum Kind {
        PEARL(SoundEvents.ENDER_PEARL_THROW),
        WIND_CHARGE(SoundEvents.WIND_CHARGE_THROW);

        final SoundEvent sound;
        final Deque<Projectile> standIns = new ArrayDeque<>();
        final Deque<Vec3> soundPositions = new ArrayDeque<>();
        final Deque<Integer> soundTicks = new ArrayDeque<>();

        Kind(SoundEvent sound) {
            this.sound = sound;
        }

        boolean enabled() {
            return this == PEARL ? Config.get().pearlOptimization : Config.get().windChargeOptimization;
        }

        boolean matches(Entity entity) {
            return this == PEARL ? entity instanceof ThrownEnderpearl : entity instanceof WindCharge;
        }
    }

    /** Stand-ins are removed if the real one hasn't shown up after this long (1.5 seconds). */
    private static final int MAX_AGE_TICKS = 30;
    private static final int SOUND_WINDOW_TICKS = 40;

    private static int nextId = Integer.MIN_VALUE + 1000;

    private ThrowOptimizer() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (Kind kind : Kind.values()) {
                while (!kind.standIns.isEmpty()) {
                    Projectile oldest = kind.standIns.peekFirst();
                    if (oldest.isRemoved() || oldest.tickCount > MAX_AGE_TICKS || oldest.level() != client.level) {
                        remove(kind.standIns.pollFirst());
                    } else {
                        break;
                    }
                }
            }
        });
    }

    private static Kind kindOf(ItemStack stack) {
        if (stack.is(Items.ENDER_PEARL)) {
            return Kind.PEARL;
        }
        if (stack.is(Items.WIND_CHARGE)) {
            return Kind.WIND_CHARGE;
        }
        return null;
    }

    /** Whether this item gets a stand-in when thrown (so its use should be watched). */
    public static boolean handles(ItemStack stack) {
        Kind kind = kindOf(stack);
        return kind != null && kind.enabled();
    }

    public static boolean isStandIn(Entity entity) {
        if (entity.getId() >= 0) {
            return false;
        }
        for (Kind kind : Kind.values()) {
            if (kind.standIns.contains(entity)) {
                return true;
            }
        }
        return false;
    }

    /** Called right after you throw on your side. */
    public static void onThrow(Player player, ItemStack stack) {
        Kind kind = kindOf(stack);
        if (kind == null || !kind.enabled() || !(player instanceof LocalPlayer)) {
            return;
        }
        if (!(player.level() instanceof ClientLevel level)) {
            return;
        }
        // Same as the server: from your eyes, your look direction, speed 1.5, plus your motion.
        Projectile standIn = kind == Kind.PEARL
                ? new ThrownEnderpearl(level, player, stack.copyWithCount(1))
                : new WindCharge(player, level, player.position().x(), player.getEyePosition().y(), player.position().z());
        standIn.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 0.0F);
        standIn.setId(nextId++);
        level.addEntity(standIn);
        kind.standIns.addLast(standIn);

        // The throw sound, as the server plays it.
        float pitch = 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F);
        level.playLocalSound(player.getX(), player.getY(), player.getZ(), kind.sound, SoundSource.NEUTRAL, 0.5F, pitch, false);
        kind.soundPositions.addLast(player.position());
        kind.soundTicks.addLast(player.tickCount);
    }

    /** Called when the server adds an entity: your real one replaces the oldest stand-in. */
    public static void onEntityAdded(Entity entity) {
        if (entity.getId() < 0 || !(entity instanceof Projectile projectile)) {
            return;
        }
        LocalPlayer me = Minecraft.getInstance().player;
        if (me == null) {
            return;
        }
        for (Kind kind : Kind.values()) {
            if (!kind.standIns.isEmpty() && kind.matches(entity) && projectile.getOwner() == me) {
                remove(kind.standIns.pollFirst());
                return;
            }
        }
    }

    /** True if this sound is the server's copy of a throw sound already played. */
    public static boolean skipServerSound(SoundEvent sound, double x, double y, double z) {
        LocalPlayer me = Minecraft.getInstance().player;
        if (me == null) {
            return false;
        }
        for (Kind kind : Kind.values()) {
            if (sound != kind.sound || kind.soundTicks.isEmpty()) {
                continue;
            }
            // Forget throws whose copy never came.
            while (!kind.soundTicks.isEmpty() && me.tickCount - kind.soundTicks.peekFirst() > SOUND_WINDOW_TICKS) {
                kind.soundTicks.pollFirst();
                kind.soundPositions.pollFirst();
            }
            if (kind.soundTicks.isEmpty() || kind.soundPositions.peekFirst().distanceToSqr(x, y, z) > 16.0) {
                return false;
            }
            kind.soundTicks.pollFirst();
            kind.soundPositions.pollFirst();
            return true;
        }
        return false;
    }

    private static void remove(Projectile projectile) {
        if (projectile != null && !projectile.isRemoved() && projectile.level() instanceof ClientLevel level) {
            level.removeEntity(projectile.getId(), Entity.RemovalReason.DISCARDED);
        }
    }
}
