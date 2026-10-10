package dev.macepvpessentials.gui;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;

/** Minecraft's yes/no confirm screen, but with the cancel button on the left (like the rest of the mod). */
final class CancelLeftConfirmScreen extends ConfirmScreen {

    CancelLeftConfirmScreen(BooleanConsumer callback, Component title, Component message, Component yes, Component cancel) {
        super(callback, title, message, yes, cancel);
    }

    @Override
    protected void addButtons(LinearLayout buttonLayout) {
        this.noButton = buttonLayout.addChild(
                Button.builder(this.noButtonComponent, b -> this.callback.accept(false)).build());
        this.yesButton = buttonLayout.addChild(
                Button.builder(this.yesButtonComponent, b -> this.callback.accept(true)).build());
    }
}
