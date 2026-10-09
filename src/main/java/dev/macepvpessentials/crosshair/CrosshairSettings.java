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
    /** The color used while aiming at a player (when Target is on). */
    public CrosshairColor target;
    /** The target color picked with right-click (when target is CUSTOM). */
    public int targetCustomRgb;
    /** Also show the crosshair in third person (F5). */
    public boolean thirdPerson;
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
        this.target = CrosshairColor.RED;
        this.targetCustomRgb = 0xFF3B3B;
        this.thirdPerson = false;
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

        if (this.target == null) {
            this.target = CrosshairColor.RED;
        }

        this.opacity = clamp(this.opacity, 10, 100);
        this.length = clamp(this.length, 1, 12);
        this.thickness = clamp(this.thickness, 1, 6);
        this.gap = clamp(this.gap, 0, 10);
    }

    /** The target color, including a custom one. */
    public int targetRgb() {
        return this.target == CrosshairColor.CUSTOM ? this.targetCustomRgb & 0xFFFFFF : this.target.rgb();
    }

    /** The crosshair color to use right now: the target color while aiming at a player. */
    public int currentRgb(boolean onPlayer) {
        int rgb = this.rgb();
        if (this.targetColor && onPlayer) {
            int t = this.targetRgb();
            // Same as the normal color would show no change, so fall back to white (or red).
            return t != rgb ? t : (t == 0xFFFFFF ? CrosshairColor.RED.rgb() : 0xFFFFFF);
        }
        return rgb;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
