package net.flaulox.create_currency_shops.mixin;

import com.simibubi.create.content.logistics.tableCloth.TableClothFilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.flaulox.create_currency_shops.CreateCurrencyShopsItems;
import net.flaulox.create_currency_shops.util.CreateCurrencyShopsHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TableClothFilteringBehaviour.class, remap = false)
public class TableClothFilteringBehaviourMixin {
    @Inject(
            method = "setFilter",
            at = @At("HEAD"),
            cancellable = true
    )
    private void setFilter(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (CreateCurrencyShopsHelper.isValidCurrency(stack)) {
            FilteringBehaviour self = (FilteringBehaviour) (Object) this;
            FilteringBehaviourAccessor accessor = (FilteringBehaviourAccessor) self;

            accessor.setCount(CreateCurrencyShopsHelper.getValue(stack));

            boolean result = self.setFilter(new ItemStack(CreateCurrencyShopsItems.CURRENCY_ITEM.get()));
            cir.setReturnValue(result);
        }
    }

    @Inject(
            method = "createBoard",
            at = @At("HEAD"),
            cancellable = true
    )
    private void createBoard(Player player, BlockHitResult hitResult, CallbackInfoReturnable<ValueSettingsBoard> cir) {
        TableClothFilteringBehaviour self = (TableClothFilteringBehaviour) (Object) this;
        if (self.getFilter().is(CreateCurrencyShopsItems.CURRENCY_ITEM)) {
            List<Component> currencyRows = List.of(
                Component.literal("Amount"),
                Component.literal("x5"),
                Component.literal("x10"),
                Component.literal("x100")
            );
            ValueSettingsBoard board = new ValueSettingsBoard(
                self.getLabel(), 100, 10,
                currencyRows,
                new ValueSettingsFormatter(value -> {
                    int[] steps = {1, 5, 10, 100};
                    int[] offsets = {0, 100, 600, 1600};
                    int step = steps[value.row()];
                    int offset = offsets[value.row()];
                    int minimum = offsets[value.row()] + steps[value.row()];
                    int actualValue = Math.max(minimum, offset + value.value() * step);
                    return Component.literal(String.valueOf(actualValue));
                })
            );
            cir.setReturnValue(board);
        }
    }

    @Inject(
            method = "setValueSettings",
            at = @At("HEAD"),
            cancellable = true
    )
    private void setValueSettings(Player player, ValueSettingsBehaviour.ValueSettings settings, boolean ctrlDown, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        TableClothFilteringBehaviour self = (TableClothFilteringBehaviour) (Object) this;
        if (self.getFilter().is(CreateCurrencyShopsItems.CURRENCY_ITEM)) {
            if (self.getValueSettings().equals(settings))
                return;
            
            int[] steps = {1, 5, 10, 100};
            int[] offsets = {0, 100, 600, 1600};
            int step = steps[settings.row()];
            int offset = offsets[settings.row()];
            int minimum = offsets[settings.row()] + steps[settings.row()];
            int actualValue = Math.max(minimum, offset + settings.value() * step);

            FilteringBehaviourAccessor accessor = (FilteringBehaviourAccessor) self;
            accessor.setCount(Math.min(actualValue, 11600));
            
            self.blockEntity.setChanged();
            self.blockEntity.sendData();
            ci.cancel();
        }
    }


}
