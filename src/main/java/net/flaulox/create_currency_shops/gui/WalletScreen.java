package net.flaulox.create_currency_shops.gui;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.flaulox.create_currency_shops.util.CurrencyValues;
import net.flaulox.create_currency_shops.CreateCurrencyShopsGuiTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;

public class WalletScreen extends AbstractSimiContainerScreen<WalletMenu> {
    protected static final CreateCurrencyShopsGuiTextures BG = CreateCurrencyShopsGuiTextures.WALLET;
    protected static final AllGuiTextures PLAYER = AllGuiTextures.PLAYER_INVENTORY;

    protected Slot hoveredWalletSlot;
    private IconButton confirmButton;
    private IconButton collectButton;
    private ToggleButton toggleButton;
    private List<Rect2i> extraAreas = Collections.emptyList();

    public WalletScreen(WalletMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        init();
    }

    @Override
    protected void init() {
        setWindowSize(30 + BG.getWidth(), BG.getHeight() + PLAYER.getHeight() - 24);
        setWindowOffset(-11, 0);
        super.init();
        clearWidgets();

        confirmButton = new IconButton(leftPos + 197, topPos + 147, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            minecraft.player.closeContainer();
        });
        addRenderableWidget(confirmButton);

        collectButton = new IconButton(leftPos + 117, topPos + 58, AllIcons.I_REFRESH);
        collectButton.withCallback(() -> {
            if (minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                // Force screen refresh
                this.init();
            }
        });
        collectButton.getToolTip().add(Component.translatable("gui.create_currency_shops.gui.collect_coins"));
        addRenderableWidget(collectButton);

        toggleButton = new ToggleButton(leftPos + 41, topPos + 46, menu.isUseCashFirst());
        toggleButton.withCallback(() -> {
            if (minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            }
        });
        addRenderableWidget(toggleButton);


        extraAreas = ImmutableList.of(
            new Rect2i(leftPos + 30 + BG.getWidth(), topPos + BG.getHeight() - 32, 72, 36)
        );
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = leftPos + imageWidth - BG.getWidth();
        int y = topPos;

        BG.render(graphics, x, y);
        

        
        graphics.drawString(font, title, x + BG.getWidth() / 2 - font.width(title) / 2, y + 4, 0x592424, false);

        int invX = leftPos + 12;
        int invY = topPos + imageHeight - PLAYER.getHeight();
        renderPlayerInventory(graphics, invX, invY);

        // Render wallet item on the right
        ItemStack walletStack = menu.getCurrentWallet();
        if (!walletStack.isEmpty()) {
            int walletX = leftPos + 38 + BG.getWidth();
            int walletY = topPos + 125;
            PoseStack ms = graphics.pose();
            ms.pushPose();
            ms.translate(walletX, walletY, 0);
            ms.scale(4.0f, 4.0f, 1.0f);
            graphics.renderItem(walletStack, 0, 0);
            graphics.renderItemDecorations(font, walletStack, 0, 0);
            ms.popPose();
        }

        PoseStack ms = graphics.pose();

        hoveredWalletSlot = null;
        for (int i = 0; i < menu.getCoinTypes().size(); i++) {
            Slot slot = menu.slots.get(i);
            Item coinType = menu.getCoinTypes().get(i);
            int coinValue = CurrencyValues.COIN_VALUES.getOrDefault(coinType, 0);
            int totalValue = menu.getTotalValue();
            int maxPossible = totalValue / coinValue;
            
            ItemStack displayStack = new ItemStack(coinType);
            int slotX = slot.x + leftPos;
            int slotY = slot.y + topPos;

            if (!displayStack.isEmpty()) {
                ms.pushPose();
                ms.translate(0, 0, 100);
                RenderSystem.enableDepthTest();
                RenderSystem.enableBlend();
                if (maxPossible == 0) {
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.4f);
                }
                graphics.renderItem(minecraft.player, displayStack, slotX, slotY, 0);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                RenderSystem.disableBlend();
                graphics.renderItemDecorations(font, displayStack, slotX, slotY, "");
                if (maxPossible > 1) {
                    ms.translate(slotX, slotY + 1, 10);
                    drawItemCount(graphics, maxPossible);
                    ms.translate(-slotX, -slotY - 1, -10);
                }
                ms.popPose();
            }

            if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                hoveredWalletSlot = slot;
                RenderSystem.disableDepthTest();
                RenderSystem.colorMask(true, true, true, false);
                int slotColor = this.getSlotColor(i);
                graphics.fillGradient(slotX, slotY, slotX + 16, slotY + 16, slotColor, slotColor);
                RenderSystem.colorMask(true, true, true, true);
                RenderSystem.enableDepthTest();
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        menu.renderPass = true;
        super.render(graphics, mouseX, mouseY, partialTicks);
        menu.renderPass = false;
        
        // Render total value prominently
        int totalValue = menu.getTotalValue();
        Component totalLabel = Component.translatable("gui.create_currency_shops.total");
        Component valueText = Component.literal(String.format("%,d", totalValue));
        int x = leftPos + imageWidth - BG.getWidth();
        
        graphics.drawString(font, totalLabel, x + 20, topPos + 123, 0x592424, false);
        
        // Center the value around a position to the right
        int valueWidth = font.width(valueText);
        int valueCenterX = x + 97; // Position to center around
        graphics.drawString(font, valueText, valueCenterX - valueWidth / 2, topPos + 123, 0x592424, false);
    }

    private void drawItemCount(GuiGraphics graphics, int count) {
        String text = count >= 1000000 ? (count / 1000000) + "m"
            : count >= 10000 ? (count / 1000) + "k"
            : count >= 1000 ? ((count * 10) / 1000) / 10f + "k" 
            : String.valueOf(count);

        if (text.isBlank())
            return;

        int x = (int) Math.floor(-text.length() * 2.5);
        for (char c : text.toCharArray()) {
            int index = c - '0';
            int xOffset = index * 6;
            int spriteWidth = AllGuiTextures.NUMBERS.getWidth();

            switch (c) {
                case ' ':
                    x += 4;
                    continue;
                case '.':
                    spriteWidth = 3;
                    xOffset = 60;
                    break;
                case 'k':
                    xOffset = 64;
                    break;
                case 'm':
                    spriteWidth = 7;
                    xOffset = 70;
                    break;
            }

            RenderSystem.enableBlend();
            graphics.blit(AllGuiTextures.NUMBERS.location, 14 + x, 10, 0,
                AllGuiTextures.NUMBERS.getStartX() + xOffset,
                AllGuiTextures.NUMBERS.getStartY(),
                spriteWidth, AllGuiTextures.NUMBERS.getHeight(), 256, 256);
            x += spriteWidth - 1;
        }
    }

    @Override
    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (hoveredWalletSlot != null) {
            hoveredSlot = hoveredWalletSlot;
            // Render coin tooltip
            int slotIndex = hoveredWalletSlot.index;
            if (slotIndex < menu.getCoinTypes().size()) {
                Item coinType = menu.getCoinTypes().get(slotIndex);

                ItemStack displayStack = new ItemStack(coinType);
                List<Component> tooltip = new ArrayList<>(displayStack.getTooltipLines(Item.TooltipContext.of(minecraft.level), minecraft.player, TooltipFlag.NORMAL));
                
                graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
            }
        }
        super.renderForeground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }
}