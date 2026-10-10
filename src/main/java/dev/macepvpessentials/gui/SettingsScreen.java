package dev.macepvpessentials.gui;

import dev.macepvpessentials.Hitboxes;
import dev.macepvpessentials.config.Config;
import dev.macepvpessentials.config.HitboxMode;
import dev.macepvpessentials.config.SlotColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mace PvP Essentials settings: normal Minecraft buttons, grouped under section headings
 * ("Visuals", …). Each setting is a button that cycles its choices; hovering it shows what the
 * setting does. Opened from the Kinetic screen and from Mod Menu.
 */
public final class SettingsScreen extends Screen {

    private static final int WHITE = 0xFFFFFFFF;
    private static final int SECTION = 0xFFE0C060;
    private static final int LINE = 0xFF5A5A5A;

    private static final int BUTTON_WIDTH = 200;
    private static final int SECTION_WIDTH = 260;
    private static final int VISUALS_TOP = 40;
    private static final int MACE_SLOTS_TOP = 208;
    private static final int CROSSHAIR_TOP = 256;
    private static final int OPTIMIZATION_TOP = 328;
    private static final int HALF_WIDTH = 122;

    /** The settings scroll between the title and the Done button. */
    private static final int LIST_TOP = 30;
    private static final int LIST_BOTTOM_MARGIN = 34;
    private static final int CONTENT_BOTTOM = 444;
    private static final int SCROLL_STEP = 16;

    private final Screen parent;

    /** Widgets inside the scrolling area, with their unscrolled y. */
    private final List<AbstractWidget> scrolled = new ArrayList<>();
    private final List<Integer> baseY = new ArrayList<>();
    private double scroll;
    private Button totemButton;
    private Button breachButton;
    private Button densityButton;

    /** The Hitboxes choice on screen; read on open so the F3+F6 option is reflected. */
    private HitboxMode hitboxes;

    public SettingsScreen(Screen parent) {
        super(Component.literal("Mace PvP Essentials"));
        this.parent = parent;
    }

    /** Named in fabric.mod.json (custom.kinetic.settings) for the Kinetic screen. */
    public static Screen open(Screen parent) {
        return new SettingsScreen(parent);
    }

    @Override
    protected void init() {
        scrolled.clear();
        baseY.clear();
        if (hitboxes == null) {
            hitboxes = Hitboxes.current();
        }

        // Visuals
        Button hitboxButton = Button.builder(Component.literal("Hitboxes: " + hitboxes.label()), b -> {
            hitboxes = hitboxes.next();
            Hitboxes.set(hitboxes);
            b.setMessage(Component.literal("Hitboxes: " + hitboxes.label()));
        }).bounds(this.width / 2 - BUTTON_WIDTH / 2, VISUALS_TOP + 16, BUTTON_WIDTH, 20).build();
        hitboxButton.setTooltip(Tooltip.create(Component.literal("Shows player and mob hitboxes.")));
        addScrolled(hitboxButton);

        // Mace Slots: two buttons side by side.
        int pairLeft = this.width / 2 - HALF_WIDTH - 2;
        Config c = Config.get();
        breachButton = Button.builder(breachLabel(), b -> {
            // From a custom color, go back to the first preset.
            if (c.breachCustom) {
                c.breachCustom = false;
                c.breachColor = SlotColor.values()[0];
            } else {
                c.breachColor = c.breachColor.next();
            }
            Config.save();
            b.setMessage(breachLabel());
        }).bounds(pairLeft, MACE_SLOTS_TOP + 16, HALF_WIDTH, 20).build();
        breachButton.setTooltip(Tooltip.create(Component.literal("Colors Breach mace slots.\nRight-click: custom color.")));
        addScrolled(breachButton);

        densityButton = Button.builder(densityLabel(), b -> {
            if (c.densityCustom) {
                c.densityCustom = false;
                c.densityColor = SlotColor.values()[0];
            } else {
                c.densityColor = c.densityColor.next();
            }
            Config.save();
            b.setMessage(densityLabel());
        }).bounds(pairLeft + HALF_WIDTH + 4, MACE_SLOTS_TOP + 16, HALF_WIDTH, 20).build();
        densityButton.setTooltip(Tooltip.create(Component.literal("Colors Density mace slots.\nRight-click: custom color.")));
        addScrolled(densityButton);

        // Crosshair: Default Crosshair on = Minecraft's own crosshair, so the editor is greyed out.
        Button edit = Button.builder(Component.literal("Edit Crosshair..."),
                b -> Minecraft.getInstance().gui.setScreen(new CrosshairEditorScreen(this)))
                .bounds(this.width / 2 - BUTTON_WIDTH / 2, CROSSHAIR_TOP + 40, BUTTON_WIDTH, 20).build();
        edit.active = Config.get().crosshair.custom;
        Button defaultCrosshair = Button.builder(onOff("Default Crosshair", !Config.get().crosshair.custom), b -> {
            Config.get().crosshair.custom = !Config.get().crosshair.custom;
            Config.save();
            b.setMessage(onOff("Default Crosshair", !Config.get().crosshair.custom));
            edit.active = Config.get().crosshair.custom;
        }).bounds(this.width / 2 - BUTTON_WIDTH / 2, CROSSHAIR_TOP + 16, BUTTON_WIDTH, 20).build();
        defaultCrosshair.setTooltip(Tooltip.create(Component.literal("Minecraft's normal crosshair.\nTurn off to make your own.")));
        addScrolled(defaultCrosshair);
        addScrolled(edit);

        // Game Optimization
        Button mace = Button.builder(onOff("Mace Optimization", Config.get().maceOptimization), b -> {
            Config.get().maceOptimization = !Config.get().maceOptimization;
            Config.save();
            b.setMessage(onOff("Mace Optimization", Config.get().maceOptimization));
        }).bounds(this.width / 2 - BUTTON_WIDTH / 2, OPTIMIZATION_TOP + 16, BUTTON_WIDTH, 20).build();
        mace.setTooltip(Tooltip.create(Component.literal("Instant smash-hit bounce,\nsound and particles.")));
        addScrolled(mace);

        Button pearl = Button.builder(onOff("Pearl Optimization", Config.get().pearlOptimization), b -> {
            Config.get().pearlOptimization = !Config.get().pearlOptimization;
            Config.save();
            b.setMessage(onOff("Pearl Optimization", Config.get().pearlOptimization));
        }).bounds(this.width / 2 - BUTTON_WIDTH / 2, OPTIMIZATION_TOP + 40, BUTTON_WIDTH, 20).build();
        pearl.setTooltip(Tooltip.create(Component.literal("Pearls show instantly.\nServer controls the real one.")));
        addScrolled(pearl);

        Button wind = Button.builder(onOff("Wind Charge Optimization", Config.get().windChargeOptimization), b -> {
            Config.get().windChargeOptimization = !Config.get().windChargeOptimization;
            Config.save();
            b.setMessage(onOff("Wind Charge Optimization", Config.get().windChargeOptimization));
        }).bounds(this.width / 2 - BUTTON_WIDTH / 2, OPTIMIZATION_TOP + 64, BUTTON_WIDTH, 20).build();
        wind.setTooltip(Tooltip.create(Component.literal("Wind charges show instantly.\nServer controls the real one.")));
        addScrolled(wind);

        Button elytra = Button.builder(onOff("Rocket Optimization", Config.get().rocketOptimization), b -> {
            Config.get().rocketOptimization = !Config.get().rocketOptimization;
            Config.save();
            b.setMessage(onOff("Rocket Optimization", Config.get().rocketOptimization));
        }).bounds(this.width / 2 - BUTTON_WIDTH / 2, OPTIMIZATION_TOP + 88, BUTTON_WIDTH, 20).build();
        elytra.setTooltip(Tooltip.create(Component.literal("Rocket boosts start instantly.\nStrict anticheats may flag it.")));
        addScrolled(elytra);

        // Visuals: Shield Status, under Hitboxes
        Button shield = Button.builder(onOff("Shield Status", Config.get().shieldStatus), b -> {
            Config.get().shieldStatus = !Config.get().shieldStatus;
            Config.save();
            b.setMessage(onOff("Shield Status", Config.get().shieldStatus));
        }).bounds(this.width / 2 - BUTTON_WIDTH / 2, VISUALS_TOP + 40, BUTTON_WIDTH, 20).build();
        shield.setTooltip(Tooltip.create(Component.literal("Green when ready,\nred when disabled.")));
        addScrolled(shield);

        // Visuals: Totem Background, under Shield Status
        totemButton = Button.builder(totemLabel(), b -> {
            nextTotem();
            Config.save();
            b.setMessage(totemLabel());
        }).bounds(this.width / 2 - BUTTON_WIDTH / 2, VISUALS_TOP + 64, BUTTON_WIDTH, 20).build();
        totemButton.setTooltip(Tooltip.create(Component.literal("Colors totem slots.\nRight-click: custom color.")));
        addScrolled(totemButton);

        // Visuals: Particles (Minecraft's option, plus None) and Damage Tilt (Minecraft's
        // Accessibility option), so changes show up in Minecraft's menus too.
        Button particles = Button.builder(Component.literal("Particles: " + dev.macepvpessentials.Particles.current().label()), b -> {
            dev.macepvpessentials.Particles.Level next = dev.macepvpessentials.Particles.current().next();
            dev.macepvpessentials.Particles.set(next);
            b.setMessage(Component.literal("Particles: " + next.label()));
        }).bounds(this.width / 2 - BUTTON_WIDTH / 2, VISUALS_TOP + 88, BUTTON_WIDTH, 20).build();
        particles.setTooltip(Tooltip.create(Component.literal("How many particles show.\nSame as Minecraft's setting.")));
        addScrolled(particles);

        int tilt = (int) Math.round(Minecraft.getInstance().options.damageTiltStrength().get() * 100.0);
        IntSlider damageTilt = new IntSlider(this.width / 2 - BUTTON_WIDTH / 2, VISUALS_TOP + 112, BUTTON_WIDTH, 20,
                "Damage Tilt", 1, 100, Math.max(1, tilt), v -> v + "%",
                v -> Minecraft.getInstance().options.damageTiltStrength().set(v / 100.0));
        damageTilt.setTooltip(Tooltip.create(Component.literal("How much the screen shakes\nwhen you get hurt.")));
        addScrolled(damageTilt);

        Button offhand = Button.builder(Component.literal("Offhand & Totem Pop..."),
                b -> Minecraft.getInstance().gui.setScreen(new OffhandSizeScreen(this)))
                .bounds(this.width / 2 - BUTTON_WIDTH / 2, VISUALS_TOP + 136, BUTTON_WIDTH, 20).build();
        offhand.setTooltip(Tooltip.create(Component.literal("Sizes of your offhand shield,\ntotem and the totem pop.")));
        addScrolled(offhand);

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
        setScroll(scroll);
    }

    private void addScrolled(AbstractWidget widget) {
        scrolled.add(widget);
        baseY.add(widget.getY());
        addWidget(widget);
    }

    private int listBottom() {
        return this.height - LIST_BOTTOM_MARGIN;
    }

    private int maxScroll() {
        return Math.max(0, CONTENT_BOTTOM - listBottom());
    }

    /** Moves the settings to the given scroll and hides the ones fully out of view. */
    private void setScroll(double value) {
        scroll = Math.max(0, Math.min(maxScroll(), value));
        for (int i = 0; i < scrolled.size(); i++) {
            AbstractWidget w = scrolled.get(i);
            int y = baseY.get(i) - (int) scroll;
            w.setY(y);
            w.visible = y + w.getHeight() > LIST_TOP && y < listBottom();
        }
    }

    private boolean inList(double mouseY) {
        return mouseY >= LIST_TOP && mouseY < listBottom();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll() > 0) {
            setScroll(scroll - scrollY * SCROLL_STEP);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    /** "Totem Background: Off", or the color name shown in its color. */
    private static Component totemLabel() {
        Config c = Config.get();
        if (!c.totemBackground) {
            return Component.literal("Totem Background: Off");
        }
        String name = c.totemCustom ? "Custom" : c.totemColor.label();
        return Component.literal("Totem Background: ").append(Component.literal(name).withColor(c.totemRgb()));
    }

    /** Off -> Red -> Light Blue -> Dark Blue -> Green -> Purple -> Off. Custom goes to Off. */
    private static void nextTotem() {
        Config c = Config.get();
        if (!c.totemBackground) {
            c.totemBackground = true;
            c.totemCustom = false;
            c.totemColor = SlotColor.values()[0];
        } else if (c.totemCustom || c.totemColor.ordinal() == SlotColor.values().length - 1) {
            c.totemBackground = false;
            c.totemCustom = false;
        } else {
            c.totemColor = c.totemColor.next();
        }
    }

    private static Component breachLabel() {
        Config c = Config.get();
        return c.breachCustom ? customLabel("Breach", c.breachCustomRgb) : colorLabel("Breach", c.breachColor);
    }

    private static Component densityLabel() {
        Config c = Config.get();
        return c.densityCustom ? customLabel("Density", c.densityCustomRgb) : colorLabel("Density", c.densityColor);
    }

    /** "Breach: Custom", with "Custom" shown in the picked color. */
    private static Component customLabel(String name, int rgb) {
        return Component.literal(name + ": ").append(Component.literal("Custom").withColor(rgb & 0xFFFFFF));
    }

    private static final net.minecraft.resources.Identifier TOTEM_TEXTURE =
            net.minecraft.resources.Identifier.withDefaultNamespace("textures/item/totem_of_undying.png");
    private static final net.minecraft.resources.Identifier MACE_TEXTURE =
            net.minecraft.resources.Identifier.withDefaultNamespace("textures/item/mace.png");

    /**
     * The color picker's preview: the item in an inventory slot with the picked background, at the
     * picked opacity, so it looks like it will in game.
     */
    private static ColorPickerScreen.Preview slotPreview(net.minecraft.resources.Identifier texture) {
        return (graphics, x, y, size, rgb, opacity) -> {
            // The world, with a real hotbar slot in front of it, drawn bigger.
            PreviewScene.world(graphics, x, y, size, size);
            int scale = Math.max(1, (size - 6) / 22);
            int slot = 22 * scale;
            PreviewScene.hotbarSlot(graphics, x + (size - slot) / 2, y + (size - slot) / 2, scale,
                    Config.withOpacity(rgb, opacity), () -> graphics.blit(
                            net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture,
                            0, 0, 0.0F, 0.0F, 16, 16, 16, 16, 16, 16));
        };
    }

    /** True when a right-click lands on {@code button} inside the scrolling area. */
    private boolean rightClicked(MouseButtonEvent event, Button button) {
        return event.button() == 1 && button != null && button.visible && inList(event.y())
                && button.isMouseOver(event.x(), event.y());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        Config c = Config.get();
        if (rightClicked(event, totemButton)) {
            Minecraft.getInstance().gui.setScreen(new ColorPickerScreen(this, "Totem Background",
                    c.totemRgb(), c.totemOpacity, (rgb, opacity) -> {
                        c.totemCustomRgb = rgb;
                        c.totemOpacity = opacity;
                        c.totemCustom = true;
                        c.totemBackground = true;
                    }, slotPreview(TOTEM_TEXTURE)));
            return true;
        }
        if (rightClicked(event, breachButton)) {
            Minecraft.getInstance().gui.setScreen(new ColorPickerScreen(this, "Breach Mace Color",
                    c.breachCustom ? c.breachCustomRgb : c.breachColor.rgb(), c.breachOpacity,
                    (rgb, opacity) -> {
                        c.breachCustomRgb = rgb;
                        c.breachOpacity = opacity;
                        c.breachCustom = true;
                    }, slotPreview(MACE_TEXTURE)));
            return true;
        }
        if (rightClicked(event, densityButton)) {
            Minecraft.getInstance().gui.setScreen(new ColorPickerScreen(this, "Density Mace Color",
                    c.densityCustom ? c.densityCustomRgb : c.densityColor.rgb(), c.densityOpacity,
                    (rgb, opacity) -> {
                        c.densityCustomRgb = rgb;
                        c.densityOpacity = opacity;
                        c.densityCustom = true;
                    }, slotPreview(MACE_TEXTURE)));
            return true;
        }
        // A setting partly scrolled behind the title or the Done button can't be clicked there.
        if (!inList(event.y())) {
            for (AbstractWidget w : scrolled) {
                if (w.visible && w.isMouseOver(event.x(), event.y())) {
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.centeredText(this.font, this.title, this.width / 2, 15, WHITE);

        // The scrolling settings, cut off at the title and the Done button.
        int offset = (int) scroll;
        int mx = inList(mouseY) ? mouseX : -1;
        int my = inList(mouseY) ? mouseY : -1;
        graphics.enableScissor(0, LIST_TOP, this.width, listBottom());
        section(graphics, "Visuals", VISUALS_TOP - offset);
        section(graphics, "Mace Slots", MACE_SLOTS_TOP - offset);
        section(graphics, "Crosshair", CROSSHAIR_TOP - offset);
        section(graphics, "Game Optimization", OPTIMIZATION_TOP - offset);
        for (AbstractWidget w : scrolled) {
            w.extractRenderState(graphics, mx, my, delta);
        }
        graphics.disableScissor();

        // Scroll bar, only when everything doesn't fit.
        if (maxScroll() > 0) {
            int x = this.width / 2 + SECTION_WIDTH / 2 + 6;
            int track = listBottom() - LIST_TOP;
            int thumb = Math.max(12, track * track / (track + maxScroll()));
            int y = LIST_TOP + (int) ((track - thumb) * (scroll / maxScroll()));
            graphics.fill(x, LIST_TOP, x + 4, listBottom(), 0x80000000);
            graphics.fill(x, y, x + 4, y + thumb, 0xFFA0A0A0);
        }
    }

    private static Component onOff(String name, boolean on) {
        return Component.literal(name + ": " + (on ? "On" : "Off"));
    }

    /** "Breach: Light Blue", with the color name shown in its color. */
    private static Component colorLabel(String name, SlotColor color) {
        return Component.literal(name + ": ").append(Component.literal(color.label()).withColor(color.rgb()));
    }

    /** A section heading: the name centered, with a line on each side. */
    private void section(GuiGraphicsExtractor graphics, String name, int y) {
        int centre = this.width / 2;
        int half = this.font.width(name) / 2 + 6;
        int left = centre - SECTION_WIDTH / 2;
        int right = centre + SECTION_WIDTH / 2;
        graphics.fill(left, y + 4, centre - half, y + 5, LINE);
        graphics.fill(centre + half, y + 4, right, y + 5, LINE);
        graphics.centeredText(this.font, name, centre, y, SECTION);
    }

    @Override
    public void onClose() {
        Config.save();
        Minecraft.getInstance().options.save();
        Minecraft.getInstance().gui.setScreen(parent);
    }
}
