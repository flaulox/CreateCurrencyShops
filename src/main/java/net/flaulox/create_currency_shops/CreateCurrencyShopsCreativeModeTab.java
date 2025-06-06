package net.flaulox.create_currency_shops;


import com.simibubi.create.AllCreativeModeTabs;
import net.flaulox.create_currency_shops.util.FeatureToggle;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

public class CreateCurrencyShopsCreativeModeTab {
    private static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateCurrencyShops.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BASE_CREATIVE_TAB = REGISTER.register("base",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_currency_shops.base"))
                    .withTabsBefore(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getId())
                    .icon(CreateCurrencyShopsItems.CURRENCY_ITEM::asStack)
                    .displayItems((parameters, output) -> {
                        output.accept(CreateCurrencyShopsItems.COPPER_COIN.get());
                        output.accept(CreateCurrencyShopsItems.IRON_COIN.get());
                        output.accept(CreateCurrencyShopsItems.ZINC_COIN.get());
                        output.accept(CreateCurrencyShopsItems.BRASS_COIN.get());
                        output.accept(CreateCurrencyShopsItems.GOLD_COIN.get());
                        output.accept(CreateCurrencyShopsItems.NETHERITE_COIN.get());
                        output.accept(CreateCurrencyShopsItems.WALLET.get());
                        output.accept(CreateCurrencyShopsItems.CREDIT_CARD.get());
                        output.accept(CreateCurrencyShopsBlocks.COIN_SAFE.get());
                        if (FeatureToggle.isEnabled(CreateCurrencyShops.asResource("coin_drafter")))
                            output.accept(CreateCurrencyShopsBlocks.COIN_DRAFTER.get());


                    }).build());

    @ApiStatus.Internal
    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }

}

