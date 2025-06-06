package net.flaulox.create_currency_shops.util;

import net.flaulox.create_currency_shops.CreateCurrencyShopsItems;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;



public class CurrencyValues {
    public static final Map<Item, Integer> COIN_VALUES = new HashMap<>();

    static {
        init();
    }

    public static void init() {
        if (!COIN_VALUES.isEmpty()) return;
        COIN_VALUES.put(CreateCurrencyShopsItems.COPPER_COIN.get(), 1);
        COIN_VALUES.put(CreateCurrencyShopsItems.IRON_COIN.get(), 5);
        COIN_VALUES.put(CreateCurrencyShopsItems.ZINC_COIN.get(), 10);
        COIN_VALUES.put(CreateCurrencyShopsItems.BRASS_COIN.get(), 50);
        COIN_VALUES.put(CreateCurrencyShopsItems.GOLD_COIN.get(), 100);
        COIN_VALUES.put(CreateCurrencyShopsItems.NETHERITE_COIN.get(), 500);
    }
}
