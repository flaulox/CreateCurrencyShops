package net.flaulox.create_currency_shops.blocks;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlock;
import net.createmod.catnip.platform.CatnipServices;
import net.flaulox.create_currency_shops.CreateCurrencyShopsBlocks;
import net.flaulox.create_currency_shops.CreateCurrencyShopsSoundEvents;
import net.flaulox.create_currency_shops.network.CoinSafeBalancePacket;
import net.flaulox.create_currency_shops.util.CoinSafeSavedData;
import net.flaulox.create_currency_shops.util.CurrencyValues;
import net.flaulox.create_currency_shops.util.CreateCurrencyShopsHelper;
import net.flaulox.create_currency_shops.gui.CoinSafeMenu;
import net.flaulox.create_currency_shops.util.CoinSafeBalanceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.animatedContainer.AnimatedContainerBehaviour;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;

import javax.annotation.Nullable;
import java.util.List;

public class CoinSafeBlockEntity extends SmartBlockEntity implements MenuProvider, CoinSafeClipboardCloneable {

    public LerpedFloat door = LerpedFloat.linear().startWithValue(0);
    public LerpedFloat wheel = LerpedFloat.linear().startWithValue(0);
    public AnimatedContainerBehaviour<CoinSafeMenu> openTracker;
    private boolean wheelSoundPlayed = false;
    private boolean doorSoundPlayed = false;

    private int totalValue = 0;
    private final CoinSafeItemHandler itemHandler = new CoinSafeItemHandler(this);
    public LogisticallyLinkedBehaviour behaviour;
    private String customName = "";
    private java.util.UUID safeId = java.util.UUID.randomUUID();
    public java.util.UUID ownerUUID = null;
    private boolean locked = false;

    public CoinSafeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(behaviour = new LogisticallyLinkedBehaviour(this, false));
        behaviours.add(openTracker = new AnimatedContainerBehaviour<>(this, CoinSafeMenu.class));
    }

    @Override
    public void tick() {
        super.tick();
        
        float targetWheel = openTracker.openCount > 0 ? 1 : 0;
        float targetDoor = openTracker.openCount > 0 ? 1 : 0;
        
        if (openTracker.openCount > 0) {
            wheel.chase(targetWheel, 0.2f, Chaser.LINEAR);
            if (wheel.getValue() > 0.8f) {
                door.chase(targetDoor, 0.2f, Chaser.LINEAR);
            }
        } else {
            door.chase(targetDoor, 0.2f, Chaser.LINEAR);
            if (door.getValue() < 0.2f) {
                wheel.chase(targetWheel, 0.2f, Chaser.LINEAR);
            }
        }
        
        playSounds();
        wheel.tickChaser();
        door.tickChaser();
    }

    private void playSounds() {
        if (level.isClientSide) return;
        
        if (openTracker.openCount > 0) {
            if (wheel.getValue() > 0.1f && !wheelSoundPlayed) {
                level.playSound(null, worldPosition, CreateCurrencyShopsSoundEvents.COIN_SAFE_WHEEL_OPEN.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
                wheelSoundPlayed = true;
            }
            if (door.getValue() > 0.1f && !doorSoundPlayed) {
                level.playSound(null, worldPosition, CreateCurrencyShopsSoundEvents.COIN_SHAKE.get(), SoundSource.BLOCKS, 0.5F, 0.8F + level.random.nextFloat() * 0.4F);
                level.playSound(null, worldPosition, CreateCurrencyShopsSoundEvents.COIN_SAFE_DOOR_OPEN.get(), SoundSource.BLOCKS, 0.5F, 0.8F + level.random.nextFloat() * 0.4F);
                doorSoundPlayed = true;
            }
        } else {
            if (door.getValue() < 0.9f && doorSoundPlayed) {
                level.playSound(null, worldPosition, CreateCurrencyShopsSoundEvents.COIN_SAFE_DOOR_CLOSE.get(), SoundSource.BLOCKS, 0.5F, 0.8F + level.random.nextFloat() * 0.4F);
                doorSoundPlayed = false;
            }
            if (wheel.getValue() < 0.9f && wheelSoundPlayed) {
                level.playSound(null, worldPosition, CreateCurrencyShopsSoundEvents.COIN_SHAKE.get(), SoundSource.BLOCKS, 0.5F, 0.8F + level.random.nextFloat() * 0.4F);
                level.playSound(null, worldPosition, CreateCurrencyShopsSoundEvents.COIN_SAFE_WHEEL_CLOSE.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
                wheelSoundPlayed = false;
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide) {
            totalValue = CoinSafeBalanceManager.getBalance((ServerLevel) level, safeId);
            CoinSafeSavedData.get((ServerLevel) level)
                .registerSafe(safeId, getShortID(), getCustomName());
        }
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            CreateCurrencyShopsBlocks.COIN_SAFE_BLOCK_ENTITY.get(),
            (be, context) -> {
                if (context == null) return be.itemHandler;
                BlockPos neighborPos = be.getBlockPos().relative(context);
                BlockState neighborState = be.getLevel().getBlockState(neighborPos);
                if (neighborState.is(AllBlocks.PACKAGER.get())) {
                    return be.itemHandler;
                }
                return null;
            }
        );
    }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (clientPacket && level != null && !level.isClientSide) {
            tag.putInt("TotalCoins", CoinSafeBalanceManager.getBalance((ServerLevel) level, safeId));
        }
        tag.putString("CustomName", customName);
        tag.putUUID("SafeId", safeId);
        if (ownerUUID != null)
            tag.putUUID("OwnerUUID", ownerUUID);
        tag.putBoolean("Locked", locked);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("CustomName")) {
            customName = tag.getString("CustomName");
        }
        if (tag.contains("SafeId")) {
            safeId = tag.getUUID("SafeId");
        }
        if (tag.contains("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        }
        if (tag.contains("Locked")) {
            locked = tag.getBoolean("Locked");
        }
        if (clientPacket && tag.contains("TotalCoins")) {
            totalValue = tag.getInt("TotalCoins");
        }
    }

    public String getCustomName() {
        return customName.isEmpty() ? Component.translatable("block.create_currency_shops.coin_safe").getString() : customName;
    }
    
    public void setCustomName(String name) {
        this.customName = name;
        setChanged();
        if (level != null && !level.isClientSide) {
            CoinSafeSavedData.get((ServerLevel) level)
                .registerSafe(safeId, getShortID(), name);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        DisplayLinkBlock.notifyGatherers(level, worldPosition);
    }

    public void addCoins(Item coinType, int amount) {
        if (level == null || level.isClientSide) return;
        if (CurrencyValues.COIN_VALUES.containsKey(coinType) && amount > 0) {
            int value = amount * CurrencyValues.COIN_VALUES.get(coinType);
            CoinSafeBalanceManager.addBalance((ServerLevel) level, safeId, value);
            setChanged();
            sendData();
            syncBalanceToClients();
            DisplayLinkBlock.notifyGatherers(level, worldPosition);
        }
    }

    public void addValue(int amount) {
        if (level == null || level.isClientSide) return;
        if (amount > 0) {
            CoinSafeBalanceManager.addBalance((ServerLevel) level, safeId, amount);
            setChanged();
            sendData();
            syncBalanceToClients();
            DisplayLinkBlock.notifyGatherers(level, worldPosition);
        }
    }

    public boolean removeValue(int amount) {
        if (level == null || level.isClientSide) return false;
        CoinSafeBalanceManager.removeBalance((ServerLevel) level, safeId, amount);
        setChanged();
        sendData();
        syncBalanceToClients();
        DisplayLinkBlock.notifyGatherers(level, worldPosition);
        return true;
    }

    public void removeCoins(Item coinType, int amount) {
        if (level == null || level.isClientSide) return;
        if (CurrencyValues.COIN_VALUES.containsKey(coinType) && amount > 0) {
            int valueToRemove = amount * CurrencyValues.COIN_VALUES.get(coinType);
            CoinSafeBalanceManager.removeBalance((ServerLevel) level, safeId, valueToRemove);
            setChanged();
            sendData();
            syncBalanceToClients();
            DisplayLinkBlock.notifyGatherers(level, worldPosition);
        }
    }

    private void syncBalanceToClients() {
        int newBalance = CoinSafeBalanceManager.getBalance((ServerLevel) level, safeId);
        java.util.Map<java.util.UUID, Integer> balances = new java.util.HashMap<>();
        balances.put(safeId, newBalance);
        CoinSafeBalancePacket packet =
            new CoinSafeBalancePacket(balances);
        CatnipServices.NETWORK.sendToAllClients(packet);
    }

    public int getTotalValue() {
        if (level != null && !level.isClientSide) {
            return CoinSafeBalanceManager.getBalance((ServerLevel) level, safeId);
        }
        return totalValue;
    }

    public java.util.UUID getSafeId() {
        return safeId;
    }

    public String getShortID() {
        long combined = safeId.getMostSignificantBits() ^ safeId.getLeastSignificantBits();
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append(chars.charAt((int) (Math.abs(combined >> (i * 5)) % chars.length())));
        }
        return code.toString();
    }

    public String getFullShortID() {
        return "$" + getShortID();
    }

    @Override
    public Component getDisplayName() {
        return customName.isEmpty() ? Component.translatable("block.create_currency_shops.coin_safe") : Component.literal(customName);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CoinSafeMenu(containerId, playerInventory, this);
    }

    public boolean isLocked() {
        return locked;
    }

    public void toggleLock(Player player) {
        if (ownerUUID == null) {

//            DEBUG set to a random UUID
//            ownerUUID = java.util.UUID.fromString("12345678-1234-1234-1234-123456789012");


            ownerUUID = player.getUUID();


            locked = !locked;
        } else if (ownerUUID.equals(player.getUUID())) {
            locked = !locked;
        }
        setChanged();
        notifyUpdate();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isAdmin(Player player) {
        return ownerUUID == null || ownerUUID.equals(player.getUUID());
    }

    public boolean canAccess(Player player) {
        return !locked || ownerUUID != null && ownerUUID.equals(player.getUUID());
    }

    public BlockPos getBlockPos() {
        return worldPosition;
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public List<ItemStack> getDrops() {
        return CreateCurrencyShopsHelper.inventorySummaryToItemStacks(
            CreateCurrencyShopsHelper.coinSummaryFromValue(getTotalValue()));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        write(tag, registries, true);
        return tag;
    }
}