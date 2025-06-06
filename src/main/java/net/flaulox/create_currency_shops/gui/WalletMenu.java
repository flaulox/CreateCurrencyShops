package net.flaulox.create_currency_shops.gui;

import net.flaulox.create_currency_shops.CreateCurrencyShopsMenus;
import net.flaulox.create_currency_shops.items.CreditCardItem;
import net.flaulox.create_currency_shops.util.CurrencyValues;
import net.flaulox.create_currency_shops.items.WalletItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.Map;

public class WalletMenu extends AbstractContainerMenu {
    private final ItemStack wallet;
    private final Container creditCardContainer;
    private final List<Item> coinTypes;
    private final int walletSlot;
    private final Inventory playerInventory;
    public boolean renderPass;

    public WalletMenu(int containerId, Inventory playerInventory, ItemStack wallet) {
        this(containerId, playerInventory, wallet, playerInventory.selected);
    }

    public WalletMenu(int containerId, Inventory playerInventory, ItemStack wallet, int walletSlot) {
        super(CreateCurrencyShopsMenus.WALLET.get(), containerId);
        this.wallet = wallet;
        this.creditCardContainer = new SimpleContainer(1);
        this.coinTypes = CurrencyValues.COIN_VALUES.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toList());
        this.walletSlot = walletSlot;
        this.playerInventory = playerInventory;

        // Initialize NBT tag if not present
        CompoundTag tag = wallet.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains("UseCashFirst")) {
            tag.putBoolean("UseCashFirst", true);
            wallet.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }

        // Coin slots - both deposit and withdrawal (6 slots in specific pattern)
        int x = 98;
        int y = 26;
        int[] xOffsets = {x, x + 40, x - 15, x + 55, x, x + 40};
        int[] yOffsets = {y, y, y + 33, y + 33, y + 66, y + 66};
        
        for (int i = 0; i < Math.min(coinTypes.size(), 6); i++) {
            this.addSlot(new CoinSlot(coinTypes.get(i), xOffsets[i], yOffsets[i]));
        }

        // Credit card slot
        this.addSlot(new Slot(creditCardContainer, 0, 41, 24) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof CreditCardItem;
            }
            
            @Override
            public int getMaxStackSize() {
                return 1;
            }
            
            @Override
            public void setChanged() {
                super.setChanged();
                WalletItem.setCreditCard(wallet, creditCardContainer.getItem(0), playerInventory.player.level());
            }
        });
        
        // Load credit card after slot is added
        ItemStack storedCard = WalletItem.getCreditCard(wallet, playerInventory.player.level());
        if (!storedCard.isEmpty()) {
            creditCardContainer.setItem(0, storedCard.copy());
        }

        addPlayerSlots(20, 165);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return slot.index > coinTypes.size() && super.canDragTo(slot);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (slot instanceof CoinSlot coinSlot && clickType == ClickType.PICKUP) {
                ItemStack carried = getCarried();
                if (carried.isEmpty()) {
                    handleCoinSlotClick(coinSlot, button, clickType, player);
                    return;
                } else if (CurrencyValues.COIN_VALUES.containsKey(carried.getItem())) {
                    int amount = button == 0 ? carried.getCount() : 1; // Left click = all, right click = 1
                    WalletItem.addCoins(wallet, carried.getItem(), amount);
                    carried.shrink(amount);
                    updateAllSlots();
                    return;
                }
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    private void handleCoinSlotClick(CoinSlot slot, int button, ClickType clickType, Player player) {
        if (clickType != ClickType.PICKUP || CurrencyValues.COIN_VALUES == null) return;
        
        int coinValue = CurrencyValues.COIN_VALUES.getOrDefault(slot.coinType, 0);
        int totalValue = WalletItem.getTotalValue(wallet);
        int maxPossible = totalValue / coinValue;
        
        if (maxPossible <= 0) return;
        
        int toTake = button == 0 ? Math.min(64, maxPossible) : Math.min(32, Math.max(1, maxPossible / 2));
        
        if (withdrawWithConversion(slot.coinType, toTake)) {
            ItemStack result = new ItemStack(slot.coinType, toTake);
            ItemStack cursor = getCarried();
            
            if (cursor.isEmpty()) {
                setCarried(result);
            } else if (ItemStack.isSameItemSameComponents(cursor, result)) {
                int newCount = Math.min(cursor.getMaxStackSize(), cursor.getCount() + toTake);
                cursor.setCount(newCount);
                if (newCount < cursor.getCount() + toTake) {
                    WalletItem.addCoins(wallet, slot.coinType, cursor.getCount() + toTake - newCount);
                }
            } else {
                WalletItem.addCoins(wallet, slot.coinType, toTake);
            }
            updateAllSlots();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null) return ItemStack.EMPTY;

        // Shift-click from coin slots - withdraw to inventory
        if (index < coinTypes.size() && slot instanceof CoinSlot coinSlot) {
            // Check if inventory has space first
            if (player.getInventory().getFreeSlot() == -1) {
                return ItemStack.EMPTY;
            }
            
            int coinValue = CurrencyValues.COIN_VALUES.getOrDefault(coinSlot.coinType, 0);
            int totalValue = WalletItem.getTotalValue(wallet);
            int maxPossible = totalValue / coinValue;
            int toTake = Math.min(64, maxPossible);
            
            if (toTake > 0) {
                ItemStack result = new ItemStack(coinSlot.coinType, toTake);
                if (player.getInventory().add(result)) {
                    withdrawWithConversion(coinSlot.coinType, toTake);
                    updateAllSlots();
                    return result;
                }
            }
            return ItemStack.EMPTY;
        }
        
        // Shift-click from credit card slot - remove to inventory
        if (index == coinTypes.size()) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && moveItemStackTo(stack, coinTypes.size() + 1, slots.size(), true)) {
                slot.set(ItemStack.EMPTY);
                WalletItem.setCreditCard(wallet, ItemStack.EMPTY, player.level());
                return stack;
            }
            return ItemStack.EMPTY;
        }
        
        // Shift-click from player inventory - deposit to wallet
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        
        // Try to insert credit card
        if (stack.getItem() instanceof CreditCardItem) {
            Slot creditCardSlot = slots.get(coinTypes.size());
            if (!creditCardSlot.hasItem()) {
                creditCardSlot.set(stack.copy());
                slot.set(ItemStack.EMPTY);
                WalletItem.setCreditCard(wallet, stack, player.level());
                return stack;
            }
            return ItemStack.EMPTY;
        }
        
        // Try to deposit coins
        if (CurrencyValues.COIN_VALUES.containsKey(stack.getItem())) {
            WalletItem.addCoins(wallet, stack.getItem(), stack.getCount());
            slot.set(ItemStack.EMPTY);
            updateAllSlots();
            return stack.copy();
        }
        
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (walletSlot >= 0 && walletSlot < player.getInventory().getContainerSize()) {
            return player.getInventory().getItem(walletSlot) == wallet;
        }
        return player.getInventory().offhand.get(0) == wallet;
    }

    public int getTotalValue() {
        ItemStack currentWallet = getCurrentWallet();
        return WalletItem.getTotalValue(currentWallet);
    }

    public int getCoinCount(Item coinType) {
        ItemStack currentWallet = getCurrentWallet();
        return WalletItem.getCoinCount(currentWallet, coinType);
    }

    public ItemStack getCurrentWallet() {
        if (walletSlot >= 0 && walletSlot < playerInventory.getContainerSize()) {
            return playerInventory.getItem(walletSlot);
        }
        return playerInventory.offhand.get(0);
    }

    protected void addPlayerSlots(int x, int y) {
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, x + col * 18, y + row * 18));
        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
            this.addSlot(new Slot(playerInventory, hotbarSlot, x + hotbarSlot * 18, y + 58));
    }
    
    private void updateAllSlots() {
        for (Slot slot : this.slots) {
            if (slot instanceof CoinSlot) {
                slot.setChanged();
            }
        }
        this.broadcastChanges();
    }

    private class CoinSlot extends Slot {
        private final Item coinType;

        public CoinSlot(Item coinType, int x, int y) {
            super(new SimpleContainer(1), 0, x, y);
            this.coinType = coinType;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return CurrencyValues.COIN_VALUES.containsKey(stack.getItem());
        }

        @Override
        public void setByPlayer(ItemStack stack, ItemStack oldStack) {
            // Deposits handled in click logic only
        }

        @Override
        public boolean mayPickup(Player player) {
            if (CurrencyValues.COIN_VALUES != null) {
                int coinValue = CurrencyValues.COIN_VALUES.getOrDefault(coinType, 0);
                int totalValue = WalletItem.getTotalValue(wallet);
                return totalValue >= coinValue;
            }
            return false;
        }

        @Override
        public ItemStack safeInsert(ItemStack stack) {
            if (!stack.isEmpty() && CurrencyValues.COIN_VALUES.containsKey(stack.getItem())) {
                WalletItem.addCoins(wallet, stack.getItem(), stack.getCount());
                return ItemStack.EMPTY;
            }
            return stack;
        }

        @Override
        public ItemStack getItem() {
            return ItemStack.EMPTY; // Custom rendering handles display
        }

        @Override
        public ItemStack remove(int amount) {
            // Don't actually remove - let quickMoveStack handle it
            if (CurrencyValues.COIN_VALUES != null) {
                int coinValue = CurrencyValues.COIN_VALUES.getOrDefault(coinType, 0);
                int totalValue = WalletItem.getTotalValue(wallet);
                int maxPossible = totalValue / coinValue;
                if (maxPossible >= amount) {
                    return new ItemStack(coinType, amount);
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public void setChanged() {
            // No-op for display slots
        }
    }

    private boolean withdrawWithConversion(Item requestedCoin, int amount) {
        if (CurrencyValues.COIN_VALUES == null) return false;

        int requiredValue = CurrencyValues.COIN_VALUES.getOrDefault(requestedCoin, 0) * amount;
        int totalValue = WalletItem.getTotalValue(wallet);
        
        if (totalValue < requiredValue) return false;
        
        WalletItem.removeCoins(wallet, requestedCoin, amount);
        return true;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            collectCoinsFromInventory();
            // Update the wallet in player inventory
            if (walletSlot >= 0 && walletSlot < player.getInventory().getContainerSize()) {
                player.getInventory().setItem(walletSlot, wallet);
            } else {
                player.getInventory().offhand.set(0, wallet);
            }
            this.broadcastFullState();
            return true;
        }
        if (id == 1) {
            setUseCashFirst(!isUseCashFirst());
            this.broadcastChanges();
            return true;
        }
        return false;
    }

    public void collectCoinsFromInventory() {
        int playerSlotsStart = coinTypes.size() + 1;
        for (int i = playerSlotsStart; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && CurrencyValues.COIN_VALUES.containsKey(stack.getItem())) {
                WalletItem.addCoins(wallet, stack.getItem(), stack.getCount());
                slot.set(ItemStack.EMPTY);
            }
        }
        updateAllSlots();
    }

    public List<Item> getCoinTypes() {
        return coinTypes;
    }

    public ItemStack getCreditCard() {
        return creditCardContainer.getItem(0);
    }

    public boolean isUseCashFirst() {
        CompoundTag tag = wallet.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains("UseCashFirst")) {
            return true;
        }
        return tag.getBoolean("UseCashFirst");
    }

    public void setUseCashFirst(boolean value) {
        CompoundTag tag = wallet.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean("UseCashFirst", value);
        wallet.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        WalletItem.setCreditCard(wallet, creditCardContainer.getItem(0), player.level());
    }
}

