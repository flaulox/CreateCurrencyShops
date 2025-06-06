package net.flaulox.create_currency_shops.ponder;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.flaulox.create_currency_shops.CreateCurrencyShopsBlocks;
import net.flaulox.create_currency_shops.ponder.scenes.CoinDrafterScenes;
import net.flaulox.create_currency_shops.ponder.scenes.CoinSafeScenes;
import net.minecraft.resources.ResourceLocation;

public class CreateCurrencyShopsPonderScenes {

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(CreateCurrencyShopsBlocks.COIN_SAFE)
                .addStoryBoard("coin_safe/intro", CoinSafeScenes::intro, CurrencyShopsPonderTags.COIN_MANAGEMENT)
                .addStoryBoard("coin_safe/shopping", CoinSafeScenes::shopping, CurrencyShopsPonderTags.COIN_MANAGEMENT);



        HELPER.forComponents(CreateCurrencyShopsBlocks.COIN_DRAFTER)
                .addStoryBoard("coin_drafter/intro", CoinDrafterScenes::intro, CurrencyShopsPonderTags.COIN_MANAGEMENT)
                .addStoryBoard("coin_drafter/write_draft", CoinDrafterScenes::writeDraft, CurrencyShopsPonderTags.COIN_MANAGEMENT);

    }
}
