package dev.macepvpessentials.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.macepvpessentials.MaceSlots;
import dev.macepvpessentials.config.Config;
import dev.macepvpessentials.crosshair.CrosshairInfo;
import dev.macepvpessentials.crosshair.CrosshairRenderer;
import dev.macepvpessentials.crosshair.CrosshairSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Breach/Density slot colors in the hotbar and off-hand slot, and the custom crosshair.
 *
 * <p>The crosshair replaces only the one call that draws Minecraft's crosshair sprite, so all
 * of Minecraft's rules for when a crosshair shows (first person, not spectating, ...) and its
 * own attack indicator still apply.
 */
@Mixin(Hud.class)
public abstract class HudMixin {

    @Inject(method = "extractSlot", at = @At("HEAD"))
    private void macepvpessentials$maceSlot(GuiGraphicsExtractor graphics, int x, int y, DeltaTracker deltaTracker,
                                            Player player, ItemStack itemStack, int seed, CallbackInfo ci) {
        MaceSlots.drawBackground(graphics, itemStack, x, y);
    }

    @WrapOperation(method = "extractCrosshair", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
            ordinal = 0))
    private void macepvpessentials$crosshair(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite,
                                             int x, int y, int width, int height, Operation<Void> original,
                                             GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
        CrosshairSettings s = Config.get().crosshair;
        if (!Config.on() || !s.custom) {
            // Default Crosshair is on: plain Minecraft crosshair, nothing added.
            original.call(new Object[]{graphics, pipeline, sprite, x, y, width, height});
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        // Same center pixel as Minecraft's 15x15 crosshair.
        int cx = x + width / 2;
        int cy = y + height / 2;
        float cooldown = player == null ? -1f : CrosshairInfo.cooldown(player, deltaTracker.getGameTimeDeltaPartialTick(false));
        boolean elytra = player != null && CrosshairInfo.wearingElytra(player);
        int rgb = s.currentRgb(CrosshairInfo.onPlayer());
        CrosshairRenderer.draw(graphics, s, cx, cy, rgb, cooldown, elytra);
    }

    /** Third Person on: the crosshair also shows in F5 (Minecraft hides it there). */
    @WrapOperation(method = "extractCrosshair", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private boolean macepvpessentials$thirdPerson(CameraType type, Operation<Boolean> original) {
        CrosshairSettings s = Config.get().crosshair;
        return original.call(type) || (Config.on() && s.custom && s.thirdPerson);
    }

    /** Smash Damage meter, drawn after the crosshair (so only when the crosshair shows). */
    @Inject(method = "extractCrosshair", at = @At("TAIL"))
    private void macepvpessentials$smashMeter(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        dev.macepvpessentials.SmashMeter.draw(graphics, graphics.guiWidth() / 2, graphics.guiHeight() / 2);
    }
}
