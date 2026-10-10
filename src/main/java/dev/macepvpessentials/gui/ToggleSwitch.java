package dev.macepvpessentials.gui;

import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * A small on/off switch: a rounded track that is green when on and grey when off, with a knob
 * that sits on the right (on) or left (off), and "On" / "Off" written beside it.
 */
final class ToggleSwitch extends Button {

    private static final int TRACK_W = 24;
    private static final int TRACK_H = 12;

    private final BooleanSupplier on;

    ToggleSwitch(int x, int y, BooleanSupplier on, Button.OnPress onPress) {
        super(x, y, TRACK_W + 4 + 18, TRACK_H, Component.literal("Mod on/off"), onPress, DEFAULT_NARRATION);
        this.on = on;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        boolean isOn = on.getAsBoolean();
        int x = this.getX();
        int y = this.getY();
        int border = this.isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF000000;
        int track = isOn ? 0xFF3FA33F : 0xFF5A5A5A;

        // Track with cut corners, so it reads as rounded at this size.
        graphics.fill(x + 1, y, x + TRACK_W - 1, y + TRACK_H, border);
        graphics.fill(x, y + 1, x + TRACK_W, y + TRACK_H - 1, border);
        graphics.fill(x + 1, y + 1, x + TRACK_W - 1, y + TRACK_H - 1, track);

        // Knob.
        int knob = TRACK_H - 2;
        int kx = isOn ? x + TRACK_W - 1 - knob : x + 1;
        graphics.fill(kx, y + 1, kx + knob, y + 1 + knob, 0xFF000000);
        graphics.fill(kx + 1, y + 2, kx + knob - 1, y + knob, 0xFFE0E0E0);

        graphics.text(net.minecraft.client.Minecraft.getInstance().font, isOn ? "On" : "Off",
                x + TRACK_W + 4, y + 2, isOn ? 0xFF7CFC7C : 0xFFA0A0A0, true);
    }
}
