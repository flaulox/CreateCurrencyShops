package net.flaulox.create_currency_shops.blocks;

import net.flaulox.create_currency_shops.Config;
import net.flaulox.create_currency_shops.CreateCurrencyShops;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@EventBusSubscriber(modid = CreateCurrencyShops.MODID)
public class CoinDrafterRecipeProvider {
    
    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        if (!Config.ENABLE_COIN_DRAFTER.get()) return;
        
        event.addListener((preparationBarrier, resourceManager, profilerFiller, profilerFiller2, executor, executor2) -> 
            preparationBarrier.wait(null).thenRunAsync(() -> {}, executor2)
        );
    }
}
