package net.flaulox.create_currency_shops.ponder;

import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.flaulox.create_currency_shops.CreateCurrencyShops;
import net.flaulox.create_currency_shops.CreateCurrencyShopsBlocks;
import net.flaulox.create_currency_shops.CreateCurrencyShopsItems;
import net.flaulox.create_currency_shops.util.FeatureToggle;
import net.minecraft.resources.ResourceLocation;

public class CurrencyShopsPonderTags {
    
    public static final ResourceLocation COIN_MANAGEMENT = CreateCurrencyShops.asResource("index");
    
    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<RegistryEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
        
        helper.registerTag(COIN_MANAGEMENT)
            .addToIndex()
            .item(CreateCurrencyShopsItems.CURRENCY_ITEM.get(), true, false)
            .title("Coin Management")
            .description("Components for storing, transferring and managing Coins as well as allowing easier shopping at Table Cloths")
            .register();


        HELPER.addToTag(COIN_MANAGEMENT).add(CreateCurrencyShopsBlocks.COIN_SAFE);


        if (FeatureToggle.isEnabled(CreateCurrencyShops.asResource("coin_drafter"))) {
            HELPER.addToTag(COIN_MANAGEMENT).add(CreateCurrencyShopsBlocks.COIN_DRAFTER);
        }
        
        HELPER.addToTag(AllCreatePonderTags.DISPLAY_SOURCES)
            .add(CreateCurrencyShopsBlocks.COIN_SAFE);
    }
}
