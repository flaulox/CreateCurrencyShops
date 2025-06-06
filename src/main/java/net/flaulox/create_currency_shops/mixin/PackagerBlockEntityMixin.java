package net.flaulox.create_currency_shops.mixin;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.createmod.catnip.data.Iterate;
import net.flaulox.create_currency_shops.items.CoinDraftItem;
import net.flaulox.create_currency_shops.util.CurrencyValues;
import net.flaulox.create_currency_shops.blocks.CoinSafeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PackagerBlockEntity.class, remap = false)
public class PackagerBlockEntityMixin {

    @Inject(method = "unwrapBox", at = @At("HEAD"), cancellable = true)
    private void handleCoinPackages(ItemStack box, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        PackagerBlockEntity self = (PackagerBlockEntity) (Object) this;

        // Check if connected to coin safe
        for (Direction d : Iterate.directions) {
            if (self.getLevel().getBlockEntity(self.getBlockPos().relative(d)) instanceof CoinSafeBlockEntity coinSafe) {
                ItemStackHandler contents = PackageItem.getContents(box);
                boolean hasValidContent = false;

                // Check if package contains only coins and/or valid drafts
                for (int i = 0; i < contents.getSlots(); i++) {
                    ItemStack stack = contents.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        if (stack.getItem() instanceof CoinDraftItem) {
                            java.util.UUID writtenTo = CoinDraftItem.getWrittenTo(stack);
                            java.util.UUID issuedFrom = CoinDraftItem.getIssuedFrom(stack);
                            java.util.UUID thisSafeId = coinSafe.getSafeId();
                            java.util.UUID anyUUID = new java.util.UUID(0, 0);
                            
                            if (!thisSafeId.equals(writtenTo) && !thisSafeId.equals(issuedFrom) && !anyUUID.equals(writtenTo)) {
                                return;
                            }
                            hasValidContent = true;
                        } else if (CurrencyValues.COIN_VALUES.containsKey(stack.getItem())) {
                            hasValidContent = true;
                        } else {
                            return;
                        }
                    }
                }

                if (hasValidContent) {
                    // Try to insert package into coin safe
                    IItemHandler coinSafeHandler = coinSafe.getItemHandler();
                    ItemStack result = coinSafeHandler.insertItem(0, box, simulate);
                    if (result.isEmpty()) {
                        if (!simulate) {
                            // Trigger full animation
                            try {
                                java.lang.reflect.Field prevField = PackagerBlockEntity.class.getDeclaredField("previouslyUnwrapped");
                                prevField.setAccessible(true);
                                prevField.set(self, box);

                                java.lang.reflect.Field animField = PackagerBlockEntity.class.getDeclaredField("animationInward");
                                animField.setAccessible(true);
                                animField.set(self, true);

                                java.lang.reflect.Field ticksField = PackagerBlockEntity.class.getDeclaredField("animationTicks");
                                ticksField.setAccessible(true);
                                ticksField.set(self, 20); // CYCLE value

                                self.notifyUpdate();
                            } catch (Exception e) {
                            }
                        }
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
        }
    }
}
