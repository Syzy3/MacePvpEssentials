package dev.macepvpessentials.gui;

import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

final class ElytraPreviewButton extends Button {
    private static final Identifier ICON = Identifier.fromNamespaceAndPath("mace-pvp-essentials", "elytra_outline");
    private final BooleanSupplier on;

    ElytraPreviewButton(int x, int y, int size, BooleanSupplier on, Button.OnPress onPress) {
        super(x, y, size, size, Component.literal("Elytra preview"), onPress, DEFAULT_NARRATION);
        this.on = on;
    }

    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.extractDefaultSprite(graphics);
        float alpha = !this.active ? 0.25F : (this.on.getAsBoolean() ? 1.0F : 0.4F);
        int x = this.getX() + (this.getWidth() - 16) / 2;
        int y = this.getY() + (this.getHeight() - 16) / 2;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ICON, x, y, 16, 16, alpha);
    }
}
