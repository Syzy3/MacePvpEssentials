package dev.macepvpessentials.crosshair;

public final class CrosshairSettings {
    public boolean custom = false;
    public CrosshairStyle style;
    public CrosshairColor color;
    public int customRgb;
    public int opacity;
    public int length;
    public int thickness;
    public int gap;
    public boolean centerDot;
    public boolean outline;
    public boolean targetColor;
    public boolean showCooldown;
    public boolean showElytra;

    public CrosshairSettings() {
        this.style = CrosshairStyle.CROSS;
        this.color = CrosshairColor.WHITE;
        this.customRgb = 16777215;
        this.opacity = 100;
        this.length = 4;
        this.thickness = 2;
        this.gap = 2;
        this.centerDot = false;
        this.outline = true;
        this.targetColor = false;
        this.showCooldown = true;
        this.showElytra = true;
    }

    public int rgb() {
        return this.color == CrosshairColor.CUSTOM ? this.customRgb & 16777215 : this.color.rgb();
    }

    public void fix() {
        if (this.style == null) {
            this.style = CrosshairStyle.CROSS;
        }

        if (this.color == null) {
            this.color = CrosshairColor.WHITE;
        }

        this.opacity = clamp(this.opacity, 10, 100);
        this.length = clamp(this.length, 1, 12);
        this.thickness = clamp(this.thickness, 1, 6);
        this.gap = clamp(this.gap, 0, 10);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
