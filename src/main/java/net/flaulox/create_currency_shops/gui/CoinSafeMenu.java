package net.flaulox.create_currency_shops.gui;

import com.simibubi.create.foundation.blockEntity.behaviour.animatedContainer.AnimatedContainerBehaviour;
import com.simibubi.create.foundation.gui.menu.MenuBase;
import net.flaulox.create_currency_shops.items.CoinDraftItem;
import net.flaulox.create_currency_shops.util.CurrencyValues;
import net.flaulox.create_currency_shops.CreateCurrencyShopsMenus;
import net.flaulox.create_currency_shops.blocks.CoinSafeBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public class CoinSafeMenu extends MenuBase<CoinSafeBlockEntity> {
    private List<Item> coinTypes;

    public CoinSafeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        super(CreateCurrencyShopsMenus.COIN_SAFE.get(), containerId, playerInventory, extraData);
    }

    public CoinSafeMenu(int containerId, Inventory playerInventory, CoinSafeBlockEntity safeEntity) {
        super(CreateCurrencyShopsMenus.COIN_SAFE.get(), containerId, playerInventory, safeEntity);
        var behaviour = contentHolder.getBehaviour(AnimatedContainerBehaviour.TYPE);
        if (behaviour != null)
            behaviour.startOpen(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        var behaviour = contentHolder.getBehaviour(AnimatedContainerBehaviour.TYPE);
        if (behaviour != null)
            behaviour.stopOpen(player);
    }

    @Override
    protected CoinSafeBlockEntity createOnClient(RegistryFriendlyByteBuf buffer) {
        return (CoinSafeBlockEntity) player.level().getBlockEntity(buffer.readBlockPos());
    }

    @Override
    protected void initAndReadInventory(CoinSafeBlockEntity contentHolder) {
        this.coinTypes = CurrencyValues.COIN_VALUES.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    protected void addSlots() {
        int x = 98;
        int y = 26;
        int[] xOffsets = {x, x + 40, x - 15, x + 55, x, x + 40};
        int[] yOffsets = {y, y, y + 33, y + 33, y + 66, y + 66};
        

            for (int i = 0; i < Math.min(coinTypes.size(), 6); i++) {
                this.addSlot(new CoinSlot(coinTypes.get(i), xOffsets[i], yOffsets[i]));
            }


        addPlayerSlots(20, 165);
    }

    @Override
    protected void saveData(CoinSafeBlockEntity contentHolder) {}

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (contentHolder.isLocked() && !contentHolder.isAdmin(player)) {
            player.closeContainer();
        }
    }

    @Override
    public boolean canDragTo(Slot slot) {
        if (slot instanceof CoinSlot) {
            ItemStack carried = getCarried();
            return carried.getItem() instanceof CoinDraftItem || CurrencyValues.COIN_VALUES.containsKey(carried.getItem());
        }
        return super.canDragTo(slot);
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
                } else if (carried.getItem() instanceof CoinDraftItem) {
                    java.util.UUID writtenTo = CoinDraftItem.getWrittenTo(carried);
                    java.util.UUID issuedFrom = CoinDraftItem.getIssuedFrom(carried);
                    java.util.UUID thisSafeId = contentHolder.getSafeId();
                    java.util.UUID anyUUID = new java.util.UUID(0, 0);
                    
                    if (thisSafeId.equals(writtenTo) || thisSafeId.equals(issuedFrom) || anyUUID.equals(writtenTo)) {
                        int value = CoinDraftItem.getValue(carried);
                        contentHolder.addValue(value);
                        carried.shrink(1);
                        updateAllSlots();
                    }
                    return;
                } else if (CurrencyValues.COIN_VALUES.containsKey(carried.getItem())) {
                    int amount = button == 0 ? carried.getCount() : 1;
                    contentHolder.addCoins(carried.getItem(), amount);
                    carried.shrink(amount);
                    updateAllSlots();
                    return;
                }
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    private void handleCoinSlotClick(CoinSlot slot, int button, ClickType clickType, Player player) {
        if (clickType != ClickType.PICKUP) return;
        
        int coinValue = CurrencyValues.COIN_VALUES.getOrDefault(slot.coinType, 0);
        int totalValue = contentHolder.getTotalValue();
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
                    contentHolder.addCoins(slot.coinType, cursor.getCount() + toTake - newCount);
                }
            } else {
                contentHolder.addCoins(slot.coinType, toTake);
            }
            updateAllSlots();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null) return ItemStack.EMPTY;
        
        if (index < coinTypes.size() && slot instanceof CoinSlot coinSlot) {
            if (player.getInventory().getFreeSlot() == -1) {
                return ItemStack.EMPTY;
            }
            
            int coinValue = CurrencyValues.COIN_VALUES.getOrDefault(coinSlot.coinType, 0);
            int totalValue = contentHolder.getTotalValue();
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
        
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        
        if (stack.getItem() instanceof CoinDraftItem) {
            java.util.UUID writtenTo = CoinDraftItem.getWrittenTo(stack);
            java.util.UUID issuedFrom = CoinDraftItem.getIssuedFrom(stack);
            java.util.UUID thisSafeId = contentHolder.getSafeId();
            java.util.UUID anyUUID = new java.util.UUID(0, 0);
            
            if (thisSafeId.equals(writtenTo) || thisSafeId.equals(issuedFrom) || anyUUID.equals(writtenTo)) {
                int value = CoinDraftItem.getValue(stack);
                contentHolder.addValue(value);
                slot.set(ItemStack.EMPTY);
                updateAllSlots();
                return stack.copy();
            }
            return ItemStack.EMPTY;
        }
        
        if (CurrencyValues.COIN_VALUES.containsKey(stack.getItem())) {
            contentHolder.addCoins(stack.getItem(), stack.getCount());
            slot.set(ItemStack.EMPTY);
            updateAllSlots();
            return stack.copy();
        }
        
        return ItemStack.EMPTY;
    }

    public int getTotalValue() {
        return contentHolder.getTotalValue();
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
        public boolean mayPickup(Player player) {
            int coinValue = CurrencyValues.COIN_VALUES.getOrDefault(coinType, 0);
            int totalValue = contentHolder.getTotalValue();
            return totalValue >= coinValue;
        }

        @Override
        public ItemStack safeInsert(ItemStack stack) {
            return stack;
        }

        @Override
        public ItemStack getItem() {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack remove(int amount) {
            int coinValue = CurrencyValues.COIN_VALUES.getOrDefault(coinType, 0);
            int totalValue = contentHolder.getTotalValue();
            int maxPossible = totalValue / coinValue;
            if (maxPossible >= amount) {
                return new ItemStack(coinType, amount);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public void setChanged() {}
    }

    private boolean withdrawWithConversion(Item requestedCoin, int amount) {
        int requiredValue = CurrencyValues.COIN_VALUES.getOrDefault(requestedCoin, 0) * amount;
        int totalValue = contentHolder.getTotalValue();
        
        if (totalValue < requiredValue) return false;
        
        contentHolder.removeValue(requiredValue);
        
        return true;
    }

    public List<Item> getCoinTypes() {
        return coinTypes;
    }

    public void collectFromInventory() {
        for (int i = coinTypes.size(); i < slots.size(); i++) {
            Slot slot = slots.get(i);
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof CoinDraftItem) {
                    java.util.UUID writtenTo = CoinDraftItem.getWrittenTo(stack);
                    java.util.UUID issuedFrom = CoinDraftItem.getIssuedFrom(stack);
                    java.util.UUID thisSafeId = contentHolder.getSafeId();
                    java.util.UUID anyUUID = new java.util.UUID(0, 0);
                    
                    if (thisSafeId.equals(writtenTo) || thisSafeId.equals(issuedFrom) || anyUUID.equals(writtenTo)) {
                        int value = CoinDraftItem.getValue(stack);
                        contentHolder.addValue(value * stack.getCount());
                        slot.set(ItemStack.EMPTY);
                    }
                } else if (CurrencyValues.COIN_VALUES.containsKey(stack.getItem())) {
                    contentHolder.addCoins(stack.getItem(), stack.getCount());
                    slot.set(ItemStack.EMPTY);
                }
            }
        }
        contentHolder.setChanged();
        if (contentHolder.getLevel() != null && !contentHolder.getLevel().isClientSide) {
            contentHolder.getLevel().sendBlockUpdated(contentHolder.getBlockPos(), contentHolder.getBlockState(), contentHolder.getBlockState(), 3);
        }
        updateAllSlots();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            collectFromInventory();
            return true;
        } else if (id == 1) {
            return true;
        }
        return false;
    }
}
