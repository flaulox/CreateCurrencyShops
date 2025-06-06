package net.flaulox.create_currency_shops;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.api.registry.registrate.SimpleBuilder;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public class CreateCurrencyShopsRegistrate extends AbstractRegistrate<CreateCurrencyShopsRegistrate> {
    @Nullable
    protected Function<Item, TooltipModifier> currentTooltipModifierFactory;


    protected CreateCurrencyShopsRegistrate(String modid) {
        super(modid);
    }

    public static CreateCurrencyShopsRegistrate create(String modid) {
        return new CreateCurrencyShopsRegistrate(modid);
    }

    public <P> CreateCurrencyShopsRegistrate setTooltipModifierFactory(@Nullable Function<Item, TooltipModifier> factory) {
        currentTooltipModifierFactory = factory;
        return self();
    }

    public <T extends MountedItemStorageType<?>> SimpleBuilder<MountedItemStorageType<?>, T, CreateCurrencyShopsRegistrate> mountedItemStorage(String name, Supplier<T> supplier) {
        return this.entry(name, callback -> new SimpleBuilder<>(
            this, this, name, callback, CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE, supplier
        ).byBlock(MountedItemStorageType.REGISTRY));
    }

    public <T extends DisplaySource> SimpleBuilder<DisplaySource, T, CreateCurrencyShopsRegistrate> displaySource(String name, Supplier<T> supplier) {
        return this.entry(name, callback -> new SimpleBuilder<>(
            this, this, name, callback, CreateRegistries.DISPLAY_SOURCE, supplier
        ));
    }
}
