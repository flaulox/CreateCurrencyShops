package net.flaulox.create_currency_shops.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;


public class CoinTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();

        // Check if this item has a value in the currencyValues map
        Integer value = CurrencyValues.COIN_VALUES.get(item);

        if (value != null) {
            String formatted = String.format("%,d", value);
            event.getToolTip().add(Component.translatable("gui.create_currency_shops.value").withStyle(ChatFormatting.GRAY).append(Component.literal(" " + formatted).withStyle(ChatFormatting.GOLD)));
        }
    }


}
