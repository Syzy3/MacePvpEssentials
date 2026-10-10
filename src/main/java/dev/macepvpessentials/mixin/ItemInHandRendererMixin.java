package dev.macepvpessentials.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.macepvpessentials.config.Config;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Offhand Size: a shield or totem in your offhand is drawn at its own size (first person). */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @WrapOperation(method = "submitArmWithItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"))
    private void macepvpessentials$offhandSize(ItemInHandRenderer renderer, LivingEntity mob, ItemStack stack,
                                               ItemDisplayContext context, PoseStack poseStack,
                                               SubmitNodeCollector collector, int light, Operation<Void> original,
                                               AbstractClientPlayer player, float frameInterp, float xRot,
                                               InteractionHand hand) {
        int percent = 100;
        if (hand == InteractionHand.OFF_HAND && Config.on()) {
            if (stack.is(Items.SHIELD)) {
                percent = Config.get().offhandShieldSize;
            } else if (stack.is(Items.TOTEM_OF_UNDYING)) {
                percent = Config.get().offhandTotemSize;
            }
        }
        if (percent == 100) {
            original.call(renderer, mob, stack, context, poseStack, collector, light);
            return;
        }
        float scale = percent / 100.0F;
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        original.call(renderer, mob, stack, context, poseStack, collector, light);
        poseStack.popPose();
    }
}
