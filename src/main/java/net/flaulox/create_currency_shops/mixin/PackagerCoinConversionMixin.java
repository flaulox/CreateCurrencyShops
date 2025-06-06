package net.flaulox.create_currency_shops.mixin;

import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import net.flaulox.create_currency_shops.util.CurrencyValues;
import net.flaulox.create_currency_shops.CreateCurrencyShopsItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(PackagerBlockEntity.class)
public class PackagerCoinConversionMixin {
    
    @Shadow public ItemStack heldBox;
    @Shadow public int animationTicks;
    @Shadow public boolean animationInward;
    @Shadow public int buttonCooldown;
    @Shadow public InvManipulationBehaviour targetInventory;
    
    @Shadow private static final int CYCLE = 20;
    
    @Inject(method = "attemptToSend", at = @At("HEAD"), cancellable = true)
    private void interceptCurrencyPackaging(List<PackagingRequest> queuedRequests, CallbackInfo ci) {
        IItemHandler targetInv = targetInventory.getInventory();
        if (targetInv == null || targetInv.getSlots() == 0)
            return;
            
        ItemStack firstSlot = targetInv.getStackInSlot(0);
        if (!firstSlot.is(CreateCurrencyShopsItems.CURRENCY_ITEM.get()))
            return;
            
        if (queuedRequests != null && !queuedRequests.isEmpty() && 
            !queuedRequests.get(0).item().is(CreateCurrencyShopsItems.CURRENCY_ITEM.get()))
            return;
            
        handleCurrencyPackaging(queuedRequests);
        ci.cancel();
    }
    
    private void handleCurrencyPackaging(List<PackagingRequest> queuedRequests) {
        if (!heldBox.isEmpty() || animationTicks != 0 || buttonCooldown > 0)
            return;

        IItemHandler targetInv = targetInventory.getInventory();
        if (targetInv == null)
            return;

        int currencyAmount = queuedRequests != null && !queuedRequests.isEmpty() 
            ? queuedRequests.get(0).getCount() 
            : targetInv.getStackInSlot(0).getCount();
        
        ItemStackHandler extractedItems = new ItemStackHandler(PackageItem.SLOTS);
        int remainingValue = currencyAmount;
        
        for (var entry : CurrencyValues.COIN_VALUES.entrySet().stream()
                .sorted(Map.Entry.<Item, Integer>comparingByValue().reversed())
                .toList()) {
            
            if (remainingValue <= 0) break;
            
            int coinValue = entry.getValue();
            int coinCount = remainingValue / coinValue;
            
            if (coinCount > 0) {
                ItemStack coinStack = new ItemStack(entry.getKey(), Math.min(coinCount, 64));
                ItemHandlerHelper.insertItemStacked(extractedItems, coinStack, false);
                remainingValue -= coinCount * coinValue;
            }
        }
        
        targetInv.extractItem(0, currencyAmount, false);
        if (queuedRequests != null && !queuedRequests.isEmpty()) {
            queuedRequests.get(0).subtract(currencyAmount);
            queuedRequests.remove(0);
        }
        
        ItemStack createdBox = PackageItem.containing(extractedItems);
        if (queuedRequests != null && !queuedRequests.isEmpty() && queuedRequests.get(0).address() != null)
            PackageItem.addAddress(createdBox, queuedRequests.get(0).address());
        
        heldBox = createdBox;
        animationInward = false;
        animationTicks = CYCLE;
        
        ((PackagerBlockEntity)(Object)this).notifyUpdate();
        ((PackagerBlockEntity)(Object)this).triggerStockCheck();
    }
}
