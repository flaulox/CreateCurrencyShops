package net.flaulox.create_currency_shops.mixin;

import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FilteringBehaviour.class, remap = false)
public interface FilteringBehaviourAccessor {
    @Accessor("count")
    int getCount();

    @Accessor("count")
    void setCount(int count);
}