package dev.macepvpessentials.gui;

import dev.macepvpessentials.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Offhand Size: separate sizes for a shield and a totem held in your offhand (first person),
 * with a preview of each. The preview draws Minecraft's own textures, so it works from the
 * title screen too.
 */
public final class OffhandSizeScreen extends Screen {

    private static final int BOX_W = 120;
    private static final int BOX_H = 96;
    private static final int GAP = 12;
    private static final int TOP = 30;
    private static final int SLIDER_W = 120;

    private static final Identifier TOTEM = Identifier.withDefaultNamespace("textures/item/totem_of_undying.png");
    /** The shield's front face is 12x22 pixels at (1, 1) in this 64x64 texture. */
    private static final Identifier SHIELD = Identifier.withDefaultNamespace("textures/entity/shield/shield_base_nopattern.png");

    private final Screen parent;

    public OffhandSizeScreen(Screen parent) {
        super(Component.literal("Offhand Size"));
        this.parent = parent;
    }

    private int left() {
        return this.width / 2 - BOX_W - GAP / 2;
    }

    @Override
    protected void init() {
        Config c = Config.get();
        int x = left();
        int y = TOP + BOX_H + 18;
        this.addRenderableWidget(new IntSlider(x, y, SLIDER_W, 20, "Shield", 10, 150, c.offhandShieldSize,
                v -> v + "%", v -> c.offhandShieldSize = v));
        this.addRenderableWidget(new IntSlider(x + BOX_W + GAP, y, SLIDER_W, 20, "Totem", 10, 150, c.offhandTotemSize,
                v -> v + "%", v -> c.offhandTotemSize = v));
        this.addRenderableWidget(Button.builder(Component.literal("Reset"), b -> {
            c.offhandShieldSize = 100;
            c.offhandTotemSize = 100;
            Config.save();
            this.rebuildWidgets();
        }).bounds(this.width / 2 - 102, this.height - 26, 100, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
                .bounds(this.width / 2 + 2, this.height - 26, 100, 18).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width / 2, 12, -1);
        Config c = Config.get();
        int x = left();
        drawBox(graphics, x, "Shield", c.offhandShieldSize, true);
        drawBox(graphics, x + BOX_W + GAP, "Totem", c.offhandTotemSize, false);
    }

    /** A little first-person view: the item sits in the bottom-left corner, like your offhand. */
    private void drawBox(GuiGraphicsExtractor graphics, int x, String name, int size, boolean shield) {
        int y = TOP;
        int sky = y + BOX_H * 60 / 100;
        graphics.fill(x - 1, y - 1, x + BOX_W + 1, y + BOX_H + 1, 0xFF000000);
        graphics.fill(x, y, x + BOX_W, sky, 0xFF86A8E8);
        graphics.fill(x, sky, x + BOX_W, y + BOX_H, 0xFF5E9C3C);
        graphics.enableScissor(x, y, x + BOX_W, y + BOX_H);
        float scale = size / 100.0F;
        if (shield) {
            // Base size at 100%: 36 x 66 (3x the 12 x 22 face).
            int w = Math.round(36 * scale);
            int h = Math.round(66 * scale);
            int left = x + 10;
            int top = y + BOX_H - h + Math.round(14 * scale);
            graphics.blit(RenderPipelines.GUI_TEXTURED, SHIELD, left, top, 1.0F, 1.0F, w, h, 12, 22, 64, 64);
        } else {
            int s = Math.round(56 * scale);
            int left = x + 8;
            int top = y + BOX_H - s;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TOTEM, left, top, 0.0F, 0.0F, s, s, 16, 16, 16, 16);
        }
        graphics.disableScissor();
        graphics.centeredText(this.font, name, x + BOX_W / 2, y + BOX_H + 5, 0xFFA0A0A0);
    }

    @Override
    public void onClose() {
        Config.save();
        Minecraft.getInstance().gui.setScreen(this.parent);
    }
}
