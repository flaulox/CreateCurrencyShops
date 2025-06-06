package net.flaulox.create_currency_shops.client;

import net.flaulox.create_currency_shops.CreateCurrencyShopsItems;
import net.flaulox.create_currency_shops.items.WalletItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public class WalletModelPredicates {
    public static void register() {
        ItemProperties.register(CreateCurrencyShopsItems.WALLET.get(), 
            ResourceLocation.withDefaultNamespace("empty"), 
            (stack, level, entity, seed) -> {
                boolean hasCoins = WalletItem.getTotalValue(stack) > 0;
                boolean hasCard = WalletItem.hasCreditCard(stack);
                return (hasCoins || hasCard) ? 0.0F : 1.0F;
            });
    }
}