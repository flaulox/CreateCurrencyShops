package net.flaulox.create_currency_shops.mixin;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.flaulox.create_currency_shops.CreateCurrencyShopsItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FilteringBehaviour.class, remap = false)
public class FilteringBehaviourGetValueSettingsMixin {
    @Inject(method = "getValueSettings", at = @At("HEAD"), cancellable = true)
    private void getValueSettings(CallbackInfoReturnable<ValueSettingsBehaviour.ValueSettings> cir) {
        FilteringBehaviour self = (FilteringBehaviour) (Object) this;
        if (self.getFilter().is(CreateCurrencyShopsItems.CURRENCY_ITEM)) {
            FilteringBehaviourAccessor accessor = (FilteringBehaviourAccessor) self;
            int count = accessor.getCount();

            int row = 0;
            int value = count;

            if (count > 1600) {
                row = 3;
                value = ((count - 1600) / 100);
            } else if (count > 600) {
                row = 2;
                value = ((count - 600) / 10);
            } else if (count > 100) {
                row = 1;
                value = ((count - 100) / 5);
            }

            cir.setReturnValue(new ValueSettingsBehaviour.ValueSettings(row, Math.max(1, value)));
        }
    }
}