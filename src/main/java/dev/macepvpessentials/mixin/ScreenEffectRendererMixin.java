package dev.macepvpessentials.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.macepvpessentials.config.Config;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Totem Pop size: scales the item in the totem pop animation. */
@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

    @WrapOperation(method = "renderItemActivationAnimation", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    private void macepvpessentials$popSize(PoseStack poseStack, float x, float y, float z, Operation<Void> original) {
        float size = Config.on() ? Config.get().totemPopSize / 100.0F : 1.0F;
        original.call(poseStack, x * size, y * size, z * size);
    }
}
