package dev.macepvpessentials.mixin;

import dev.macepvpessentials.MaceSlots;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Breach/Density slot colors in inventories, chests and every other container screen. */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Inject(method = "extractSlot", at = @At("HEAD"))
    private void macepvpessentials$maceSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY,
                                            CallbackInfo ci) {
        MaceSlots.drawBackground(graphics, slot.getItem(), slot.x, slot.y);
    }
}
