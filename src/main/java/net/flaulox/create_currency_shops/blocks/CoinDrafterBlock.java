package net.flaulox.create_currency_shops.blocks;

import com.simibubi.create.content.logistics.packager.PackagerBlock;
import net.flaulox.create_currency_shops.CreateCurrencyShopsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CoinDrafterBlock extends PackagerBlock {
    
    public CoinDrafterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends CoinDrafterBlockEntity> getBlockEntityType() {
        return CreateCurrencyShopsBlocks.COIN_DRAFTER_BLOCK_ENTITY.get();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferredFacing = null;
        for (Direction face : context.getNearestLookingDirections()) {
            BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos().relative(face));
            if (be instanceof CoinSafeBlockEntity) {
                preferredFacing = face.getOpposite();
                break;
            }
        }

        Player player = context.getPlayer();
        if (preferredFacing == null) {
            Direction facing = context.getNearestLookingDirection();
            preferredFacing = player != null && player.isShiftKeyDown() ? facing : facing.getOpposite();
        }

        return super.getStateForPlacement(context).setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos())).setValue(FACING, preferredFacing);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        
        if (level.isClientSide)
            return;
        
        Direction currentFacing = state.getValue(FACING);
        BlockPos facingPos = pos.relative(currentFacing);
        
        if (neighborPos.equals(facingPos) && CreateCurrencyShopsBlocks.COIN_SAFE.has(level.getBlockState(facingPos))) {
            if (level.getBlockEntity(pos) instanceof CoinDrafterBlockEntity drafter) {
                drafter.recheckIfLinksPresent();
            }
        }
    }
}
