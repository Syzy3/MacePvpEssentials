package dev.macepvpessentials.gui;

import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * A small on/off switch made from Minecraft's own slider pieces: the dark slider track with the
 * stone handle sitting on the right (on) or left (off), and "ON" / "OFF" written in the empty
 * side in green or red.
 */
final class ToggleSwitch extends Button {

    private static final Identifier TRACK = Identifier.withDefaultNamespace("widget/slider");
    private static final Identifier HANDLE = Identifier.withDefaultNamespace("widget/slider_handle");
    private static final Identifier HANDLE_HOVER = Identifier.withDefaultNamespace("widget/slider_handle_highlighted");
    private static final int WIDTH = 44;
    private static final int HEIGHT = 16;
    private static final int HANDLE_W = 8;

    private final BooleanSupplier on;

    ToggleSwitch(int x, int y, BooleanSupplier on, Button.OnPress onPress) {
        super(x, y, WIDTH, HEIGHT, Component.literal("Mod on/off"), onPress, DEFAULT_NARRATION);
        this.on = on;
    }

    static int width() {
        return WIDTH;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        boolean isOn = on.getAsBoolean();
        int x = this.getX();
        int y = this.getY();
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TRACK, x, y, WIDTH, HEIGHT);
        int hx = isOn ? x + WIDTH - HANDLE_W : x;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.isHoveredOrFocused() ? HANDLE_HOVER : HANDLE,
                hx, y, HANDLE_W, HEIGHT);
        String label = isOn ? "ON" : "OFF";
        int textW = Minecraft.getInstance().font.width(label);
        int free = WIDTH - HANDLE_W;
        int tx = (isOn ? x : x + HANDLE_W) + (free - textW) / 2;
        graphics.text(Minecraft.getInstance().font, label, tx, y + (HEIGHT - 8) / 2,
                isOn ? 0xFF55FF55 : 0xFFFF5555, true);
    }
}
