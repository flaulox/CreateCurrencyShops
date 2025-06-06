package net.flaulox.create_currency_shops.blocks;

import com.simibubi.create.content.logistics.box.PackageItem;
import net.flaulox.create_currency_shops.items.CoinDraftItem;
import net.flaulox.create_currency_shops.util.CurrencyValues;
import net.flaulox.create_currency_shops.CreateCurrencyShopsItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;


public class CoinSafeItemHandler extends ItemStackHandler {
    public final CoinSafeBlockEntity coinSafe;
    private final List<Item> coinTypes;

    public CoinSafeItemHandler(CoinSafeBlockEntity coinSafe) {
        super(1);
        this.coinSafe = coinSafe;
        this.coinTypes = CurrencyValues.COIN_VALUES.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot != 0) return ItemStack.EMPTY;

        int totalValue = coinSafe.getTotalValue();
        if (totalValue > 0) {
            ItemStack stack = new ItemStack(CreateCurrencyShopsItems.CURRENCY_ITEM.get());
            stack.setCount(totalValue);

            var nbt = new CompoundTag();
            nbt.putString("SafeName", coinSafe.getCustomName());
            nbt.putString("SafeCode", coinSafe.getShortID());
            if (coinSafe.isLocked()) {
                nbt.putUUID("LockedOwner", coinSafe.ownerUUID);
            }
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        // Handle coin drafts
        if (stack.getItem() instanceof CoinDraftItem) {
            java.util.UUID writtenTo = CoinDraftItem.getWrittenTo(stack);
            java.util.UUID issuedFrom = CoinDraftItem.getIssuedFrom(stack);
            java.util.UUID thisSafeId = coinSafe.getSafeId();
            java.util.UUID anyUUID = new java.util.UUID(0, 0);
            
            if (thisSafeId.equals(writtenTo) || thisSafeId.equals(issuedFrom) || anyUUID.equals(writtenTo)) {
                if (!simulate) {
                    int value = CoinDraftItem.getValue(stack);
                    coinSafe.addValue(value);
                }
                return ItemStack.EMPTY;
            }
            return stack;
        }
        
        // Allow insertion of coin packages in any slot
        if (stack.getItem() instanceof PackageItem) {
            ItemStackHandler contents = PackageItem.getContents(stack);
            
            // Validate all coin drafts in package
            for (int i = 0; i < contents.getSlots(); i++) {
                ItemStack item = contents.getStackInSlot(i);
                if (item.getItem() instanceof CoinDraftItem) {
                    java.util.UUID writtenTo = CoinDraftItem.getWrittenTo(item);
                    java.util.UUID issuedFrom = CoinDraftItem.getIssuedFrom(item);
                    java.util.UUID thisSafeId = coinSafe.getSafeId();
                    java.util.UUID anyUUID = new java.util.UUID(0, 0);
                    
                    if (!thisSafeId.equals(writtenTo) && !thisSafeId.equals(issuedFrom) && !anyUUID.equals(writtenTo)) {
                        return stack;
                    }
                }
            }
            
            if (!simulate) {
                for (int i = 0; i < contents.getSlots(); i++) {
                    ItemStack coinStack = contents.getStackInSlot(i);
                    if (!coinStack.isEmpty() && CurrencyValues.COIN_VALUES.containsKey(coinStack.getItem())) {
                        coinSafe.addCoins(coinStack.getItem(), coinStack.getCount());
                    } else if (coinStack.getItem() instanceof CoinDraftItem) {
                        int value = CoinDraftItem.getValue(coinStack);
                        coinSafe.addValue(value);
                    }
                }
            }
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 0 || amount <= 0) return ItemStack.EMPTY;

        int totalValue = coinSafe.getTotalValue();
        int toExtract = Math.min(amount, totalValue);

        if (toExtract <= 0) return ItemStack.EMPTY;

        if (!simulate) {
            coinSafe.removeValue(toExtract);
        }

        ItemStack result = new ItemStack(CreateCurrencyShopsItems.CURRENCY_ITEM.get());
        result.setCount(toExtract);

        var nbt = new CompoundTag();
        nbt.putString("SafeName", coinSafe.getCustomName());
        nbt.putString("SafeCode", coinSafe.getShortID());
        if (coinSafe.isLocked()) {
            nbt.putUUID("LockedOwner", coinSafe.ownerUUID);
        }
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        return result;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return stack.getItem() instanceof PackageItem
            || stack.getItem() instanceof CoinDraftItem;
    }


}