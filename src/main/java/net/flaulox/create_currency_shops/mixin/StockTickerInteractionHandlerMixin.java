package net.flaulox.create_currency_shops.mixin;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;
import net.flaulox.create_currency_shops.CreateCurrencyShopsItems;
import net.flaulox.create_currency_shops.util.CreateCurrencyShopsHelper;
import net.flaulox.create_currency_shops.items.CreditCardItem;
import net.flaulox.create_currency_shops.items.WalletItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;


@Mixin(StockTickerInteractionHandler.class)
public abstract class StockTickerInteractionHandlerMixin {


    @Inject(
            method = "interactWithShop",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void CurrencyInteractWithShop(Player player, Level level, BlockPos targetPos, ItemStack mainHandItem, CallbackInfo ci) {
        if (level.isClientSide())
            return;
        if (!(level.getBlockEntity(targetPos) instanceof StockTickerBlockEntity tickerBE))
            return;

        ShoppingListItem.ShoppingList list = ShoppingListItem.getList(mainHandItem);
        if (list == null)
            return;

        if (!tickerBE.behaviour.freqId.equals(list.shopNetwork())) {
            AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
            CreateLang.translate("stock_keeper.wrong_network")
                    .style(ChatFormatting.RED)
                    .sendStatus(player);
            return;
        }

        Couple<InventorySummary> bakeEntries = list.bakeEntries(level, null);
        InventorySummary paymentEntries = bakeEntries.getSecond();
        InventorySummary orderEntries = bakeEntries.getFirst();
        PackageOrder order = new PackageOrder(orderEntries.getStacksByCount());

        // Must be up-to-date
        tickerBE.getAccurateSummary();

        // Check stock levels
        InventorySummary recentSummary = tickerBE.getRecentSummary();
        for (BigItemStack entry : order.stacks()) {
            if (recentSummary.getCountOf(entry.stack) >= entry.count)
                continue;

            AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
            CreateLang.translate("stock_keeper.stock_level_too_low")
                    .style(ChatFormatting.RED)
                    .sendStatus(player);
            return;
        }

        // Check space in stock ticker
        int requiredValue = 0;
        for (BigItemStack stack : paymentEntries.getStacks()) {
            if (stack.stack.is(CreateCurrencyShopsItems.CURRENCY_ITEM)) {
                requiredValue += stack.count;
                paymentEntries.erase(stack.stack);
            }
        }


        InventorySummary transferredPaymentEntries = paymentEntries.copy();
        transferredPaymentEntries.add(CreateCurrencyShopsHelper.coinSummaryFromValue(requiredValue));






        int occupiedSlots = 0;
        for (BigItemStack entry : transferredPaymentEntries.getStacksByCount())
            occupiedSlots += Mth.ceil(entry.count / (float) entry.stack.getMaxStackSize());
        for (int i = 0; i < ((StockTickerBlockEntityAccessor) tickerBE).getReceivedPayments().getSlots(); i++)
            if (((StockTickerBlockEntityAccessor) tickerBE).getReceivedPayments().getStackInSlot(i)
                    .isEmpty())
                occupiedSlots--;

        if (occupiedSlots > 0) {
            AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
            CreateLang.translate("stock_keeper.cash_register_full")
                    .style(ChatFormatting.RED)
                    .sendStatus(player);
            return;
        }


        int change = 0;

        if (player.getOffhandItem().getItem() == CreateCurrencyShopsItems.WALLET.asItem()) {
            ItemStack item = player.getOffhandItem();
            int walletTotal = WalletItem.getTotalValue(item);
            if (WalletItem.hasCreditCard(item)) {
                walletTotal += CreditCardItem.getTotalValue(WalletItem.getCreditCard(item, level), level);
            }
            if (walletTotal >= requiredValue) {
                CreateCurrencyShopsHelper.processWalletPayment(player, requiredValue, level);
            } else {
                paymentEntries.add(new ItemStack(CreateCurrencyShopsItems.CURRENCY_ITEM.asItem()));
                ci.cancel();
            }
        }

        else if (player.getOffhandItem().getItem() == CreateCurrencyShopsItems.CREDIT_CARD.asItem()) {
            ItemStack item = player.getOffhandItem();
            if (CreditCardItem.getTotalValue(item, level) >= requiredValue) {
                CreateCurrencyShopsHelper.processCardPayment(player, requiredValue, level);
            } else {
                paymentEntries.add(new ItemStack(CreateCurrencyShopsItems.CURRENCY_ITEM.asItem()));
                ci.cancel();
            }
        }






        else if (CreateCurrencyShopsHelper.countTotalValueInInventory(player.getInventory(), player.level()) >= requiredValue) {
            Pair<InventorySummary, Integer> payment = CreateCurrencyShopsHelper.processPayment(player, requiredValue, level);
            paymentEntries.add(payment.getFirst());
            change = payment.getSecond();
        } else {
            paymentEntries.add(new ItemStack(CreateCurrencyShopsItems.CURRENCY_ITEM.asItem()));
            ci.cancel();
        }








        // Transfer payment to stock ticker
        for (boolean simulate : Iterate.trueAndFalse) {
            InventorySummary tally = paymentEntries.copy();
            List<ItemStack> toTransfer = new ArrayList<>();

            for (int i = 0; i < player.getInventory().items.size(); i++) {
                ItemStack item = player.getInventory()
                        .getItem(i);
                if (item.isEmpty())
                    continue;
                int countOf = tally.getCountOf(item);
                if (countOf == 0)
                    continue;
                int toRemove = Math.min(item.getCount(), countOf);
                tally.add(item, -toRemove);

                if (simulate)
                    continue;

                int newStackSize = item.getCount() - toRemove;
                player.getInventory()
                        .setItem(i, newStackSize == 0 ? ItemStack.EMPTY : item.copyWithCount(newStackSize));
                toTransfer.add(item.copyWithCount(toRemove));
            }

            if (simulate && tally.getTotalCount() != 0) {
                AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
                CreateLang.translate("stock_keeper.too_broke")
                        .style(ChatFormatting.RED)
                        .sendStatus(player);
                return;
            }

            if (simulate)
                continue;

            System.out.println();
            toTransfer.clear();
            toTransfer.addAll(CreateCurrencyShopsHelper.inventorySummaryToItemStacks(transferredPaymentEntries));
            System.out.println(toTransfer);
            toTransfer.forEach(s -> ItemHandlerHelper.insertItemStacked(((StockTickerBlockEntityAccessor) tickerBE).getReceivedPayments(), s, false));
        }

        tickerBE.broadcastPackageRequest(LogisticallyLinkedBehaviour.RequestType.PLAYER, order, null, ShoppingListItem.getAddress(mainHandItem));
        CreateCurrencyShopsHelper.processChange(player, change);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        if (!order.isEmpty())
            AllSoundEvents.STOCK_TICKER_TRADE.playOnServer(level, tickerBE.getBlockPos());
        ci.cancel();
    }



}


