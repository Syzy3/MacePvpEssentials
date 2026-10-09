package dev.macepvpessentials.optimize;

import dev.macepvpessentials.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;

/**
 * Mace Optimization. When a mace smash hit lands, the server stops your fall (sets your up/down
 * speed to 0.01) and sends you that change, which arrives one ping later. This applies the exact
 * same change the moment you hit, then skips the server's late copy so you don't stall twice.
 * Nothing the server doesn't do itself: only predicted when the hit should count.
 */
public final class MaceOptimizer {

    /** The speed MaceItem gives the attacker on a smash hit. */
    private static final double SMASH_Y = 0.01F;
    /** How long to wait for the server's copy before giving up (2 seconds). */
    private static final int ECHO_WINDOW_TICKS = 40;

    private static int predictedTick = -1;
    /** Wind Burst adds its launch on top of the server's copy, so that copy must not be skipped. */
    private static boolean keepServerCopy;

    private MaceOptimizer() {
    }

    /** Called when you attack, before the attack is sent. */
    public static void onAttack(Player player, Entity target) {
        if (!Config.get().maceOptimization || !(player instanceof LocalPlayer)) {
            return;
        }
        if (!player.getMainHandItem().is(Items.MACE) || !MaceItem.canSmashAttack(player)) {
            return;
        }
        // Only when the server will surely count the hit: a living target that isn't
        // blocking, isn't still flashing red from a hit, and isn't in creative/spectator.
        if (!(target instanceof LivingEntity living) || target instanceof ArmorStand
                || !living.isAlive() || living.isDeadOrDying() || living.isBlocking() || living.hurtTime > 0) {
            return;
        }
        if (living instanceof Player other && (other.isCreative() || other.isSpectator())) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, SMASH_Y, motion.z);
        player.resetFallDistance();
        predictedTick = player.tickCount;
        keepServerCopy = player.getMainHandItem().getEnchantments().keySet().stream()
                .anyMatch(e -> e.is(Enchantments.WIND_BURST));
    }

    /** True if this motion update is the server's copy of a bounce already applied. */
    public static boolean skipServerCopy(Entity entity, Vec3 movement) {
        if (predictedTick < 0) {
            return false;
        }
        LocalPlayer me = Minecraft.getInstance().player;
        if (me == null || entity != me) {
            return false;
        }
        int age = me.tickCount - predictedTick;
        if (age < 0 || age > ECHO_WINDOW_TICKS) {
            predictedTick = -1;
            return false;
        }
        if (Math.abs(movement.y - SMASH_Y) > 0.005) {
            return false;
        }
        predictedTick = -1;
        return !keepServerCopy;
    }
}
