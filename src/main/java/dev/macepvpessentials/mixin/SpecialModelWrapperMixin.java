package dev.macepvpessentials.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.macepvpessentials.ShieldStatus;
import dev.macepvpessentials.TintedComponents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Attaches the Shield Status tint to a shield while the game still knows who's holding it. */
@Mixin(SpecialModelWrapper.class)
public abstract class SpecialModelWrapperMixin {

    @WrapOperation(method = "update", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/special/SpecialModelRenderer;extractArgument(Lnet/minecraft/world/item/ItemStack;)Ljava/lang/Object;"))
    private Object macepvpessentials$shieldTint(SpecialModelRenderer<?> renderer, ItemStack stack, Operation<Object> original,
                                                ItemStackRenderState output, ItemStack item, ItemModelResolver resolver,
                                                ItemDisplayContext displayContext, ClientLevel level, ItemOwner owner, int seed) {
        Object argument = original.call(renderer, stack);
        if (renderer instanceof ShieldSpecialRenderer && argument instanceof DataComponentMap components) {
            int tint = ShieldStatus.tint(owner, stack);
            if (tint != 0) {
                return new TintedComponents(components, tint);
            }
        }
        return argument;
    }
}
