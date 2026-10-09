package dev.macepvpessentials.gui;

import dev.macepvpessentials.config.Config;
import dev.macepvpessentials.crosshair.CrosshairColor;
import dev.macepvpessentials.crosshair.CrosshairSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ColorPickerScreen extends Screen {
    private static final int SLIDER_W = 150;
    private static final int HEIGHT = 18;
    private static final int TOP = 34;
    private static final int PREVIEW = 76;
    private final Screen parent;
    private final java.util.function.IntConsumer onDone;
    /** Draws the preview box; the crosshair preview unless told otherwise. */
    private final Preview preview;

    /** Draws a preview of the picked color in a {@code size} x {@code size} box at (x, y). */
    public interface Preview {
        void draw(GuiGraphicsExtractor graphics, int x, int y, int size, int rgb);
    }
    private int red;
    private int green;
    private int blue;

    /** Picks the crosshair's own custom color. */
    public ColorPickerScreen(Screen parent) {
        this(parent, "Crosshair Color", Config.get().crosshair.rgb(), (rgb) -> {
            CrosshairSettings s = Config.get().crosshair;
            s.customRgb = rgb;
            s.color = CrosshairColor.CUSTOM;
        });
    }

    /** Picks a color starting from {@code startRgb}; {@code onDone} gets it when Done is pressed. */
    public ColorPickerScreen(Screen parent, String title, int startRgb, java.util.function.IntConsumer onDone) {
        this(parent, title, startRgb, onDone, (g, x, y, size, rgb) ->
                CrosshairEditorScreen.drawPreview(g, Config.get().crosshair, x, y, size, size, rgb));
    }

    public ColorPickerScreen(Screen parent, String title, int startRgb, java.util.function.IntConsumer onDone,
                             Preview preview) {
        super(Component.literal(title));
        this.preview = preview;
        this.parent = parent;
        this.onDone = onDone;
        this.red = startRgb >> 16 & 255;
        this.green = startRgb >> 8 & 255;
        this.blue = startRgb & 255;
    }

    private int left() {
        return this.width / 2 - 118;
    }

    private int rgb() {
        return this.red << 16 | this.green << 8 | this.blue;
    }

    protected void init() {
        int x = this.left();
        this.addRenderableWidget(new IntSlider(x, 34, 150, 18, "Red", 0, 255, this.red, Integer::toString, (v) -> this.red = v));
        this.addRenderableWidget(new IntSlider(x, 56, 150, 18, "Green", 0, 255, this.green, Integer::toString, (v) -> this.green = v));
        this.addRenderableWidget(new IntSlider(x, 78, 150, 18, "Blue", 0, 255, this.blue, Integer::toString, (v) -> this.blue = v));
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), (b) -> this.back()).bounds(this.width / 2 - 102, this.height - 26, 100, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("Done"), (b) -> {
            this.onDone.accept(this.rgb());
            Config.save();
            this.back();
        }).bounds(this.width / 2 + 2, this.height - 26, 100, 18).build());
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width / 2, 12, -1);
        int x = this.left();
        int sy = 102;
        graphics.fill(x - 1, sy - 1, x + 150 + 1, sy + 17, -16777216);
        graphics.fill(x, sy, x + 150, sy + 16, -16777216 | this.rgb());
        graphics.text(this.font, String.format("#%06X", this.rgb()), x, sy + 22, -6250336, false);
        this.preview.draw(graphics, x + 150 + 10, 34, 76, this.rgb());
    }

    private void back() {
        Minecraft.getInstance().gui.setScreen(this.parent);
    }

    public void onClose() {
        this.back();
    }
}
