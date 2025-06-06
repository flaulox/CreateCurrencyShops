package net.flaulox.create_currency_shops.mixin;

import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.logistics.BigItemStack;
import net.flaulox.create_currency_shops.CreateCurrencyShopsItems;
import net.flaulox.create_currency_shops.util.CreateCurrencyShopsHelper;
import net.flaulox.create_currency_shops.items.CreditCardItem;
import net.flaulox.create_currency_shops.items.WalletItem;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlueprintOverlayRenderer.class)
public class BlueprintOverlayRendererMixin {
    @Inject(
            method = "canAfford",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void injectCanAfford(Player player, BigItemStack entry, CallbackInfoReturnable<Boolean> cir) {
        // Check if this is a currency item
        if (entry.stack.is(CreateCurrencyShopsItems.CURRENCY_ITEM)) {
            int totalInventoryValue;

            if (player.getOffhandItem().getItem() == CreateCurrencyShopsItems.WALLET.asItem()) {
                totalInventoryValue = WalletItem.getTotalValue(player.getOffhandItem());
                if (WalletItem.hasCreditCard(player.getOffhandItem())) {
                    totalInventoryValue += CreditCardItem.getTotalValue(WalletItem.getCreditCard(player.getOffhandItem(), player.level()), player.level());
                }
            } else if (player.getOffhandItem().getItem() == CreateCurrencyShopsItems.CREDIT_CARD.asItem()){
                totalInventoryValue = CreditCardItem.getTotalValue(player.getOffhandItem(), player.level());
            } else {
                totalInventoryValue = CreateCurrencyShopsHelper.countTotalValueInInventory(player.getInventory(), player.level());
            }
            
            cir.setReturnValue(totalInventoryValue >= entry.count);
        }
    }
}
