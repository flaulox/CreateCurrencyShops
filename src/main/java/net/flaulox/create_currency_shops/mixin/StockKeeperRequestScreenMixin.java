package net.flaulox.create_currency_shops.mixin;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import net.flaulox.create_currency_shops.CreateCurrencyShopsItems;
import net.minecraft.core.component.DataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(StockKeeperRequestScreen.class)
public class StockKeeperRequestScreenMixin {
    @Shadow public List<List<BigItemStack>> displayedItems;
    
    @Inject(method = "refreshSearchResults", at = @At("RETURN"), remap = false)
    private void filterLockedCoinSafes(boolean scrollBackUp, CallbackInfo ci) {
        var player = ((StockKeeperRequestScreen)(Object)this).getMenu().player;
        
        for (List<BigItemStack> category : displayedItems) {
            category.removeIf(stack -> {
                if (!stack.stack.is(CreateCurrencyShopsItems.CURRENCY_ITEM) && !stack.stack.is(CreateCurrencyShopsItems.WRITE_DRAFT_ITEM)) return false;
                var customData = stack.stack.get(DataComponents.CUSTOM_DATA);
                if (customData == null) return false;
                var nbt = customData.copyTag();
                if (!nbt.hasUUID("LockedOwner")) return false;
                return !nbt.getUUID("LockedOwner").equals(player.getUUID());
            });
        }
    }
}
