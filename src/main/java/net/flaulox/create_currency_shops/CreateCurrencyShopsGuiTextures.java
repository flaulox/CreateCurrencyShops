package net.flaulox.create_currency_shops;

import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public enum CreateCurrencyShopsGuiTextures implements ScreenElement, TextureSheetSegment {

    WALLET("wallet", 200, 171),
    COIN_SAFE("coin_safe", 200, 171),
    TOGGLE_BUTTON_OFF("button_widgets", 0, 0, 16, 10),
    TOGGLE_BUTTON_ON("button_widgets", 17, 0, 16, 10),
    LOCK_BUTTON_LOCKED("button_widgets", 0, 11, 15, 16),
    LOCK_BUTTON_UNLOCKED("button_widgets", 16, 11, 15, 16);

    public final ResourceLocation location;
    private final int width;
    private final int height;
    private final int startX;
    private final int startY;

    CreateCurrencyShopsGuiTextures(String location, int width, int height) {
        this(location, 0, 0, width, height);
    }

    CreateCurrencyShopsGuiTextures(String location, int startX, int startY, int width, int height) {
        this(CreateCurrencyShops.MODID, location, startX, startY, width, height);
    }

    CreateCurrencyShopsGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
        this.location = ResourceLocation.fromNamespaceAndPath(namespace, "textures/gui/" + location + ".png");
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
    }

    @Override
    public ResourceLocation getLocation() {
        return location;
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, startX, startY, width, height);
    }


    @Override
    public int getStartX() {
        return startX;
    }

    @Override
    public int getStartY() {
        return startY;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
