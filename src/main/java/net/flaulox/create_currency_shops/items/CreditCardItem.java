package net.flaulox.create_currency_shops.items;

import com.simibubi.create.AllSoundEvents;
import net.createmod.catnip.platform.CatnipServices;
import net.flaulox.create_currency_shops.blocks.CoinSafeBlockEntity;
import net.flaulox.create_currency_shops.network.CoinSafeBalancePacket;
import net.flaulox.create_currency_shops.util.ClientBalanceCache;
import net.flaulox.create_currency_shops.util.CoinSafeBalanceManager;
import net.flaulox.create_currency_shops.util.LogisticsHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.simibubi.create.foundation.item.ItemDescription;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.UUID;

public class CreditCardItem extends Item {
    public CreditCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isLinked(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack card = context.getItemInHand();

        if (level.isClientSide() || player == null) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CoinSafeBlockEntity coinSafe)) return InteractionResult.PASS;

        if (isLinked(card)) {
            player.displayClientMessage(Component.literal("§cCredit card is already linked!"), true);
            AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
            return InteractionResult.FAIL;
        }

        if (LogisticsHelper.isNetworkLocked(level, pos, player)) {
            return InteractionResult.FAIL;
        }

        linkToSafe(card, pos, coinSafe.getCustomName(), coinSafe.getSafeId());
        player.displayClientMessage(Component.literal("§aCredit card linked to " + coinSafe.getCustomName()), true);
        AllSoundEvents.CONFIRM.playOnServer(level, player.blockPosition());
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        UUID safeId = getNetworkId(stack);
        if (safeId != null && context.level() != null) {
            int balance = getTotalValue(stack, context.level());
            if (balance >= 0) {
                String formatted = String.format("%,d", balance).replace(',', '.');
                tooltipComponents.add(Component.literal("§7Balance: §6" + formatted));
            } else {
                tooltipComponents.add(Component.literal("§7Balance: §6-"));
            }
        } else {
            tooltipComponents.add(Component.literal("§7Balance: §6-"));
        }

        ItemDescription description = ItemDescription.create(this, FontHelper.Palette.STANDARD_CREATE);
        if (description != null) {
            tooltipComponents.addAll(description.getCurrentLines());
        }


        if (Screen.hasShiftDown() && isLinked(stack)) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            String safeName = tag.getString("SafeName");
            if (context.level() != null) {
                CoinSafeBlockEntity safe = getLinkedSafe(stack, context.level());
                if (safe != null) {
                    safeName = safe.getCustomName();
                }
            }
            tooltipComponents.add(3, Component.literal(""));
            tooltipComponents.add(4, Component.literal("§7Linked to: §6" + safeName));
        }
        
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public static void linkToSafe(ItemStack card, BlockPos pos, String safeName, UUID safeId) {
        CompoundTag tag = card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putLong("SafePos", pos.asLong());
        tag.putString("SafeName", safeName);
        tag.putUUID("SafeId", safeId);
        card.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static BlockPos getLinkedSafePos(ItemStack card) {
        CompoundTag tag = card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains("SafePos") ? BlockPos.of(tag.getLong("SafePos")) : null;
    }

    public static UUID getNetworkId(ItemStack card) {
        CompoundTag tag = card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains("SafeId") ? tag.getUUID("SafeId") : null;
    }

    public static boolean isLinked(ItemStack card) {
        UUID safeId = getNetworkId(card);
        if (safeId == null) return false;
        
        int balance = ClientBalanceCache.getBalance(safeId);
        if (balance < 0) {
            card.remove(DataComponents.CUSTOM_DATA);
            return false;
        }
        return getLinkedSafePos(card) != null;
    }

    public static CoinSafeBlockEntity getLinkedSafe(ItemStack card, Level level) {
        BlockPos pos = getLinkedSafePos(card);
        if (pos == null) return null;
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof CoinSafeBlockEntity ? (CoinSafeBlockEntity) be : null;
    }

    public static int getTotalValue(ItemStack card, Level level) {
        UUID safeId = getNetworkId(card);
        if (safeId == null) return -1;
        
        if (level.isClientSide()) {
            return ClientBalanceCache.getBalance(safeId);
        }
        return CoinSafeBalanceManager.getBalance((ServerLevel) level, safeId);
    }

    public static boolean removeTotalValue(ItemStack card, Level level, int amount) {
        if (level.isClientSide()) return false;
        UUID safeId = getNetworkId(card);
        if (safeId == null) return false;
        
        CoinSafeBalanceManager.removeBalance((ServerLevel) level, safeId, amount);
        
        int newBalance = CoinSafeBalanceManager.getBalance((ServerLevel) level, safeId);
        java.util.Map<UUID, Integer> balances = new java.util.HashMap<>();
        balances.put(safeId, newBalance);
        CoinSafeBalancePacket packet =
            new CoinSafeBalancePacket(balances);
        CatnipServices.NETWORK.sendToAllClients(packet);
        
        return true;
    }
}