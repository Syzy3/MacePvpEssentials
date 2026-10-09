package dev.macepvpessentials;

import dev.macepvpessentials.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;

import java.util.HashMap;
import java.util.Map;

/**
 * Shield Status: shields held by players are tinted green when ready and red while disabled.
 *
 * <p>Your own shield uses its real cooldown. Other players' cooldowns aren't sent to you, but
 * when a shield is disabled the game plays the shield's "disabled" sound right at that player,
 * so a disable sound marks the player it came from (the closest one, holding that shield) as
 * disabled for 5 seconds (how long an axe disables a shield). Each player is tracked on its
 * own: your shield never changes anyone else's, and theirs never change yours.
 */
public final class ShieldStatus {

    /** Multiplied with the shield's texture. */
    private static final int READY = 0xFF66FF66;
    private static final int DISABLED = 0xFFFF5050;

    private static final long DISABLED_TICKS = 100;
    private static final double MATCH_DISTANCE_SQ = 2.0 * 2.0;

    /** Other players' entity ids, mapped to the game time their shield works again. */
    private static final Map<Integer, Long> DISABLED_UNTIL = new HashMap<>();
    private static ClientLevel trackedLevel;

    private ShieldStatus() {
    }

    /** The tint for a shield held by {@code owner}, or 0 for no tint. */
    public static int tint(ItemOwner owner, ItemStack stack) {
        if (!Config.get().shieldStatus || owner == null || !stack.has(DataComponents.BLOCKS_ATTACKS)) {
            return 0;
        }
        LivingEntity living = owner.asLivingEntity();
        if (!(living instanceof Player player)) {
            return 0;
        }
        return isDisabled(player, stack) ? DISABLED : READY;
    }

    private static boolean isDisabled(Player player, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (player == mc.player) {
            return player.getCooldowns().isOnCooldown(stack);
        }
        ClientLevel level = mc.level;
        if (level == null || level != trackedLevel) {
            return false;
        }
        Long until = DISABLED_UNTIL.get(player.getId());
        return until != null && level.getGameTime() < until;
    }

    /** Called for every positioned sound the server plays. */
    public static void onSound(Holder<SoundEvent> sound, double x, double y, double z) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || !Config.get().shieldStatus) {
            return;
        }
        if (level != trackedLevel) {
            DISABLED_UNTIL.clear();
            trackedLevel = level;
        }
        // The sound plays exactly where the disabled player stands, so only the closest player
        // counts. That includes you: if it was your shield, nobody else is marked.
        AbstractClientPlayer closest = null;
        double closestDistance = MATCH_DISTANCE_SQ;
        for (AbstractClientPlayer player : level.players()) {
            double distance = player.distanceToSqr(x, y, z);
            if (distance <= closestDistance) {
                closest = player;
                closestDistance = distance;
            }
        }
        if (closest == null || closest == mc.player) {
            return; // your own shield uses its real cooldown instead
        }
        if (playsDisableSound(closest.getMainHandItem(), sound) || playsDisableSound(closest.getOffhandItem(), sound)) {
            DISABLED_UNTIL.put(closest.getId(), level.getGameTime() + DISABLED_TICKS);
        }
    }

    private static boolean playsDisableSound(ItemStack stack, Holder<SoundEvent> sound) {
        BlocksAttacks blocks = stack.get(DataComponents.BLOCKS_ATTACKS);
        return blocks != null && blocks.disableSound().map(s -> s.value() == sound.value()).orElse(false);
    }
}
