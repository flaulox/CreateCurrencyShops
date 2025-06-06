package net.flaulox.create_currency_shops.gui;

import net.flaulox.create_currency_shops.CreateCurrencyShopsGuiTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ToggleButton extends AbstractWidget {
    private boolean state;
    private Runnable callback;
    private final CreateCurrencyShopsGuiTextures onTexture = CreateCurrencyShopsGuiTextures.TOGGLE_BUTTON_ON;
    private final CreateCurrencyShopsGuiTextures offTexture = CreateCurrencyShopsGuiTextures.TOGGLE_BUTTON_OFF;

    public ToggleButton(int x, int y, boolean initialState) {
        super(x, y, CreateCurrencyShopsGuiTextures.TOGGLE_BUTTON_ON.getWidth(), CreateCurrencyShopsGuiTextures.TOGGLE_BUTTON_ON.getHeight(), Component.empty());
        this.state = initialState;
    }

    public ToggleButton withCallback(Runnable callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        state = !state;
        if (callback != null) {
            callback.run();
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        CreateCurrencyShopsGuiTextures texture = state ? onTexture : offTexture;
        texture.render(graphics, getX(), getY());
        
        if (isHovered) {
            String key = state ? "gui.create_currency_shops.wallet.use_cash_first" : "gui.create_currency_shops.wallet.use_credit_card_first";
            graphics.renderTooltip(Minecraft.getInstance().font,
                Component.translatable(key), mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        String key = state ? "gui.create_currency_shops.wallet.use_cash_first" : "gui.create_currency_shops.wallet.use_credit_card_first";
        output.add(NarratedElementType.TITLE, Component.translatable(key));
    }
}
