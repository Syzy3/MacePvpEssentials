package dev.macepvpessentials.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/** A normal Minecraft slider over whole numbers, e.g. "Length: 4". */
final class IntSlider extends AbstractSliderButton {

    private final String name;
    private final int min;
    private final int max;
    private final IntConsumer onChange;
    private final IntFunction<String> format;

    IntSlider(int x, int y, int width, int height, String name, int min, int max, int current,
              IntFunction<String> format, IntConsumer onChange) {
        super(x, y, width, height, Component.empty(), (current - min) / (double) (max - min));
        this.name = name;
        this.min = min;
        this.max = max;
        this.format = format;
        this.onChange = onChange;
        updateMessage();
    }

    private int current() {
        return min + (int) Math.round(value * (max - min));
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.literal(name + ": " + format.apply(current())));
    }

    @Override
    protected void applyValue() {
        onChange.accept(current());
    }
}
