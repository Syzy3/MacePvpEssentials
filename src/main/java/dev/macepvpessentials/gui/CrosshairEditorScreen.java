package dev.macepvpessentials.gui;

import dev.macepvpessentials.config.Config;
import dev.macepvpessentials.crosshair.CrosshairInfo;
import dev.macepvpessentials.crosshair.CrosshairRenderer;
import dev.macepvpessentials.crosshair.CrosshairSettings;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public final class CrosshairEditorScreen extends Screen {
    private static final int COLUMN = 108;
    private static final int HEIGHT = 18;
    private static final int ROW = 20;
    private static final int TOP = 30;
    private static final int PREVIEW_W = 96;
    private static final int PREVIEW_H = 118;
    private static final int RIGHT_BUTTON = 1;
    private static final int ELYTRA_BUTTON = 20;
    static boolean simulateElytra = false;
    private final Screen parent;
    private Button colorButton;

    public CrosshairEditorScreen(Screen parent) {
        super(Component.literal("Crosshair Editor"));
        this.parent = parent;
    }

    private CrosshairSettings s() {
        return Config.get().crosshair;
    }

    private int left() {
        return this.width / 2 - 174;
    }

    protected void init() {
        int col1 = this.left();
        int col2 = col1 + 108 + 4;
        CrosshairSettings s = this.s();
        this.toggle(col1, row(0), "Custom", () -> s.custom, (v) -> s.custom = v, "Off uses Minecraft's crosshair.");
        this.cycle(col1, row(1), "Style", (st) -> st.style.label(), (st) -> st.style = st.style.next());
        this.colorButton = this.cycle(col1, row(2), "Color", (st) -> st.color.label(), (st) -> st.color = st.color.next());
        this.colorButton.setTooltip(Tooltip.create(Component.literal("Right-click for a custom color.")));
        this.addRenderableWidget(new IntSlider(col1, row(3), 108, 18, "Opacity", 10, 100, s.opacity, (v) -> v + "%", (v) -> s.opacity = Math.max(10, Math.round((float)v / 5.0F) * 5)));
        this.toggle(col1, row(4), "Outline", () -> s.outline, (v) -> s.outline = v, (String)null);
        this.toggle(col1, row(5), "Cooldown", () -> s.showCooldown, (v) -> s.showCooldown = v, "Shows your mace's attack charge, or the held item's cooldown.");
        this.addRenderableWidget(new IntSlider(col2, row(0), 108, 18, "Length", 1, 12, s.length, Integer::toString, (v) -> s.length = v));
        this.addRenderableWidget(new IntSlider(col2, row(1), 108, 18, "Thickness", 1, 6, s.thickness, Integer::toString, (v) -> s.thickness = v));
        this.addRenderableWidget(new IntSlider(col2, row(2), 108, 18, "Gap", 0, 10, s.gap, Integer::toString, (v) -> s.gap = v));
        this.toggle(col2, row(3), "Center Dot", () -> s.centerDot, (v) -> s.centerDot = v, (String)null);
        this.toggle(col2, row(4), "Target Color", () -> s.targetColor, (v) -> s.targetColor = v, "Turns red while aiming at a player.");
        this.toggle(col2, row(5), "Elytra", () -> s.showElytra, (v) -> s.showElytra = v, "Shows an elytra icon while you wear one.");
        ElytraPreviewButton elytraButton = (ElytraPreviewButton)this.addRenderableWidget(new ElytraPreviewButton(col1 + 216 + 12 + 96 + 4, 30, 20, () -> simulateElytra, (b) -> simulateElytra = !simulateElytra));
        elytraButton.setTooltip(Tooltip.create(Component.literal("Shows the preview as if you're wearing an elytra.")));
        this.addRenderableWidget(Button.builder(Component.literal("Reset"), (b) -> {
            boolean custom = this.s().custom;
            Config.get().crosshair = new CrosshairSettings();
            Config.get().crosshair.custom = custom;
            Config.save();
            this.rebuildWidgets();
        }).bounds(this.width / 2 - 102, this.height - 26, 100, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("Done"), (b) -> this.onClose()).bounds(this.width / 2 + 2, this.height - 26, 100, 18).build());
    }

    private static int row(int i) {
        return 30 + i * 20;
    }

    private void toggle(int x, int y, String name, BooleanSupplier get, Consumer<Boolean> set, String tooltip) {
        Button b = Button.builder(Component.literal(name + ": " + (get.getAsBoolean() ? "On" : "Off")), (btn) -> {
            set.accept(!get.getAsBoolean());
            btn.setMessage(Component.literal(name + ": " + (get.getAsBoolean() ? "On" : "Off")));
        }).bounds(x, y, 108, 18).build();
        if (tooltip != null) {
            b.setTooltip(Tooltip.create(Component.literal(tooltip)));
        }

        this.addRenderableWidget(b);
    }

    private Button cycle(int x, int y, String name, Function<CrosshairSettings, String> label, Consumer<CrosshairSettings> next) {
        return (Button)this.addRenderableWidget(Button.builder(Component.literal(name + ": " + (String)label.apply(this.s())), (btn) -> {
            next.accept(this.s());
            btn.setMessage(Component.literal(name + ": " + (String)label.apply(this.s())));
        }).bounds(x, y, 108, 18).build());
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 1 && this.colorButton != null && this.colorButton.isMouseOver(event.x(), event.y())) {
            Minecraft.getInstance().gui.setScreen(new ColorPickerScreen(this));
            return true;
        } else {
            return super.mouseClicked(event, doubleClick);
        }
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width / 2, 12, -1);
        drawPreview(graphics, this.s(), this.left() + 216 + 12, 30, 96, 118, this.s().rgb());
    }

    static void drawPreview(GuiGraphicsExtractor graphics, CrosshairSettings s, int px, int py, int w, int h, int rgb) {
        int sky = py + h * 55 / 100;
        int grass = sky + h * 20 / 100;
        graphics.fill(px - 1, py - 1, px + w + 1, py + h + 1, -16777216);
        graphics.fill(px, py, px + w, sky, -7951896);
        graphics.fill(px, sky, px + w, grass, -10577348);
        graphics.fill(px, grass, px + w, py + h, -8750470);
        boolean elytra = s.showElytra && previewElytra();
        int reach = CrosshairRenderer.reach(s);
        int across = reach + (elytra ? 11 : 2);
        int down = reach + (s.showCooldown ? 7 : 2);
        float scale = Math.max(1.0F, Math.min(4.0F, Math.min(((float)w / 2.0F - 3.0F) / (float)across, ((float)h / 2.0F - 3.0F) / (float)down)));
        float demoCooldown = (float)(System.currentTimeMillis() % 1600L) / 1600.0F;
        graphics.enableScissor(px, py, px + w, py + h);
        graphics.pose().pushMatrix();
        graphics.pose().translate((float)px + (float)w / 2.0F, (float)py + (float)h / 2.0F);
        graphics.pose().scale(scale, scale);
        CrosshairRenderer.draw(graphics, s, 0, 0, rgb, demoCooldown, previewElytra());
        graphics.pose().popMatrix();
        graphics.disableScissor();
    }

    private static boolean previewElytra() {
        LocalPlayer player = Minecraft.getInstance().player;
        return simulateElytra || player != null && CrosshairInfo.wearingElytra(player);
    }

    public void onClose() {
        Config.save();
        Minecraft.getInstance().gui.setScreen(this.parent);
    }
}
