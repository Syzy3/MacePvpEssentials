package dev.macepvpessentials.mixin;

import dev.macepvpessentials.config.Config;
import dev.macepvpessentials.config.HitboxMode;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws "Elytra only" hitboxes once per frame, after the game's own debug drawing. Off and On
 * cost nothing here (On is Minecraft's own option). Elytra only looks at the player list (a
 * handful of entries), not every entity, and draws only gliding players with the game's own
 * hitbox code. Skipped on servers with reduced debug info, and while Minecraft's own option is
 * on (it already draws every hitbox).
 */
@Mixin(DebugRenderer.class)
public abstract class DebugRendererMixin {

    @Unique
    private EntityHitboxDebugRenderer macepvpessentials$hitboxes;

    @Inject(method = "emitGizmos", at = @At("TAIL"))
    private void macepvpessentials$elytraHitboxes(Frustum frustum, double camX, double camY, double camZ,
                                                  float partialTicks, CallbackInfo ci) {
        if (!Config.on() || Config.get().hitboxes != HitboxMode.ELYTRA_ONLY) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null || mc.player.isReducedDebugInfo()
                || mc.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)) {
            return;
        }
        if (macepvpessentials$hitboxes == null) {
            macepvpessentials$hitboxes = new EntityHitboxDebugRenderer(mc);
        }

        boolean firstPerson = mc.options.getCameraType() == CameraType.FIRST_PERSON;
        for (AbstractClientPlayer player : level.players()) {
            if (!player.isFallFlying() || player.isInvisible()) {
                continue;
            }
            if (firstPerson && player == mc.getCameraEntity()) {
                continue;
            }
            if (!frustum.isVisible(player.getBoundingBox())
                    || !mc.levelExtractor.isEntityVisible(player, frustum, camX, camY, camZ)) {
                continue;
            }
            float entityPartialTicks = mc.getDeltaTracker()
                    .getGameTimeDeltaPartialTick(!level.tickRateManager().isEntityFrozen(player));
            ((EntityHitboxDebugRendererAccessor) macepvpessentials$hitboxes)
                    .macepvpessentials$showHitboxes(player, entityPartialTicks, false);
        }
    }
}
