package dev.macepvpessentials.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Shared backgrounds for the preview boxes, drawn from Minecraft's own textures so a preview
 * looks like the game: a sky, a grassy field going off into the distance and a tree, plus the
 * real hotbar slot. Only textures are used, so previews work from the title screen too.
 */
final class PreviewScene {

    private static final Identifier GRASS_SIDE = block("grass_block_side");
    private static final Identifier GRASS_TOP = block("grass_block_top");
    private static final Identifier OAK_LOG = block("oak_log");
    private static final Identifier OAK_LEAVES = block("oak_leaves");
    private static final Identifier HOTBAR = Identifier.withDefaultNamespace("hud/hotbar");
    private static final Identifier CROSSHAIR = Identifier.withDefaultNamespace("hud/crosshair");

    /** Plains biome grass and leaves colors (the textures are grey and get tinted in game). */
    private static final int GRASS_TINT = 0xFF91BD59;
    private static final int LEAVES_TINT = 0xFF77AB2F;

    private PreviewScene() {
    }

    private static Identifier block(String name) {
        return Identifier.withDefaultNamespace("textures/block/" + name + ".png");
    }

    /** The world behind a preview: sky, a field with rows getting bigger as they come closer, a tree. */
    static void world(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
        g.enableScissor(x, y, x + w, y + h);
        int horizon = y + h * 56 / 100;
        g.fillGradient(x, y, x + w, horizon, 0xFF7BA4FF, 0xFFC0D8FF);

        // A tree on the left, standing on the far edge of the field.
        int tree = Math.max(6, w / 12);
        int trunkX = x + w / 6;
        for (int i = 1; i <= 2; i++) {
            tile(g, OAK_LOG, trunkX, horizon - i * tree, tree, 0xFFFFFFFF);
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 3; dy <= 4; dy++) {
                if (dy == 4 && dx != 0) {
                    continue;
                }
                tile(g, OAK_LEAVES, trunkX + dx * tree, horizon - dy * tree, tree, LEAVES_TINT);
            }
        }

        // The field: a thin row of grass blocks at the horizon, then grass tops that get bigger.
        int size = Math.max(4, w / 16);
        drawRow(g, GRASS_SIDE, x, w, horizon, size, 0xFFFFFFFF);
        int rowY = horizon + size;
        float grow = 1.45F;
        while (rowY < y + h) {
            size = Math.round(size * grow);
            drawRow(g, GRASS_TOP, x, w, rowY, size, GRASS_TINT);
            rowY += size;
        }
        // Distance haze along the horizon.
        g.fillGradient(x, horizon - 6, x + w, horizon + 10, 0x00C0D8FF, 0x55C0D8FF);
        g.disableScissor();
    }

    private static void drawRow(GuiGraphicsExtractor g, Identifier texture, int x, int w, int y, int size, int tint) {
        // Center the row so blocks line up around the middle like a real view.
        int start = x + w / 2 - size / 2;
        while (start > x) {
            start -= size;
        }
        for (int bx = start; bx < x + w; bx += size) {
            tile(g, texture, bx, y, size, tint);
        }
    }

    private static void tile(GuiGraphicsExtractor g, Identifier texture, int x, int y, int size, int tint) {
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, size, size, 16, 16, 16, 16, tint);
    }

    /** Minecraft's crosshair at (cx, cy), real size. */
    static void vanillaCrosshair(GuiGraphicsExtractor g, int cx, int cy) {
        g.blitSprite(RenderPipelines.CROSSHAIR, CROSSHAIR, cx - 7, cy - 7, 15, 15);
    }

    /**
     * One real hotbar slot (22x22) at (x, y), {@code scale} times bigger, with the slot's
     * background color and the item drawn by {@code item} at the slot's 16x16 item spot.
     */
    static void hotbarSlot(GuiGraphicsExtractor g, int x, int y, int scale, int fill, Runnable item) {
        g.enableScissor(x, y, x + 22 * scale, y + 22 * scale);
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR, 0, 0, 182, 22);
        if (fill != 0) {
            g.fill(3, 3, 19, 19, fill);
        }
        g.pose().translate(3, 3);
        item.run();
        g.pose().popMatrix();
        g.disableScissor();
    }
}
