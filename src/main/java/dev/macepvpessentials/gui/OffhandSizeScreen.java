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
 * Offhand & Totem Pop: separate sizes for a shield and a totem held in your offhand (first
 * person) and for the totem pop animation, with a preview of each (the pop plays on a loop). The preview draws Minecraft's own textures, so it works from the
 * title screen too.
 */
public final class OffhandSizeScreen extends Screen {

    private static final int BOX_W = 110;
    private static final int BOX_H = 96;
    private static final int GAP = 8;
    private static final int TOP = 30;
    private static final int SLIDER_W = 110;

    private static final Identifier TOTEM = Identifier.withDefaultNamespace("textures/item/totem_of_undying.png");
    /** The shield's front face is 12x22 pixels at (1, 1) in this 64x64 texture. */
    private static final Identifier SHIELD = Identifier.withDefaultNamespace("textures/entity/shield/shield_base_nopattern.png");

    private final Screen parent;

    public OffhandSizeScreen(Screen parent) {
        super(Component.literal("Offhand & Totem Pop"));
        this.parent = parent;
    }

    private int left() {
        return this.width / 2 - (3 * BOX_W + 2 * GAP) / 2;
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
        this.addRenderableWidget(new IntSlider(x + 2 * (BOX_W + GAP), y, SLIDER_W, 20, "Pop", 10, 150, c.totemPopSize,
                v -> v + "%", v -> c.totemPopSize = v));
        // A little play button in the corner of the Totem Pop preview.
        Button play = Button.builder(Component.literal("\u25B6"), b -> playPop())
                .bounds(x + 2 * (BOX_W + GAP) + BOX_W - 18, TOP + 2, 16, 16).build();
        play.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Play the totem pop.")));
        this.addRenderableWidget(play);
        this.addRenderableWidget(Button.builder(Component.literal("Reset"), b -> {
            c.offhandShieldSize = 100;
            c.offhandTotemSize = 100;
            c.totemPopSize = 100;
            Config.save();
            this.rebuildWidgets();
        }).bounds(this.width / 2 - 102, this.height - 26, 100, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
                .bounds(this.width / 2 + 2, this.height - 26, 100, 18).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // Previews first, so the play button is drawn on top of its box.
        Config c = Config.get();
        int x = left();
        drawBox(graphics, x, "Shield", c.offhandShieldSize, true);
        drawBox(graphics, x + BOX_W + GAP, "Totem", c.offhandTotemSize, false);
        drawPop(graphics, x + 2 * (BOX_W + GAP), c.totemPopSize);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width / 2, 12, -1);
        graphics.centeredText(this.font, "The faint copy shows the normal size.", this.width / 2,
                TOP + BOX_H + 18 + 26, 0xFF808080);
    }

    /**
     * A little first-person view: the world, Minecraft's crosshair in the middle, and the item in
     * the bottom-left corner like your offhand. A faint copy shows the normal (100%) size.
     */
    private void drawBox(GuiGraphicsExtractor graphics, int x, String name, int size, boolean shield) {
        int y = TOP;
        PreviewScene.world(graphics, x, y, BOX_W, BOX_H);
        graphics.enableScissor(x, y, x + BOX_W, y + BOX_H);
        PreviewScene.vanillaCrosshair(graphics, x + BOX_W / 2, y + BOX_H / 2);
        if (size != 100) {
            drawItem(graphics, x, y, 1.0F, shield, 0x50FFFFFF);
        }
        drawItem(graphics, x, y, size / 100.0F, shield, 0xFFFFFFFF);
        graphics.disableScissor();
        graphics.centeredText(this.font, name, x + BOX_W / 2, y + BOX_H + 5, 0xFFA0A0A0);
    }

    /** The item anchored at the hand (bottom-left), so it shrinks and grows toward it like in game. */
    private static void drawItem(GuiGraphicsExtractor graphics, int x, int y, float scale, boolean shield, int color) {
        if (shield) {
            // 100%: 36 x 66 (3x the 12 x 22 face), partly below the bottom edge like in game.
            int w = Math.round(36 * scale);
            int h = Math.round(66 * scale);
            int left = x + Math.round(14 * scale);
            int top = y + BOX_H - h + Math.round(14 * scale);
            graphics.blit(RenderPipelines.GUI_TEXTURED, SHIELD, left, top, 1.0F, 1.0F, w, h, 12, 22, 64, 64, color);
        } else {
            int s = Math.round(56 * scale);
            int left = x + Math.round(10 * scale);
            int top = y + BOX_H - s + Math.round(6 * scale);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TOTEM, left, top, 0.0F, 0.0F, s, s, 16, 16, 16, 16, color);
        }
    }

    /** Pop playback: start time (-1 = not playing), random drift, and the particles. */
    private long popStart = -1;
    private int popTicksDone;
    private float popOffX;
    private float popOffY;
    private final java.util.List<float[]> particles = new java.util.ArrayList<>();
    private final java.util.Random random = new java.util.Random();

    private static final Identifier[] GLITTER = new Identifier[8];
    static {
        for (int i = 0; i < 8; i++) {
            GLITTER[i] = Identifier.withDefaultNamespace("textures/particle/glitter_" + i + ".png");
        }
    }

    /** Starts one pop in the preview, with the real totem sound. */
    private void playPop() {
        popStart = System.currentTimeMillis();
        popTicksDone = 0;
        popOffX = random.nextFloat() * 2.0F - 1.0F;
        popOffY = random.nextFloat() * 2.0F - 1.0F;
        particles.clear();
        Minecraft.getInstance().getSoundManager().play(
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.TOTEM_USE, 1.0F));
    }

    /**
     * One game tick of the pop's particles, like Minecraft's: for 30 ticks a burst of green and
     * yellow sparkles comes off you; they slow down fast, then drift down and fade (60-72 ticks).
     * Each particle: x, y, dx, dy, age, lifetime, r, g, b.
     */
    private void tickParticles(int x, int y) {
        if (popTicksDone < 30) {
            for (int i = 0; i < 16; i++) {
                // All around you, so they fill the whole view (more toward the middle).
                float px = x + BOX_W * (0.5F + (random.nextFloat() - random.nextFloat()) * 0.6F);
                float py = y + BOX_H * (0.55F + (random.nextFloat() - random.nextFloat()) * 0.55F);
                float dx = (random.nextFloat() * 2.0F - 1.0F) * 10.0F;
                float dy = (random.nextFloat() * 2.0F - 1.0F) * 10.0F - 3.0F;
                float r, g, b;
                if (random.nextInt(4) == 0) {
                    r = 0.6F + random.nextFloat() * 0.2F; g = 0.6F + random.nextFloat() * 0.3F; b = random.nextFloat() * 0.2F;
                } else {
                    r = 0.1F + random.nextFloat() * 0.2F; g = 0.4F + random.nextFloat() * 0.3F; b = random.nextFloat() * 0.2F;
                }
                particles.add(new float[]{px, py, dx, dy, 0, 60 + random.nextInt(12), r, g, b});
            }
        }
        java.util.Iterator<float[]> it = particles.iterator();
        while (it.hasNext()) {
            float[] p = it.next();
            p[4]++;
            if (p[4] >= p[5]) {
                it.remove();
                continue;
            }
            p[3] += 0.12F;
            p[0] += p[2];
            p[1] += p[3];
            p[2] *= 0.6F;
            p[3] *= 0.6F;
        }
        popTicksDone++;
    }

    /**
     * The totem pop. Same math as Minecraft's: the totem flies toward the screen, spins and drifts
     * off to a random side, then shrinks away, with the sparkles and the totem sound.
     * Press the play button to play it. The box is a small screen.
     */
    private void drawPop(GuiGraphicsExtractor graphics, int x, int sizePercent) {
        int y = TOP;
        PreviewScene.world(graphics, x, y, BOX_W, BOX_H);
        graphics.enableScissor(x, y, x + BOX_W, y + BOX_H);
        PreviewScene.vanillaCrosshair(graphics, x + BOX_W / 2, y + BOX_H / 2);

        if (popStart < 0) {
            graphics.disableScissor();
            graphics.centeredText(this.font, "Press \u25B6", x + BOX_W / 2, y + BOX_H / 2 + 14, 0xFFFFFFFF);
            graphics.centeredText(this.font, "Totem Pop", x + BOX_W / 2, y + BOX_H + 5, 0xFFA0A0A0);
            return;
        }
        float ticks = (System.currentTimeMillis() - popStart) / 50.0F;
        while (popTicksDone < (int) ticks) {
            tickParticles(x, y);
        }

        // Sparkles behind the totem, like in game.
        for (float[] p : particles) {
            int frame = Math.min(7, (int) (p[4] * 8 / p[5]));
            // Fully visible for the first half of its life, then fades out (like the game).
            float life = p[4] / p[5];
            int alpha = life < 0.5F ? 255 : (int) (255 * Math.max(0.0F, 1.0F - (life - 0.5F) * 2.0F));
            int color = alpha << 24 | (int) (p[6] * 255) << 16 | (int) (p[7] * 255) << 8 | (int) (p[8] * 255);
            int size = Math.max(4, BOX_H / 12);
            graphics.blit(RenderPipelines.GUI_TEXTURED, GLITTER[7 - frame], (int) p[0] - size / 2, (int) p[1] - size / 2,
                    0.0F, 0.0F, size, size, 8, 8, 8, 8, color);
        }

        if (ticks < 40.0F) {
            float t = ticks / 40.0F;
            float ts = t * t;
            float tc = t * ts;
            float smooth = 10.25F * tc * ts - 24.95F * ts * ts + 25.5F * tc - 13.8F * ts + 4.0F * t;
            float pi = smooth * (float) Math.PI;
            // Distance from the camera (-10 + 9 sin), 70 degree view: tan(35) ~ 0.7.
            float depth = Math.max(0.5F, 10.0F - 9.0F * (float) Math.sin(pi));
            float drift = Math.abs((float) Math.sin(pi * 2.0F));
            float ndcX = popOffX * 0.3F * drift / (depth * 0.7F);
            float ndcY = popOffY * 0.3F * drift / (depth * 0.7F);
            float px = x + BOX_W / 2.0F + ndcX * BOX_W / 2.0F;
            float py = y + BOX_H / 2.0F - ndcY * BOX_H / 2.0F;
            float sizePx = 0.8F * (sizePercent / 100.0F) / (depth * 0.7F) * BOX_H / 2.0F;
            // The spin: 900 degrees around the up axis shows as the totem getting thin and flipping.
            float spin = (float) Math.toRadians(900.0F * Math.abs(Math.sin(pi)));
            float tilt = (float) Math.toRadians(6.0F * Math.cos(t * 8.0F));
            graphics.pose().pushMatrix();
            graphics.pose().translate(px, py);
            graphics.pose().rotate(tilt);
            // Never a negative width: a flipped picture isn't drawn at all, which hid the totem
            // exactly when it was biggest. A little minimum width stands in for its 3D thickness.
            float width = Math.max(0.12F, Math.abs((float) Math.cos(spin)));
            graphics.pose().scale(sizePx / 16.0F * width, sizePx / 16.0F);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TOTEM, -8, -8, 0.0F, 0.0F, 16, 16, 16, 16, 16, 16);
            graphics.pose().popMatrix();
        } else if (particles.isEmpty()) {
            popStart = -1;
        }
        graphics.disableScissor();
        graphics.centeredText(this.font, "Totem Pop", x + BOX_W / 2, y + BOX_H + 5, 0xFFA0A0A0);
    }

    @Override
    public void onClose() {
        Config.save();
        Minecraft.getInstance().gui.setScreen(this.parent);
    }
}
