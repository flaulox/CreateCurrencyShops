package net.flaulox.create_currency_shops.blocks;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.api.contraption.transformable.TransformableBlock;
import com.simibubi.create.content.contraptions.StructureTransform;
import net.createmod.catnip.platform.CatnipServices;
import net.flaulox.create_currency_shops.CreateCurrencyShopsBlocks;
import net.flaulox.create_currency_shops.CreateCurrencyShopsSoundEvents;
import net.flaulox.create_currency_shops.items.CoinDraftItem;
import net.flaulox.create_currency_shops.items.CreditCardItem;
import net.flaulox.create_currency_shops.network.CoinSafeBalancePacket;
import net.flaulox.create_currency_shops.util.CoinSafeSavedData;
import net.flaulox.create_currency_shops.util.LogisticsHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.Containers;
import net.minecraft.world.item.context.UseOnContext;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;
import java.util.UUID;

public class CoinSafeBlock extends BaseEntityBlock implements IBE<CoinSafeBlockEntity>, IWrenchable, TransformableBlock {
    public static final MapCodec<CoinSafeBlock> CODEC = simpleCodec(CoinSafeBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    
    private static final VoxelShape SHAPE_NORTH = Block.box(1, 0, 0, 15, 16, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.box(1, 0, 0, 15, 16, 16);
    private static final VoxelShape SHAPE_WEST = Block.box(0, 0, 1, 16, 16, 15);
    private static final VoxelShape SHAPE_EAST = Block.box(0, 0, 1, 16, 16, 15);

    public CoinSafeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public Class<CoinSafeBlockEntity> getBlockEntityClass() {
        return CoinSafeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CoinSafeBlockEntity> getBlockEntityType() {
        return CreateCurrencyShopsBlocks.COIN_SAFE_BLOCK_ENTITY.get();
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        if (LogisticsHelper.isNetworkLocked(context.getLevel(), context.getClickedPos(), context.getPlayer())) {
            return InteractionResult.FAIL;
        }
        return IWrenchable.super.onSneakWrenched(state, context);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CreateCurrencyShopsBlocks.COIN_SAFE_BLOCK_ENTITY.create(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (AllBlocks.CLIPBOARD.isIn(stack)) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof CoinSafeBlockEntity safe) {
                safe.addAddressToClipboard(player, stack);
            }
            return ItemInteractionResult.SUCCESS;
        }
        if (stack.getItem() instanceof CreditCardItem) {
            return ItemInteractionResult.FAIL;
        }
        if (stack.getItem() instanceof CoinDraftItem && level.getBlockEntity(pos) instanceof CoinSafeBlockEntity safe) {
            java.util.UUID writtenTo = CoinDraftItem.getWrittenTo(stack);
            java.util.UUID issuedFrom = CoinDraftItem.getIssuedFrom(stack);
            java.util.UUID thisSafeId = safe.getSafeId();
            java.util.UUID anyUUID = new java.util.UUID(0, 0);
            
            if (thisSafeId.equals(writtenTo) || thisSafeId.equals(issuedFrom) || anyUUID.equals(writtenTo)) {
                int draftValue = CoinDraftItem.getValue(stack);
                if (!level.isClientSide) {
                    safe.addValue(draftValue);
                    stack.shrink(1);
                }
                player.displayClientMessage(Component.literal(String.format("%,d", draftValue) + " " + Component.translatable("create_currency_shops.wallet.deposited").getString()), true);
                player.playSound(CreateCurrencyShopsSoundEvents.COIN_SHAKE.value());
                player.playSound(AllSoundEvents.ITEM_HATCH.getMainEvent(), 0.5f, 1f);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            AllSoundEvents.DENY.playOnServer(level, pos);
            player.displayClientMessage(Component.translatable("create_currency_shops.coin_draft.wrong_safe").withStyle(ChatFormatting.RED), true);
            return ItemInteractionResult.FAIL;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }


    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof CoinSafeBlockEntity safeEntity) {
            if (LogisticsHelper.isNetworkLocked(level, pos, player)) {
                return InteractionResult.SUCCESS;
            }
            
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(safeEntity, pos);
            }
        }

        return InteractionResult.SUCCESS;
    }


    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (level instanceof Level l && LogisticsHelper.isNetworkLocked(l, pos, player)) {
            return 0.0F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof CoinSafeBlockEntity safeEntity) {
                safeEntity.getDrops().forEach(stack -> Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack));
                if (!level.isClientSide) {
                    UUID safeId = safeEntity.getSafeId();
                    CoinSafeSavedData.get((ServerLevel) level)
                        .deleteSafe(safeId);
                    
                    java.util.Map<UUID, Integer> balances = new java.util.HashMap<>();
                    balances.put(safeId, -1);
                    CoinSafeBalancePacket packet =
                        new CoinSafeBalancePacket(balances);
                    CatnipServices.NETWORK.sendToAllClients(packet);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public BlockState transform(BlockState state, StructureTransform transform) {
        if (state.hasProperty(FACING)) {
            return state.setValue(FACING, transform.rotateFacing(state.getValue(FACING)));
        }
        return state;
    }

}