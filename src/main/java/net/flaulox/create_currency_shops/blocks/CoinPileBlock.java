package net.flaulox.create_currency_shops.blocks;

import com.mojang.serialization.MapCodec;
import net.flaulox.create_currency_shops.CreateCurrencyShopsBlocks;
import net.flaulox.create_currency_shops.CreateCurrencyShopsItems;
import net.flaulox.create_currency_shops.CreateCurrencyShopsSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.registries.BuiltInRegistries;

import javax.annotation.Nullable;

public class CoinPileBlock extends BaseEntityBlock {
    public static final MapCodec<CoinPileBlock> CODEC = simpleCodec(CoinPileBlock::new);
    public static final IntegerProperty COINS = IntegerProperty.create("coins", 1, 32);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<CoinType> COIN_TYPE =
        EnumProperty.create("coin_type", CoinType.class);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 1, 16);

    public enum CoinType implements StringRepresentable {
        COPPER("copper"), IRON("iron"), ZINC("zinc"), BRASS("brass"), GOLD("gold"), NETHERITE("netherite");
        private final String name;
        CoinType(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }

    public CoinPileBlock(Properties properties) {
        super(properties.pushReaction(PushReaction.DESTROY));
        registerDefaultState(stateDefinition.any().setValue(COINS, 1).setValue(FACING, Direction.NORTH).setValue(COIN_TYPE, CoinType.COPPER));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !neighborState.isAir() && !neighborState.isFaceSturdy(level, neighborPos, Direction.UP)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return isCoinItem(context.getItemInHand().getItem()) && state.getValue(COINS) < COINS.getPossibleValues().size() || super.canBeReplaced(state, context);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockState = context.getLevel().getBlockState(context.getClickedPos());
        if (blockState.is(this) && blockState.getValue(COINS) < COINS.getPossibleValues().size()) {
            if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof CoinPileBlockEntity be) {
                ItemStack stack = context.getItemInHand();
                int maxCoins = COINS.getPossibleValues().size();
                int currentCoins = blockState.getValue(COINS);
                int toAdd = context.isSecondaryUseActive() ? Math.min(stack.getCount(), maxCoins - currentCoins) : 1;
                
                if (toAdd > 1 && !context.getLevel().isClientSide) {
                    context.getLevel().playSound(null, context.getClickedPos(), CreateCurrencyShopsSoundEvents.COIN_SHAKE.get(), SoundSource.BLOCKS, 1F, 0.8F + context.getLevel().random.nextFloat() * 0.4F);
                } else if (toAdd == 1 && !context.getLevel().isClientSide) {
                    context.getLevel().playSound(null, context.getClickedPos(), CreateCurrencyShopsSoundEvents.COIN_PING.get(), SoundSource.BLOCKS, 1.0F, 0.8F + context.getLevel().random.nextFloat() * 0.4F);
                }
                
                for (int i = 0; i < toAdd; i++) {
                    be.addCoin(stack.getItem());
                }
                
                if (!context.getLevel().isClientSide) {
                    context.getLevel().sendBlockUpdated(context.getClickedPos(), blockState, blockState, 3);
                }
                
                if (!context.getPlayer().isCreative()) {
                    stack.shrink(toAdd - 1);
                }
            }
            return blockState.setValue(COINS, Math.min(COINS.getPossibleValues().size(), blockState.getValue(COINS) + (context.isSecondaryUseActive() ? Math.min(context.getItemInHand().getCount(), COINS.getPossibleValues().size() - blockState.getValue(COINS)) : 1)))
                .setValue(COIN_TYPE, getCoinType(context.getItemInHand().getItem()));
        }
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(COIN_TYPE, getCoinType(context.getItemInHand().getItem()));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (state.getValue(COINS) == 1 && level.getBlockEntity(pos) instanceof CoinPileBlockEntity be) {
            int maxCoins = COINS.getPossibleValues().size();
            int toAdd = placer != null && placer.isShiftKeyDown() ? Math.min(stack.getCount() - 1, maxCoins - 1) : 0;

            if (!level.isClientSide) {
                if (toAdd > 0) {
                    level.playSound(null, pos, CreateCurrencyShopsSoundEvents.COIN_SHAKE.get(), SoundSource.BLOCKS, 1F, 0.8F + level.random.nextFloat() * 0.4F);
                    level.playSound(null, pos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);

                } else {
                    level.playSound(null, pos, CreateCurrencyShopsSoundEvents.COIN_PING.get(), SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
                    level.playSound(null, pos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                
                }
            }
            
            be.addCoin(stack.getItem());
            for (int i = 0; i < toAdd; i++) {
                be.addCoin(stack.getItem());
            }
            
            if (!level.isClientSide) {
                BlockState newState = state.setValue(COINS, Math.min(maxCoins, 1 + toAdd));
                level.setBlock(pos, newState, 3);
                level.sendBlockUpdated(pos, state, newState, 3);
            }
            
            if (placer instanceof Player player && !player.getAbilities().instabuild) {
                stack.shrink(toAdd);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COINS, FACING, COIN_TYPE);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!oldState.is(state.getBlock()) && level.getBlockEntity(pos) instanceof CoinPileBlockEntity be) {
            if (be.coins.isEmpty() && state.getValue(COINS) > 0) {
                Item coinType = getCoinItemFromType(state.getValue(COIN_TYPE));
                for (int i = 0; i < state.getValue(COINS); i++) {
                    be.coins.add(coinType);
                }
                be.setChanged();
            }
        }
    }

    private Item getCoinItemFromType(CoinType type) {
        return switch (type) {
            case COPPER -> CreateCurrencyShopsItems.COPPER_COIN.get();
            case IRON -> CreateCurrencyShopsItems.IRON_COIN.get();
            case ZINC -> CreateCurrencyShopsItems.ZINC_COIN.get();
            case BRASS -> CreateCurrencyShopsItems.BRASS_COIN.get();
            case GOLD -> CreateCurrencyShopsItems.GOLD_COIN.get();
            case NETHERITE -> CreateCurrencyShopsItems.NETHERITE_COIN.get();
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int coins = state.getValue(COINS);
        float height = Math.max((coins + 9) / 10, 1);
        return Block.box(0, 0, 0, 16, height, 16);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CreateCurrencyShopsBlocks.COIN_PILE_BLOCK_ENTITY.create(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                if (state.getValue(COINS) == 1) {
                    level.playSound(null, pos, CreateCurrencyShopsSoundEvents.COIN_PING.get(), SoundSource.BLOCKS, 0.8F, 0.8F + level.random.nextFloat() * 0.4F);
                } else {
                    level.playSound(null, pos, CreateCurrencyShopsSoundEvents.COIN_SHAKE.get(), SoundSource.BLOCKS, 0.8F, 0.8F + level.random.nextFloat() * 0.4F);
                }
            }
            if (!isMoving && level.getBlockEntity(pos) instanceof CoinPileBlockEntity be) {
                for (Item coin : be.coins) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(coin));
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof CoinPileBlockEntity be && !be.coins.isEmpty()) {
            return new ItemStack(be.coins.get(0));
        }
        return ItemStack.EMPTY;
    }

    private boolean isCoinItem(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        return id.contains("_coin") && id.contains("create_currency_shops");
    }

    private CoinType getCoinType(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).getPath();
        if (id.contains("copper")) return CoinType.COPPER;
        if (id.contains("iron")) return CoinType.IRON;
        if (id.contains("zinc")) return CoinType.ZINC;
        if (id.contains("brass")) return CoinType.BRASS;
        if (id.contains("gold")) return CoinType.GOLD;
        if (id.contains("netherite")) return CoinType.NETHERITE;
        return CoinType.COPPER;
    }
}
