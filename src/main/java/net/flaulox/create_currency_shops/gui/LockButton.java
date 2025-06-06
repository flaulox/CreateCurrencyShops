package net.flaulox.create_currency_shops.gui;

import net.flaulox.create_currency_shops.CreateCurrencyShopsGuiTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class LockButton extends AbstractWidget {
    private boolean state;
    private Runnable callback;
    private final CreateCurrencyShopsGuiTextures lockedTexture = CreateCurrencyShopsGuiTextures.LOCK_BUTTON_LOCKED;
    private final CreateCurrencyShopsGuiTextures unlockedTexture = CreateCurrencyShopsGuiTextures.LOCK_BUTTON_UNLOCKED;

    public LockButton(int x, int y, boolean initialState) {
        super(x, y, CreateCurrencyShopsGuiTextures.LOCK_BUTTON_LOCKED.getWidth(), CreateCurrencyShopsGuiTextures.LOCK_BUTTON_LOCKED.getHeight(), Component.empty());
        this.state = initialState;
    }

    public LockButton withCallback(Runnable callback) {
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
        CreateCurrencyShopsGuiTextures texture = state ? lockedTexture : unlockedTexture;
        texture.render(graphics, getX(), getY());
        
        if (isHovered) {
            graphics.renderComponentTooltip(Minecraft.getInstance().font,
                java.util.List.of(
                    Component.translatable(state ? "gui.create_currency_shops.coin_safe_locked" : "gui.create_currency_shops.coin_safe_open"),
                    Component.translatable("gui.create_currency_shops.coin_safe_lock_tip").withStyle(ChatFormatting.GRAY),
                    Component.translatable("gui.create_currency_shops.coin_safe_lock_tip_1").withStyle(ChatFormatting.GRAY),
                    Component.translatable("gui.create_currency_shops.coin_safe_lock_tip_2").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC)
                ),
                mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE,
            Component.translatable(state ? "gui.create_currency_shops.coin_safe_locked" : "gui.create_currency_shops.coin_safe_open"));
    }
}
