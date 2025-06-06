package net.flaulox.create_currency_shops.ponder;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.flaulox.create_currency_shops.CreateCurrencyShops;
import net.minecraft.resources.ResourceLocation;

public class CurrencyShopsPonderPlugin implements PonderPlugin {
    
    @Override
    public String getModId() {
        return CreateCurrencyShops.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        CreateCurrencyShopsPonderScenes.register(helper);
    }
    
    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        CurrencyShopsPonderTags.register(helper);
    }
}
