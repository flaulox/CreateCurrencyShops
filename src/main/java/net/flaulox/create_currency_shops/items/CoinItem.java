package net.flaulox.create_currency_shops.items;

import net.flaulox.create_currency_shops.Config;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CoinItem extends Item {
    private final Block block;
    
    public CoinItem(Block block, Properties properties) {
        super(properties);
        this.block = block;
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!Config.ALLOW_COIN_PILE_PLACEMENT.get())
            return InteractionResult.PASS;
        return this.place(new BlockPlaceContext(context));
    }
    
    public InteractionResult place(BlockPlaceContext context) {
        if (!context.canPlace()) return InteractionResult.FAIL;
        BlockState belowState = context.getLevel().getBlockState(context.getClickedPos().below());
        if (!belowState.isFaceSturdy(context.getLevel(), context.getClickedPos().below(), Direction.UP)) {
            return InteractionResult.FAIL;
        }
        BlockState state = block.getStateForPlacement(context);
        if (state == null) return InteractionResult.FAIL;
        if (!context.getLevel().setBlock(context.getClickedPos(), state, 11)) return InteractionResult.FAIL;
        block.setPlacedBy(context.getLevel(), context.getClickedPos(), state, context.getPlayer(), context.getItemInHand());
        context.getItemInHand().shrink(1);
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
    
    @Override
    public String getDescriptionId(ItemStack stack) {
        return this.getDescriptionId();
    }
    
    @Override
    public String getDescriptionId() {
        return "item." + BuiltInRegistries.ITEM.getKey(this).toString().replace(':', '.');
    }
}
