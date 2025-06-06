package net.flaulox.create_currency_shops.util;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.flaulox.create_currency_shops.Config;
import net.flaulox.create_currency_shops.CreateCurrencyShops;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public record FeatureToggle(ResourceLocation feature) implements ICondition {
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS = 
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, CreateCurrencyShops.MODID);

    public static final Supplier<MapCodec<FeatureToggle>> CODEC = CONDITION_CODECS.register(
            "feature_toggle",
            () -> RecordCodecBuilder.mapCodec(
                    instance -> instance.group(
                            ResourceLocation.CODEC.fieldOf("tag").forGetter(FeatureToggle::feature)
                    ).apply(instance, FeatureToggle::new)
            )
    );

    public static void register(IEventBus modEventBus) {
        CONDITION_CODECS.register(modEventBus);
    }

    @Override
    public boolean test(ICondition.IContext context) {
        return isEnabled(feature);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC.get();
    }

    public static boolean isEnabled(ResourceLocation key) {
        if (key.getPath().equals("coin_drafter")) {
            return Config.ENABLE_COIN_DRAFTER.get();
        }
        return true;
    }
}
