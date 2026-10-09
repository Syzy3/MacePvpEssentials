package dev.macepvpessentials.crosshair;

public enum CrosshairColor {
    WHITE("White", 16777215),
    RED("Red", 16726843),
    GREEN("Green", 4513092),
    LIGHT_BLUE("Light Blue", 5623039),
    DARK_BLUE("Dark Blue", 2837472),
    YELLOW("Yellow", 16769088),
    PURPLE("Purple", 10502399),
    PINK("Pink", 16740552),
    BLACK("Black", 1052688),
    CUSTOM("Custom", 16777215);

    private final String label;
    private final int rgb;

    private CrosshairColor(String label, int rgb) {
        this.label = label;
        this.rgb = rgb;
    }

    public String label() {
        return this.label;
    }

    public int rgb() {
        return this.rgb;
    }

    public CrosshairColor next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
