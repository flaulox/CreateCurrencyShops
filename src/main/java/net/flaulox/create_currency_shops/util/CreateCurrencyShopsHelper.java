package net.flaulox.create_currency_shops.util;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import net.flaulox.create_currency_shops.CreateCurrencyShopsItems;
import net.flaulox.create_currency_shops.items.CreditCardItem;
import net.flaulox.create_currency_shops.items.WalletItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.stream.Collectors;

public class CreateCurrencyShopsHelper {

    public static int getValue(ItemStack stack) {
        return CurrencyValues.COIN_VALUES.getOrDefault(stack.getItem(), -1);
    }

    public static int countValueInInventory(Inventory inventory) {
        int total = 0;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty())
                continue;

            int valuePerItem = getValue(stack);
            if (valuePerItem > 0) {
                total += valuePerItem * stack.getCount();
            }
        }
        return total;
    }

    public static int countTotalValueInInventory(Inventory inventory, Level level) {
        int total = 0;
        total += countValueInInventory(inventory);
        Set<UUID> processedSafes = new HashSet<>();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() == CreateCurrencyShopsItems.WALLET.get()) {
                total += WalletItem.getTotalValue(stack);

                if (WalletItem.hasCreditCard(stack)) {
                    UUID safeId = CreditCardItem.getNetworkId(WalletItem.getCreditCard(stack, level));
                    if (safeId != null && !processedSafes.contains(safeId)) {
                        total += CreditCardItem.getTotalValue(WalletItem.getCreditCard(stack, level), level);
                        processedSafes.add(safeId);
                    }
                }
            }
        }

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() == CreateCurrencyShopsItems.CREDIT_CARD.get()) {
                UUID safeId = CreditCardItem.getNetworkId(stack);
                if (safeId != null && !processedSafes.contains(safeId)) {
                    total += CreditCardItem.getTotalValue(stack, level);
                    processedSafes.add(safeId);
                }
            }
        }
        return total;
    }

    public static InventorySummary coinSummaryFromValue(int value) {
        InventorySummary paymentEntries = new InventorySummary();
        List<Map.Entry<Item, Integer>> sortedCoinValues = sortCoinValues().reversed();
        int remainingValue = value;

        for (Map.Entry<Item, Integer> coinValue : sortedCoinValues) {
            int coinValueInt = coinValue.getValue();
            int count = remainingValue / coinValueInt;
            if (count > 0) {
                paymentEntries.add(new ItemStack(coinValue.getKey(), count));
                remainingValue -= count * coinValueInt;
            }
            if (remainingValue == 0) {
                break;
            }
        }

        return paymentEntries;
    }


    public static List<Map.Entry<Item, Integer>> sortCoinValues() {
        return  CurrencyValues.COIN_VALUES.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toList());
    }

    public static int countValueInInventorySummary(InventorySummary summary) {
        int value = 0;
        for (BigItemStack stack : summary.getStacks()) {
            value += getValue(stack.stack) * stack.count;
        }
        return value;
    }

    public static Pair<InventorySummary, Integer> processPayment(Player player, int targetValue, Level level) {
        InventorySummary paymentEntries = new InventorySummary();

        Pair<InventorySummary, Integer> processedInventoryPayment = processInventoryPayment(player, targetValue);


        paymentEntries.add(processedInventoryPayment.getFirst());
        targetValue = targetValue - countValueInInventorySummary(paymentEntries);
        if (targetValue <= 0) {return Pair.of(paymentEntries, processedInventoryPayment.getSecond());}
        targetValue = processWalletPayment(player, targetValue, level);
        if (targetValue <= 0) {return Pair.of(paymentEntries, processedInventoryPayment.getSecond());}
        processCardPayment(player, targetValue, level);

        System.out.println(targetValue);
        System.out.println(countValueInInventorySummary(paymentEntries));
        return Pair.of(paymentEntries, processedInventoryPayment.getSecond());
    }

    public static int processWalletPayment(Player player, int targetValue, Level level) {
        if (level.isClientSide()) return targetValue;
        Inventory inventory = player.getInventory();
        int rest = targetValue;
        Set<UUID> processedSafes = new HashSet<>();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() == CreateCurrencyShopsItems.WALLET.get()) {

                if (WalletItem.getUseCashFirst(stack,level)) {
                    int walletValueBefore = WalletItem.getTotalValue(stack);
                    WalletItem.removeTotalValue(stack, rest);
                    int walletValueAfter = WalletItem.getTotalValue(stack);
                    rest -= (walletValueBefore - walletValueAfter);

                    if (WalletItem.hasCreditCard(stack)) {
                        ItemStack card = WalletItem.getCreditCard(stack, level);
                        UUID safeId = CreditCardItem.getNetworkId(card);
                        if (safeId != null && !processedSafes.contains(safeId)) {
                            int cardValueBefore = CreditCardItem.getTotalValue(card, level);
                            CreditCardItem.removeTotalValue(card, level, rest);
                            int cardValueAfter = CreditCardItem.getTotalValue(card, level);
                            rest -= (cardValueBefore - cardValueAfter);
                            processedSafes.add(safeId);
                        }
                    }
                } else {
                    if (WalletItem.hasCreditCard(stack)) {
                        ItemStack card = WalletItem.getCreditCard(stack, level);
                        UUID safeId = CreditCardItem.getNetworkId(card);
                        if (safeId != null && !processedSafes.contains(safeId)) {
                            int cardValueBefore = CreditCardItem.getTotalValue(card, level);
                            CreditCardItem.removeTotalValue(card, level, rest);
                            int cardValueAfter = CreditCardItem.getTotalValue(card, level);
                            rest -= (cardValueBefore - cardValueAfter);
                            processedSafes.add(safeId);
                        }
                    }
                    int walletValueBefore = WalletItem.getTotalValue(stack);
                    WalletItem.removeTotalValue(stack, rest);
                    int walletValueAfter = WalletItem.getTotalValue(stack);
                    rest -= (walletValueBefore - walletValueAfter);
                }


            }
        }
        return rest;
    }

    public static void processCardPayment(Player player, int targetValue, Level level) {
        if (level.isClientSide()) return;
        Inventory inventory = player.getInventory();
        Set<UUID> processedSafes = new HashSet<>();
        int remaining = targetValue;

        for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() == CreateCurrencyShopsItems.CREDIT_CARD.get()) {
                UUID safeId = CreditCardItem.getNetworkId(stack);
                if (safeId != null && !processedSafes.contains(safeId)) {
                    int before = CreditCardItem.getTotalValue(stack, level);
                    CreditCardItem.removeTotalValue(stack, level, remaining);
                    int after = CreditCardItem.getTotalValue(stack, level);
                    remaining -= (before - after);
                    processedSafes.add(safeId);
                }
            }
        }
    }

    public static Pair<InventorySummary, Integer> processInventoryPayment(Player player, int targetValue) {
        InventorySummary paymentEntries = new InventorySummary();
        Inventory inventory = player.getInventory();
        

        // create coinsAvailable List from Coins in players Inventory
        List<Map.Entry<Item, Integer>> coinsAvailable = new ArrayList<>();
        for (Map.Entry<Item, Integer> coinValue : sortCoinValues()) {
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack.getItem() == coinValue.getKey()) {
                    for (int j = 0; j < stack.getCount(); j++) {
                        coinsAvailable.add(coinValue);
                    }
                }
            }
        }

        // move forward in coinsAvailable until iterated Value is greater than targetValue
        int iteratedValue = 0;
        int index = -1;
        while (iteratedValue < targetValue && index + 1 < coinsAvailable.size()) {
            index++;
            iteratedValue += coinsAvailable.get(index).getValue();
        }



        // move backwards in coinsAvailable from index
        iteratedValue = 0;
        while (iteratedValue < targetValue && index >= 0) {
            iteratedValue += coinsAvailable.get(index).getValue();
            paymentEntries.add(new ItemStack(coinsAvailable.get(index).getKey(), 1));
            index--;
        }
        int difference = iteratedValue - targetValue;

        return Pair.of(paymentEntries, difference);
    }

    //places Change directly into Players Inventory
    public static void processChange(Player player, int totalAmount) {
        Inventory inventory = player.getInventory();
        List<Map.Entry<Item, Integer>> sortedCoinValues = sortCoinValues();
        if (totalAmount <= 0) {return;}
//        Item lowestDenomination = sortedCoinValues.getFirst().getKey();
        int index = sortedCoinValues.size() - 1;
        while (totalAmount > 0 && index >= 0) {


            int amount = totalAmount;
            for (int i = 0; i < amount / sortedCoinValues.get(index).getValue(); i++) {
                boolean changeAdded = false;
                for (int j = 0; j < inventory.getContainerSize(); j++) {
                    ItemStack stack = inventory.getItem(j);
                    if (stack.getItem() == sortedCoinValues.get(index).getKey() && stack.getCount() < stack.getMaxStackSize()) {
                        stack.setCount(stack.getCount() + 1);
                        changeAdded = true;
                        totalAmount -= sortedCoinValues.get(index).getValue();
                        break;
                    }
                }
                if (!changeAdded) {
                    if (!inventory.add(new ItemStack(sortedCoinValues.get(index).getKey()))) {
                        if (!player.level().isClientSide) {
                            player.level().addFreshEntity(new ItemEntity(
                                    player.level(),
                                    player.getX(),
                                    player.getY() + 1,
                                    player.getZ(),
                                    new ItemStack(sortedCoinValues.get(index).getKey())
                            ));
                        }
                    }
                    totalAmount -= sortedCoinValues.get(index).getValue();
                }
            }
            index--;

        }
    }

    public static InventorySummary reducePayment(List<BigItemStack> given, int change) {
        List<Map.Entry<Item, Integer>> coinsAvailable = new ArrayList<>();
        InventorySummary result = new InventorySummary();
        for (BigItemStack stack: given) {
            int isNoCoin = 0;
            for (Map.Entry<Item, Integer> coinValue : sortCoinValues()) {
                if (stack.stack.getItem() == coinValue.getKey()) {
//                    for (int j = 0; j < stack.stack.getCount(); j++) {
                    for (int j = 0; j < stack.count; j++) {
                        coinsAvailable.add(coinValue);
                    }
                } else {
                    isNoCoin++;
                }
            }
            if (isNoCoin >= sortCoinValues().size()) {
                result.add(stack);

            }

        }

        int iteratedValue = 0;
        int index = -1;
        while (iteratedValue < change) {
            index++;
            iteratedValue += coinsAvailable.get(index).getValue();
        }

        // move backwards in coinsAvailable from index
        iteratedValue = 0;
        while (iteratedValue < change) {
            iteratedValue += coinsAvailable.get(index).getValue();
            coinsAvailable.remove(coinsAvailable.get(index));
            index--;
        }
        int difference = iteratedValue - change;

        List<Map.Entry<Item, Integer>> sortedCoinValues = sortCoinValues().reversed();

        while (difference > 0) {
            for (Map.Entry<Item, Integer> coinValue : sortedCoinValues) {
                while (difference / coinValue.getValue() > 0) {
                    coinsAvailable.add(coinValue);
                    difference -= coinValue.getValue();
                }
            }
        }



        for (Map.Entry<Item, Integer> coinValue : coinsAvailable) {
            result.add(new ItemStack(coinValue.getKey(), 1));
        }

        return result;
    }


    public static List<ItemStack> inventorySummaryToItemStacks(InventorySummary summary) {
        List<ItemStack> result = new ArrayList<>();

        for (BigItemStack bigStack : summary.getStacks()) {
            ItemStack base = bigStack.stack;
            int remaining = bigStack.count;
            int maxStackSize = base.getMaxStackSize();

            while (remaining > 0) {
                int toTake = Math.min(remaining, maxStackSize);
                result.add(base.copyWithCount(toTake));
                remaining -= toTake;
            }
        }

        return result;
    }





    public static boolean isValidCurrency(ItemStack stack) {
        return CurrencyValues.COIN_VALUES.containsKey(stack.getItem());
    }

    public static int calculateTotalValue(Map<Item, Integer> coinCounts) {
        if (CurrencyValues.COIN_VALUES == null || coinCounts == null) return 0;
        int total = 0;
        for (Map.Entry<Item, Integer> entry : coinCounts.entrySet()) {
            Integer value = CurrencyValues.COIN_VALUES.get(entry.getKey());
            if (value != null) {
                total += entry.getValue() * value;
            }
        }
        return total;
    }


}
