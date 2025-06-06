package net.flaulox.create_currency_shops.items;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.item.ItemDescription;
import net.createmod.catnip.lang.FontHelper;
import net.flaulox.create_currency_shops.blocks.CoinSafeBlockEntity;
import net.flaulox.create_currency_shops.util.CurrencyValues;
import net.flaulox.create_currency_shops.gui.WalletMenu;
import net.flaulox.create_currency_shops.util.LogisticsHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

public class WalletItem extends Item {
    public WalletItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack wallet = player.getItemInHand(hand);



        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : -1;
            serverPlayer.openMenu(new WalletMenuProvider(wallet), buf -> buf.writeInt(slot));
        }
        player.playSound(SoundEvents.ARMOR_EQUIP_CHAIN.value(), 0.5F, 1.0F);
        return InteractionResultHolder.sidedSuccess(wallet, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack wallet = context.getItemInHand();



        if (level.getBlockEntity(pos) instanceof CoinSafeBlockEntity coinSafe) {

            if (LogisticsHelper.isNetworkLocked(level, pos, player)) {
                player.displayClientMessage(Component.translatable("create.logistically_linked.protected").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            int walletValue = getTotalValue(wallet);
            if (walletValue > 0) {
                coinSafe.addValue(walletValue);
                removeTotalValue(wallet, walletValue);
                player.displayClientMessage(Component.literal(String.format("%,d", walletValue) + " " + Component.translatable("create_currency_shops.wallet.deposited").getString()), true);
                player.playSound(SoundEvents.ARMOR_EQUIP_CHAIN.value());
                player.playSound(AllSoundEvents.ITEM_HATCH.getMainEvent(), 0.5f, 1f);
                return InteractionResult.SUCCESS;
            }
        }

        if (level.isClientSide() || player == null) return InteractionResult.PASS;

        return InteractionResult.PASS;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack wallet, ItemStack coins, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !CurrencyValues.COIN_VALUES.containsKey(coins.getItem())) {
            return false;
        }
        
        addCoins(wallet, coins.getItem(), coins.getCount());
        access.set(ItemStack.EMPTY);
        player.playSound(SoundEvents.BUNDLE_INSERT);
        return true;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack wallet, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || !slot.hasItem()) {
            return false;
        }
        
        ItemStack slotStack = slot.getItem();
        if (!CurrencyValues.COIN_VALUES.containsKey(slotStack.getItem())) {
            return false;
        }
        
        addCoins(wallet, slotStack.getItem(), slotStack.getCount());
        slot.set(ItemStack.EMPTY);
        player.playSound(SoundEvents.BUNDLE_INSERT);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int totalValue = getTotalValue(stack);
        tooltipComponents.add(Component.translatable("gui.create_currency_shops.total").withStyle(ChatFormatting.GRAY).append(Component.literal(" " + String.format("%,d", totalValue )).withStyle(ChatFormatting.GOLD)));
        if (WalletItem.hasCreditCard(stack)) {
            tooltipComponents.add(Component.translatable("gui.create_currency_shops.credit_card").withStyle(ChatFormatting.GRAY).append(Component.literal(" " + String.format("%,d", CreditCardItem.getTotalValue(getCreditCard(stack, context.level()), context.level()))).withStyle(ChatFormatting.GOLD)));
        }

        ItemDescription description = ItemDescription.create(this, FontHelper.Palette.STANDARD_CREATE);
        if (description != null) {
            tooltipComponents.addAll(description.getCurrentLines());
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public static void addCoins(ItemStack wallet, Item coinType, int amount) {
        int value = CurrencyValues.COIN_VALUES.getOrDefault(coinType, 0) * amount;
        CompoundTag tag = wallet.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("TotalValue", tag.getInt("TotalValue") + value);
        wallet.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void removeCoins(ItemStack wallet, Item coinType, int amount) {
        int value = CurrencyValues.COIN_VALUES.getOrDefault(coinType, 0) * amount;
        CompoundTag tag = wallet.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("TotalValue", Math.max(0, tag.getInt("TotalValue") - value));
        wallet.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int getCoinCount(ItemStack wallet, Item coinType) {
        return 0; // Not stored individually
    }

    public static int getTotalValue(ItemStack wallet) {
        CompoundTag tag = wallet.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getInt("TotalValue");
    }

    public static void removeTotalValue(ItemStack wallet, int value) {
        CompoundTag tag = wallet.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("TotalValue", Math.max(0, tag.getInt("TotalValue") - value));
        wallet.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static class WalletMenuProvider implements MenuProvider {
        private final ItemStack wallet;

        public WalletMenuProvider(ItemStack wallet) {
            this.wallet = wallet;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("item.create_currency_shops.wallet");
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
            return new WalletMenu(containerId, playerInventory, wallet);
        }
    }

    public static boolean getUseCashFirst(ItemStack wallet, Level level) {
        CompoundTag tag = wallet.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("UseCashFirst")) {
            return tag.getBoolean("UseCashFirst");

        }
        return false;
    }

    public static ItemStack getCreditCard(ItemStack wallet, Level level) {
        CompoundTag tag = wallet.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("CreditCard")) {
            return ItemStack.parseOptional(level.registryAccess(), tag.getCompound("CreditCard"));
        }
        return ItemStack.EMPTY;
    }


    public static void setCreditCard(ItemStack wallet, ItemStack card, Level level) {
        if (wallet.isEmpty()) return;
        CompoundTag tag = wallet.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (card.isEmpty()) {
            tag.remove("CreditCard");
        } else {
            tag.put("CreditCard", card.save(level.registryAccess()));
        }
        wallet.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean hasCreditCard(ItemStack wallet) {
        CompoundTag tag = wallet.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains("CreditCard");
    }
}