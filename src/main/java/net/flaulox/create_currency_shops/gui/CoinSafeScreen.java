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
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.platform.CatnipServices;
import net.flaulox.create_currency_shops.*;
import net.flaulox.create_currency_shops.blocks.CoinSafeBlock;
import net.flaulox.create_currency_shops.network.CoinSafeLockPacket;
import net.flaulox.create_currency_shops.network.CoinSafeNamePacket;
import net.flaulox.create_currency_shops.util.CurrencyValues;
import net.flaulox.create_currency_shops.util.FeatureToggle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;

public class CoinSafeScreen extends AbstractSimiContainerScreen<CoinSafeMenu> {
    protected static final CreateCurrencyShopsGuiTextures BG = CreateCurrencyShopsGuiTextures.COIN_SAFE;
    protected static final AllGuiTextures PLAYER = AllGuiTextures.PLAYER_INVENTORY;

    protected Slot hoveredWalletSlot;
    private IconButton confirmButton;
    private IconButton collectButton;
    private LockButton lockButton;
    private EditBox nameBox;
    private List<Rect2i> extraAreas = Collections.emptyList();

    public CoinSafeScreen(CoinSafeMenu menu, Inventory inv, Component title) {
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
            // Send name change if modified
            String nameToSend = nameBox.getValue().isEmpty() ? "Coin Safe" : nameBox.getValue();
            if (!nameToSend.equals(menu.contentHolder.getCustomName())) {
                CatnipServices.NETWORK.sendToServer(
                    new CoinSafeNamePacket(
                        menu.contentHolder.getBlockPos(), nameToSend));
            }
            minecraft.player.closeContainer();
        });
        addRenderableWidget(confirmButton);

        collectButton = new IconButton(leftPos + 117, topPos + 58, AllIcons.I_REFRESH);
        collectButton.withCallback(() -> {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
        });
        collectButton.getToolTip().add(Component.translatable("gui.create_currency_shops.gui.collect_coins"));
        addRenderableWidget(collectButton);

        if (menu.contentHolder.isAdmin(menu.player)) {
            lockButton = new LockButton(leftPos + 200, topPos + 20, menu.contentHolder.isLocked());
            lockButton.withCallback(() -> {
                CatnipServices.NETWORK.sendToServer(
                    new CoinSafeLockPacket(menu.contentHolder.getBlockPos()));
            });
            addRenderableWidget(lockButton);
        }
        
        nameBox = new EditBox(font, leftPos + 36 + 5, topPos + 4, BG.getWidth() - 20, 12, Component.empty());
        nameBox.setBordered(false);
        nameBox.setMaxLength(20);
        nameBox.setTextColor(0x592424);
        nameBox.setTextShadow(false);
        nameBox.setValue(menu.contentHolder.getCustomName());
        nameBox.setResponder(s -> nameBox.setX(nameBoxX(s, nameBox)));
        nameBox.setX(nameBoxX(nameBox.getValue().isEmpty() ? "Coin Safe" : nameBox.getValue(), nameBox));
        if (menu.contentHolder.isAdmin(menu.player)) {
            addRenderableWidget(nameBox);
        }

        extraAreas = ImmutableList.of(
            new Rect2i(leftPos + 30 + BG.getWidth(), topPos + BG.getHeight() - 50, 72, 52)
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean nameBoxClicked = nameBox.mouseClicked(mouseX, mouseY, button);
        
        if (nameBoxClicked && nameBox.getValue().equals("Coin Safe")) {
            nameBox.setValue("");
        } else if (nameBox.isFocused() && nameBox.getValue().isEmpty() && !nameBoxClicked) {
            nameBox.setValue("Coin Safe");
            nameBox.setFocused(false);
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
            if (nameBox.isFocused()) {
                // Set default name if empty
                if (nameBox.getValue().isEmpty()) {
                    nameBox.setValue("Coin Safe");
                }
                // Send name change to server
                String nameToSend = nameBox.getValue().isEmpty() ? "Coin Safe" : nameBox.getValue();
                CatnipServices.NETWORK.sendToServer(
                    new CoinSafeNamePacket(
                        menu.contentHolder.getBlockPos(), nameToSend));
                nameBox.setFocused(false);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        // Send name change if modified when closing
        String nameToSend = nameBox.getValue().isEmpty() ? "Coin Safe" : nameBox.getValue();
        if (!nameToSend.equals(menu.contentHolder.getCustomName())) {
            CatnipServices.NETWORK.sendToServer(
                new CoinSafeNamePacket(
                    menu.contentHolder.getBlockPos(), nameToSend));
        }
        super.onClose();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = leftPos + imageWidth - BG.getWidth();
        int y = topPos;

        BG.render(graphics, x, y);



        renderCoinSafe(graphics, leftPos + 38 + BG.getWidth() + 45, topPos + 125 + 53, partialTicks);

        
        // Don't render title since nameBox replaces it

        // Render name text or edit icon
        if (menu.contentHolder.isAdmin(menu.player)) {
            if (!nameBox.isFocused()) {
                String text = nameBox.getValue();
                AllGuiTextures.STATION_EDIT_NAME.render(graphics,
                    nameBox.getX() + font.width(text) + 5, topPos + 1);
            }
        } else {
            String text = menu.contentHolder.getCustomName();
            graphics.drawString(font, text, nameBoxX(text, nameBox), topPos + 4, 0x592424, false);
        }

        int invX = leftPos + 12;
        int invY = topPos + imageHeight - PLAYER.getHeight();
        renderPlayerInventory(graphics, invX, invY);

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
                if (maxPossible == 0) {
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.4f);
                }
                graphics.renderItem(minecraft.player, displayStack, slotX, slotY, 0);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                graphics.renderItemDecorations(font, displayStack, slotX, slotY, "");
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
        super.render(graphics, mouseX, mouseY, partialTicks);


        // Render total value prominently
        int totalValue = menu.getTotalValue();
        Component totalLabel = Component.translatable("gui.create_currency_shops.total");
        Component valueText = Component.literal(String.format("%,d", totalValue));
        int x = leftPos + imageWidth - BG.getWidth();
        
        graphics.drawString(font, totalLabel, x + 20, topPos + 123, 0x592424, false);
        
        int valueWidth = font.width(valueText);
        int valueCenterX = x + 97;
        graphics.drawString(font, valueText, valueCenterX - valueWidth / 2, topPos + 123, 0x592424, false);

        if (FeatureToggle.isEnabled(CreateCurrencyShops.asResource("coin_drafter"))) {
            String shortId = "$" + menu.contentHolder.getShortID();
            graphics.drawString(font, shortId, x + 157, topPos + 130, 0x8B8B8B, false);
        }


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
    
    private void renderCoinSafe(GuiGraphics graphics, int x, int y, float partialTicks) {



        PoseStack ms = graphics.pose();



        TransformStack.of(ms)
            .pushPose()
            .translate(x, y, 100)
            .scale(45)
//                .rotateToFace(Direction.NORTH)
            .rotateXDegrees(-22)
            .rotateYDegrees(-202);




        GuiGameElement.of(menu.contentHolder.getBlockState().setValue(CoinSafeBlock.FACING, Direction.NORTH))
            .render(graphics);




        float doorAngle = menu.contentHolder.door.getValue(partialTicks);
        TransformStack.of(ms)
            .pushPose()
                .translate(14/16f, 1/16f, 1/16f)
                .rotateYDegrees(-45 * doorAngle)
                .translate(-14/16f, -1/16f, -1/16f);

        GuiGameElement.of(CreateCurrencyShopsPartialModels.COIN_SAFE_DOOR)
            .render(graphics);
        ms.popPose();

        float wheelAngle = menu.contentHolder.wheel.getValue(partialTicks);
        TransformStack.of(ms)
            .pushPose()
                .translate(14/16f, 1/16f, 1/16f)
                .rotateYDegrees(-45 * doorAngle)
                .translate(-14/16f, -1/16f, -1/16f)

                .translate(8/16f, -9/16f, 0.5f/16f)
                .rotateZDegrees(wheelAngle * 180)
                .translate(-8/16f, 9/16f, -0.5f/16f);


        GuiGameElement.of(CreateCurrencyShopsPartialModels.COIN_SAFE_WHEEL)
            .render(graphics);
        ms.popPose();

        ms.popPose();
    }

    private int nameBoxX(String s, EditBox nameBox) {
        if (font == null) return leftPos + 30 + 5; // Fallback if font is null
        String displayText = s;
        return leftPos + 30 + BG.getWidth() / 2 - (Math.min(font.width(displayText), nameBox.getWidth()) + 7) / 2;

    }
}