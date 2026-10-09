package dev.macepvpessentials.crosshair;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class CrosshairRenderer {
    private static final Identifier ELYTRA = Identifier.withDefaultNamespace("textures/item/elytra.png");

    private CrosshairRenderer() {
    }

    public static void draw(GuiGraphicsExtractor g, CrosshairSettings s, int cx, int cy, int rgb, float cooldown, boolean elytra) {
        int alpha = Math.round((float)s.opacity / 100.0F * 255.0F) << 24;
        int color = alpha | rgb;
        int outline = alpha;
        float shift = (float)(s.thickness % 2) * 0.25F;
        g.pose().pushMatrix();
        g.pose().translate((float)cx + 0.5F - shift, (float)cy + 0.5F - shift);
        g.pose().scale(0.5F, 0.5F);
        if (s.style == CrosshairStyle.CIRCLE) {
            ring(g, s, color, alpha);
        }

        List<int[]> rects = shape(s);
        if (s.outline) {
            for (int[] r : rects) {
                g.fill(r[0] - 1, r[1] - 1, r[2] + 1, r[3] + 1, outline);
            }
        }

        for (int[] r : rects) {
            g.fill(r[0], r[1], r[2], r[3], color);
        }

        g.pose().popMatrix();
        drawExtras(g, s, cx, cy, reach(s), rgb, cooldown, elytra);
    }

    public static void drawExtras(GuiGraphicsExtractor g, CrosshairSettings s, int cx, int cy, int reach, int rgb, float cooldown, boolean elytra) {
        if (s.showCooldown && cooldown >= 0.0F && cooldown < 1.0F) {
            int width = 12;
            int x0 = cx - width / 2 + 1;
            int y0 = cy + reach + 3;
            g.fill(x0 - 1, y0 - 1, x0 + width + 1, y0 + 2, -1879048192);
            g.fill(x0, y0, x0 + Math.round((float)width * cooldown), y0 + 1, -16777216 | rgb);
        }

        if (s.showElytra && elytra) {
            g.pose().pushMatrix();
            g.pose().translate((float)(cx + reach + 3), (float)cy - 2.5F);
            g.pose().scale(0.375F, 0.375F);
            g.blit(RenderPipelines.GUI_TEXTURED, ELYTRA, 0, 0, 0.0F, 0.0F, 16, 16, 16, 16, 16, 16);
            g.pose().popMatrix();
        }

    }

    private static List<int[]> shape(CrosshairSettings s) {
        List<int[]> rects = new ArrayList();
        int t = s.thickness;
        int x = -t / 2;
        int y = -t / 2;
        int len = s.length;
        int gap = s.gap;
        switch (s.style) {
            case CROSS, T_SHAPE -> {
                rects.add(new int[]{x - gap - len, y, x - gap, y + t});
                rects.add(new int[]{x + t + gap, y, x + t + gap + len, y + t});
                rects.add(new int[]{x, y + t + gap, x + t, y + t + gap + len});
                if (s.style == CrosshairStyle.CROSS) {
                    rects.add(new int[]{x, y - gap - len, x + t, y - gap});
                }
            }
            case DOT -> {
                int size = dotSize(s);
                int x0 = -size / 2;
                rects.add(new int[]{x0, x0, x0 + size, x0 + size});
            }
            case SQUARE -> {
                int half = gap + len;
                int x0 = x - half;
                int y0 = y - half;
                int x1 = x + t + half;
                int y1 = y + t + half;
                rects.add(new int[]{x0, y0, x1, y0 + t});
                rects.add(new int[]{x0, y1 - t, x1, y1});
                rects.add(new int[]{x0, y0 + t, x0 + t, y1 - t});
                rects.add(new int[]{x1 - t, y0 + t, x1, y1 - t});
            }
            case CIRCLE -> {
                // The ring is drawn separately.
            }
        }

        if (s.centerDot && s.style != CrosshairStyle.DOT) {
            rects.add(new int[]{x, y, x + t, y + t});
        }

        return rects;
    }

    private static int dotSize(CrosshairSettings s) {
        return Math.max(s.thickness, Math.min(s.length, 8));
    }

    public static int reach(CrosshairSettings s) {
        int edge = s.outline ? 1 : 0;
        int halfUnits = switch (s.style) {
            case CROSS, T_SHAPE, SQUARE -> (s.thickness + 1) / 2 + s.gap + s.length + edge;
            case DOT -> dotSize(s) / 2 + 1 + edge;
            case CIRCLE -> s.gap + s.length + 1 + edge;
        };
        return (halfUnits + 1) / 2;
    }

    private static void ring(GuiGraphicsExtractor g, CrosshairSettings s, int color, int outline) {
        float outer = (float)(s.gap + s.length) + 0.5F;
        float inner = outer - (float)s.thickness;
        int bound = (int)Math.ceil((double)outer) + 1;

        for (int pass = s.outline ? 0 : 1; pass < 2; ++pass) {
            float lo = pass == 0 ? inner - 1.0F : inner;
            float hi = pass == 0 ? outer + 1.0F : outer;
            int c = pass == 0 ? outline : color;

            for (int dy = -bound; dy <= bound; ++dy) {
                for (int dx = -bound; dx <= bound; ++dx) {
                    float px = (float)dx + 0.5F - (float)(s.thickness % 2) * 0.5F;
                    float py = (float)dy + 0.5F - (float)(s.thickness % 2) * 0.5F;
                    float d = (float)Math.sqrt((double)(px * px + py * py));
                    if (d > lo && d <= hi) {
                        g.fill(dx, dy, dx + 1, dy + 1, c);
                    }
                }
            }
        }

    }
}
