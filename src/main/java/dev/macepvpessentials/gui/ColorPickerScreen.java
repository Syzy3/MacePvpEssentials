package dev.macepvpessentials.gui;

import dev.macepvpessentials.config.Config;
import dev.macepvpessentials.crosshair.CrosshairColor;
import dev.macepvpessentials.crosshair.CrosshairSettings;
import java.util.function.IntConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Red/Green/Blue sliders for a custom color, with a live preview. Slot colors (maces, totems)
 * also get an Opacity slider.
 */
public final class ColorPickerScreen extends Screen {
    private static final int SLIDER_W = 150;
    private static final int HEIGHT = 18;
    private static final int TOP = 34;
    private static final int STEP = 22;
    private static final int PREVIEW = 76;

    /** Draws a preview of the picked color in a {@code size} x {@code size} box at (x, y). */
    public interface Preview {
        void draw(GuiGraphicsExtractor graphics, int x, int y, int size, int rgb, int opacity);
    }

    /** Gets the picked color and opacity (percent) when Done is pressed. */
    public interface Result {
        void accept(int rgb, int opacity);
    }

    private final Screen parent;
    private final Result onDone;
    private final Preview preview;
    /** Whether there is an Opacity slider. */
    private final boolean hasOpacity;
    private int red;
    private int green;
    private int blue;
    private int opacity;

    /** Picks the crosshair's own custom color. */
    public ColorPickerScreen(Screen parent) {
        this(parent, "Crosshair Color", Config.get().crosshair.rgb(), (rgb) -> {
            CrosshairSettings s = Config.get().crosshair;
            s.customRgb = rgb;
            s.color = CrosshairColor.CUSTOM;
        });
    }

    /** A crosshair color (no opacity); {@code onDone} gets it when Done is pressed. */
    public ColorPickerScreen(Screen parent, String title, int startRgb, IntConsumer onDone) {
        this(parent, title, startRgb, -1, (rgb, opacity) -> onDone.accept(rgb), (g, x, y, size, rgb, opacity) ->
                CrosshairEditorScreen.drawPreview(g, Config.get().crosshair, x, y, size, size, rgb));
    }

    /** A color with an Opacity slider (0-100) unless {@code startOpacity} is negative. */
    public ColorPickerScreen(Screen parent, String title, int startRgb, int startOpacity, Result onDone, Preview preview) {
        super(Component.literal(title));
        this.parent = parent;
        this.onDone = onDone;
        this.preview = preview;
        this.hasOpacity = startOpacity >= 0;
        this.red = startRgb >> 16 & 255;
        this.green = startRgb >> 8 & 255;
        this.blue = startRgb & 255;
        this.opacity = hasOpacity ? Math.max(5, Math.min(100, startOpacity)) : 100;
    }

    private int left() {
        return this.width / 2 - 118;
    }

    private int rgb() {
        return this.red << 16 | this.green << 8 | this.blue;
    }

    @Override
    protected void init() {
        int x = this.left();
        this.addRenderableWidget(new IntSlider(x, TOP, SLIDER_W, HEIGHT, "Red", 0, 255, this.red, Integer::toString, (v) -> this.red = v));
        this.addRenderableWidget(new IntSlider(x, TOP + STEP, SLIDER_W, HEIGHT, "Green", 0, 255, this.green, Integer::toString, (v) -> this.green = v));
        this.addRenderableWidget(new IntSlider(x, TOP + STEP * 2, SLIDER_W, HEIGHT, "Blue", 0, 255, this.blue, Integer::toString, (v) -> this.blue = v));
        if (hasOpacity) {
            this.addRenderableWidget(new IntSlider(x, TOP + STEP * 3, SLIDER_W, HEIGHT, "Opacity", 5, 100, this.opacity,
                    (v) -> v + "%", (v) -> this.opacity = Math.max(5, Math.round(v / 5.0F) * 5)));
        }
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), (b) -> this.back())
                .bounds(this.width / 2 - 102, this.height - 26, 100, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("Done"), (b) -> {
            this.onDone.accept(this.rgb(), this.opacity);
            Config.save();
            this.back();
        }).bounds(this.width / 2 + 2, this.height - 26, 100, 18).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width / 2, 12, -1);
        int x = this.left();
        int sy = TOP + STEP * (hasOpacity ? 4 : 3);
        graphics.fill(x - 1, sy - 1, x + SLIDER_W + 1, sy + 17, 0xFF000000);
        graphics.fill(x, sy, x + SLIDER_W, sy + 16, 0xFF000000 | this.rgb());
        String hex = String.format("#%06X", this.rgb()) + (hasOpacity ? "  " + this.opacity + "%" : "");
        graphics.text(this.font, hex, x, sy + 22, 0xFFA0A0A0, false);
        this.preview.draw(graphics, x + SLIDER_W + 10, TOP, PREVIEW, this.rgb(), this.opacity);
    }

    private void back() {
        Minecraft.getInstance().gui.setScreen(this.parent);
    }

    @Override
    public void onClose() {
        this.back();
    }
}
