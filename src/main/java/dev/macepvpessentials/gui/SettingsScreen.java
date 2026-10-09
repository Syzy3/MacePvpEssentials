package dev.macepvpessentials.gui;

import dev.macepvpessentials.Hitboxes;
import dev.macepvpessentials.config.Config;
import dev.macepvpessentials.config.HitboxMode;
import dev.macepvpessentials.config.SlotColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
    private static final int MACE_SLOTS_TOP = 112;
    private static final int CROSSHAIR_TOP = 160;
    private static final int HALF_WIDTH = 122;

    private final Screen parent;

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
        if (hitboxes == null) {
            hitboxes = Hitboxes.current();
        }

        // Visuals
        Button hitboxButton = Button.builder(Component.literal("Hitboxes: " + hitboxes.label()), b -> {
            hitboxes = hitboxes.next();
            Hitboxes.set(hitboxes);
            b.setMessage(Component.literal("Hitboxes: " + hitboxes.label()));
        }).bounds(this.width / 2 - BUTTON_WIDTH / 2, VISUALS_TOP + 16, BUTTON_WIDTH, 20).build();
        hitboxButton.setTooltip(Tooltip.create(Component.literal("Shows the hitboxes of players and mobs.")));
        addRenderableWidget(hitboxButton);

        // Mace Slots: two buttons side by side.
        int pairLeft = this.width / 2 - HALF_WIDTH - 2;
        Button breach = Button.builder(colorLabel("Breach", Config.get().breachColor), b -> {
            Config.get().breachColor = Config.get().breachColor.next();
            Config.save();
            b.setMessage(colorLabel("Breach", Config.get().breachColor));
        }).bounds(pairLeft, MACE_SLOTS_TOP + 16, HALF_WIDTH, 20).build();
        breach.setTooltip(Tooltip.create(Component.literal("Colors the slot of a Breach mace.")));
        addRenderableWidget(breach);

        Button density = Button.builder(colorLabel("Density", Config.get().densityColor), b -> {
            Config.get().densityColor = Config.get().densityColor.next();
            Config.save();
            b.setMessage(colorLabel("Density", Config.get().densityColor));
        }).bounds(pairLeft + HALF_WIDTH + 4, MACE_SLOTS_TOP + 16, HALF_WIDTH, 20).build();
        density.setTooltip(Tooltip.create(Component.literal("Colors the slot of a Density mace.")));
        addRenderableWidget(density);

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
        defaultCrosshair.setTooltip(Tooltip.create(Component.literal("Uses Minecraft's normal crosshair. Turn off to make your own.")));
        addRenderableWidget(defaultCrosshair);
        addRenderableWidget(edit);

        // Visuals: Shield Status, under Hitboxes
        Button shield = Button.builder(onOff("Shield Status", Config.get().shieldStatus), b -> {
            Config.get().shieldStatus = !Config.get().shieldStatus;
            Config.save();
            b.setMessage(onOff("Shield Status", Config.get().shieldStatus));
        }).bounds(this.width / 2 - BUTTON_WIDTH / 2, VISUALS_TOP + 40, BUTTON_WIDTH, 20).build();
        shield.setTooltip(Tooltip.create(Component.literal("Shields are green when ready and red when disabled.")));
        addRenderableWidget(shield);

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.centeredText(this.font, this.title, this.width / 2, 15, WHITE);

        section(graphics, "Visuals", VISUALS_TOP);
        section(graphics, "Mace Slots", MACE_SLOTS_TOP);
        section(graphics, "Crosshair", CROSSHAIR_TOP);
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
        Minecraft.getInstance().gui.setScreen(parent);
    }
}
