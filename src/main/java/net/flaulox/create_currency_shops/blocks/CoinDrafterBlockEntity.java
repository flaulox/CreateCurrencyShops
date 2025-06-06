package net.flaulox.create_currency_shops.blocks;

import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.edgeInteraction.EdgeInteractionBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import net.flaulox.create_currency_shops.CreateCurrencyShopsBlocks;
import net.flaulox.create_currency_shops.CreateCurrencyShopsItems;
import net.flaulox.create_currency_shops.CreateCurrencyShopsSoundEvents;
import net.flaulox.create_currency_shops.items.CoinDraftItem;
import net.flaulox.create_currency_shops.util.CoinSafeSavedData;
import net.flaulox.create_currency_shops.util.CurrencyValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;
import java.util.UUID;


public class CoinDrafterBlockEntity extends PackagerBlockEntity {

    private VersionedInventoryTrackerBehaviour invVersionTracker;

    public CoinDrafterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }



    @Override
    public void tick() {
        super.tick();
        
        if (level.isClientSide && animationTicks > 0) {
            if (animationTicks == CYCLE - (animationInward ? 5 : 1)) {
                level.playLocalSound(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    CreateCurrencyShopsSoundEvents.COIN_SHAKE.get(),
                    SoundSource.BLOCKS, 1f, 1.0f, false);
            }
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        recheckIfLinksPresent();
    }





    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(targetInventory = new InvManipulationBehaviour(this, CapManipulationBehaviourBase.InterfaceProvider.oppositeOfBlockFacing())
            .withFilter(this::supportsBlockEntity));
        behaviours.add(invVersionTracker = new VersionedInventoryTrackerBehaviour(this));
        behaviours.add(new EdgeInteractionBehaviour(this, (w, p, n) -> {})
            .connectivity((w, p, f, d) -> false));
        behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
    }

    public boolean supportsBlockEntity(BlockEntity target) {
        return target instanceof CoinSafeBlockEntity;
    }

    @Override
    public InventorySummary getAvailableItems() {
        CoinSafeBlockEntity safe = getConnectedCoinSafe();
        if (safe == null)
            return new InventorySummary();
        
        InventorySummary summary = new InventorySummary();
        int totalValue = safe.getTotalValue();
        if (totalValue > 0) {
            ItemStack stack = new ItemStack(CreateCurrencyShopsItems.WRITE_DRAFT_ITEM.get());
            
            var nbt = new CompoundTag();
            nbt.putString("SafeName", safe.getCustomName());
            nbt.putString("SafeCode", safe.getShortID());
            
            if (safe.isLocked()) {
                nbt.putUUID("LockedOwner", safe.ownerUUID);
            }
            
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
            
            summary.add(stack, totalValue);
        }
        return summary;
    }

    public CoinSafeBlockEntity getConnectedCoinSafe() {
        if (level == null) return null;
        Direction facing = getBlockState().getValue(CoinDrafterBlock.FACING).getOpposite();
        BlockEntity be = level.getBlockEntity(worldPosition.relative(facing));
        return be instanceof CoinSafeBlockEntity safe ? safe : null;
    }

    @Override
    public boolean unwrapBox(ItemStack box, boolean simulate) {
        if (animationTicks > 0)
            return false;
        
        CoinSafeBlockEntity safe = getConnectedCoinSafe();
        if (safe == null)
            return false;
        
        ItemStackHandler contents = PackageItem.getContents(box);
        boolean hasValidContent = false;
        
        // Check if package contains only drafts and/or coins
        for (int i = 0; i < contents.getSlots(); i++) {
            ItemStack stack = contents.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof CoinDraftItem) {
                    java.util.UUID writtenTo = CoinDraftItem.getWrittenTo(stack);
                    java.util.UUID issuedFrom = CoinDraftItem.getIssuedFrom(stack);
                    java.util.UUID thisSafeId = safe.getSafeId();
                    java.util.UUID anyUUID = new java.util.UUID(0, 0);
                    
                    if (!thisSafeId.equals(writtenTo) && !thisSafeId.equals(issuedFrom) && !anyUUID.equals(writtenTo)) {
                        return false;
                    }
                    hasValidContent = true;
                } else if (CurrencyValues.COIN_VALUES.containsKey(stack.getItem())) {
                    hasValidContent = true;
                } else {
                    return false;
                }
            }
        }
        
        if (!hasValidContent)
            return false;
        
        // Deposit all drafts and coins
        if (!simulate) {
            for (int i = 0; i < contents.getSlots(); i++) {
                ItemStack stack = contents.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    if (stack.getItem() instanceof CoinDraftItem) {
                        int value = CoinDraftItem.getValue(stack);
                        safe.addValue(value);
                    } else if (CurrencyValues.COIN_VALUES.containsKey(stack.getItem())) {
                        safe.addCoins(stack.getItem(), stack.getCount());
                    }
                }
            }
            
            previouslyUnwrapped = box;
            animationInward = true;
            animationTicks = CYCLE;
            notifyUpdate();
        }
        return true;
    }



    @Override
    public void attemptToSend(List<PackagingRequest> queuedRequests) {
        if (queuedRequests == null || queuedRequests.isEmpty())
            return;
        if (!heldBox.isEmpty() || animationTicks != 0 || buttonCooldown > 0)
            return;
        if (!queuedExitingPackages.isEmpty())
            return;

        CoinSafeBlockEntity safe = getConnectedCoinSafe();
        if (safe == null)
            return;

        PackagingRequest request = queuedRequests.get(0);
        int requestedValue = request.getCount();
        if (requestedValue <= 0)
            return;
        if (safe.getTotalValue() < requestedValue)
            return;

        updateSignAddress();
        String stockTickerAddress = request.address();
        String signAddress = signBasedAddress;
        
        // Parse sign address into components
        String signPackageAddr = null;
        String signDraftCode = null;
        if (signAddress != null && signAddress.contains("$")) {
            String[] parts = signAddress.split("\\$", 2);
            if (parts.length == 2) {
                signPackageAddr = parts[0].trim();
                signDraftCode = parts[1].trim();
            }
        } else if (signAddress != null && !signAddress.isBlank()) {
            signPackageAddr = signAddress;
        }
        
        // Parse stock ticker address into components
        String tickerPackageAddr = null;
        String tickerDraftCode = null;
        if (stockTickerAddress != null && stockTickerAddress.contains("$")) {
            String[] parts = stockTickerAddress.split("\\$", 2);
            if (parts.length == 2) {
                tickerPackageAddr = parts[0].trim();
                tickerDraftCode = parts[1].trim();
            }
        } else if (stockTickerAddress != null && !stockTickerAddress.isBlank()) {
            tickerPackageAddr = stockTickerAddress;
        }
        
        // Modular fallback: use ticker values, fallback to sign values
        String finalPackageAddr = tickerPackageAddr != null && !tickerPackageAddr.isBlank() ? tickerPackageAddr : signPackageAddr;
        String finalDraftCode = tickerDraftCode != null && !tickerDraftCode.isBlank() ? tickerDraftCode : signDraftCode;
        
        // Resolve draft target UUID
        UUID targetUUID;
        String targetReadableID;
        if (finalDraftCode == null || finalDraftCode.isBlank()) {
            targetUUID = new UUID(0, 0);
            targetReadableID = "Any";
        } else {
            targetUUID = CoinSafeSavedData.get((ServerLevel)level)
                .findSafeByCode(finalDraftCode);
            if (targetUUID == null) return;
            targetReadableID = "$" + finalDraftCode;
        }

        if (!safe.removeValue(requestedValue))
            return;

        ItemStack draft = new ItemStack(CreateCurrencyShopsItems.COIN_DRAFT.get());
        CoinDraftItem.setValue(draft, requestedValue);
        CoinDraftItem.setIssuedFrom(draft, safe.getSafeId(), safe.getCustomName(), "$" + safe.getShortID());
        
        // Resolve target safe name and code from saved data
        String targetName;
        String targetCode;
        if (targetReadableID.equals("Any")) {
            targetName = "Any";
            targetCode = "";
        } else {
            targetName = CoinSafeSavedData.get((ServerLevel)level)
                .getSafeName(targetUUID);
            String code = CoinSafeSavedData.get((ServerLevel)level)
                .uuidToCode.get(targetUUID);
            targetCode = code != null ? "$" + code : "";
        }
        CoinDraftItem.setWrittenTo(draft, targetUUID, targetName, targetCode);

        ItemStackHandler extractedItems = new ItemStackHandler(PackageItem.SLOTS);
        extractedItems.setStackInSlot(0, draft);

        heldBox = PackageItem.containing(extractedItems);
        if (finalPackageAddr != null && !finalPackageAddr.isBlank())
            PackageItem.addAddress(heldBox, finalPackageAddr);
        animationInward = false;
        animationTicks = CYCLE;
        notifyUpdate();
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            CreateCurrencyShopsBlocks.COIN_DRAFTER_BLOCK_ENTITY.get(),
            (be, context) -> be.inventory
        );
    }
}
