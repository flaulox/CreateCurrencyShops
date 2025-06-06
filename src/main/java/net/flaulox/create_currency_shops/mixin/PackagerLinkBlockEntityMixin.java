package net.flaulox.create_currency_shops.mixin;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlock;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import net.flaulox.create_currency_shops.blocks.CoinSafeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PackagerLinkBlockEntity.class)
public class PackagerLinkBlockEntityMixin {
    
    @Inject(method = "getPackager", at = @At("RETURN"), cancellable = true)
    private void checkForCoinSafe(CallbackInfoReturnable<PackagerBlockEntity> cir) {
        if (cir.getReturnValue() != null) return;
        
        PackagerLinkBlockEntity self = (PackagerLinkBlockEntity) (Object) this;
        BlockState blockState = self.getBlockState();
        BlockPos source = self.getBlockPos().relative(PackagerLinkBlock.getConnectedDirection(blockState).getOpposite());
        
        if (self.getLevel().getBlockEntity(source) instanceof CoinSafeBlockEntity coinSafe) {
            cir.setReturnValue(new CoinSafePackagerWrapper(coinSafe));
        }
    }
    
    private static class CoinSafePackagerWrapper extends PackagerBlockEntity {
        private final CoinSafeBlockEntity coinSafe;
        
        public CoinSafePackagerWrapper(CoinSafeBlockEntity coinSafe) {
            super(null, coinSafe.getBlockPos(), coinSafe.getBlockState());
            this.coinSafe = coinSafe;
        }
        
        @Override
        public InventorySummary getAvailableItems() {
            InventorySummary summary = new InventorySummary();
            IItemHandler handler = coinSafe.getItemHandler();
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    summary.add(stack);
                }
            }
            return summary;
        }
        
        @Override
        public boolean isTooBusyFor(LogisticallyLinkedBehaviour.RequestType type) {
            return false;
        }
    }
}