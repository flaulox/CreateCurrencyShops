package net.flaulox.create_currency_shops;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.flaulox.create_currency_shops.displaylink.CoinSafeTotalValueSource;
import net.flaulox.create_currency_shops.displaylink.CoinSafeShortIDSource;
import net.flaulox.create_currency_shops.displaylink.CoinSafeNameSource;
import net.minecraft.core.registries.Registries;

public class CreateCurrencyShopsDisplaySources {

    public static final RegistryEntry<DisplaySource, CoinSafeNameSource> COIN_SAFE_NAME =
            CreateCurrencyShops.registrate()
                    .displaySource("coin_safe_name", CoinSafeNameSource::new)
                    .onRegisterAfter(Registries.BLOCK_ENTITY_TYPE, source ->
                            DisplaySource.BY_BLOCK_ENTITY.add(CreateCurrencyShopsBlocks.COIN_SAFE_BLOCK_ENTITY.get(), source))
                    .register();

    
    public static final RegistryEntry<DisplaySource, CoinSafeTotalValueSource> COIN_SAFE_TOTAL_VALUE =
        CreateCurrencyShops.registrate()
            .displaySource("coin_safe_total_value", CoinSafeTotalValueSource::new)
            .onRegisterAfter(Registries.BLOCK_ENTITY_TYPE, source -> 
                DisplaySource.BY_BLOCK_ENTITY.add(CreateCurrencyShopsBlocks.COIN_SAFE_BLOCK_ENTITY.get(), source))
            .register();
    

    
    public static final RegistryEntry<DisplaySource, CoinSafeShortIDSource> COIN_SAFE_SHORTID =
        CreateCurrencyShops.registrate()
            .displaySource("coin_safe_shortid", CoinSafeShortIDSource::new)
            .onRegisterAfter(Registries.BLOCK_ENTITY_TYPE, source -> 
                DisplaySource.BY_BLOCK_ENTITY.add(CreateCurrencyShopsBlocks.COIN_SAFE_BLOCK_ENTITY.get(), source))
            .register();
    
    public static void register() {}
}
