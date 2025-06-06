package net.flaulox.create_currency_shops;

import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.flaulox.create_currency_shops.blocks.CoinDrafterBlock;
import net.flaulox.create_currency_shops.blocks.CoinDrafterBlockEntity;
import net.flaulox.create_currency_shops.blocks.CoinPileBlock;
import net.flaulox.create_currency_shops.blocks.CoinPileBlockEntity;
import net.flaulox.create_currency_shops.blocks.CoinSafeBlock;
import net.flaulox.create_currency_shops.blocks.CoinSafeBlockEntity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class CreateCurrencyShopsBlocks {
    private static final CreateCurrencyShopsRegistrate REGISTRATE = CreateCurrencyShops.registrate();

    public static final BlockEntry<CoinSafeBlock> COIN_SAFE = REGISTRATE
            .block("coin_safe", CoinSafeBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.strength(3.0F, 1200.0F))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_BLUE)
                    .sound(SoundType.NETHERITE_BLOCK))
            .transform(pickaxeOnly())
            .item()
            .build()
            .register();

    public static final BlockEntry<CoinPileBlock> COIN_PILE = REGISTRATE
            .block("coin_pile", CoinPileBlock::new)
            .properties(p -> p.strength(0).sound(SoundType.METAL).noOcclusion())
            .register();

    public static final BlockEntityEntry<CoinPileBlockEntity> COIN_PILE_BLOCK_ENTITY = REGISTRATE
            .<CoinPileBlockEntity>blockEntity("coin_pile", (type, pos, state) -> new CoinPileBlockEntity(type, pos, state))
            .validBlocks(COIN_PILE)
            .register();

    public static final BlockEntityEntry<CoinSafeBlockEntity> COIN_SAFE_BLOCK_ENTITY = REGISTRATE
            .<CoinSafeBlockEntity>blockEntity("coin_safe", (type, pos, state) -> new CoinSafeBlockEntity(type, pos, state))
            .validBlocks(COIN_SAFE)
            .register();

    public static final BlockEntry<CoinDrafterBlock> COIN_DRAFTER = REGISTRATE
            .block("coin_drafter", CoinDrafterBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.strength(3.0F, 6.0F))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_BLUE)
                    .sound(SoundType.NETHERITE_BLOCK))
            .transform(pickaxeOnly())
            .item()
            .build()
            .register();

    public static final BlockEntityEntry<CoinDrafterBlockEntity> COIN_DRAFTER_BLOCK_ENTITY = REGISTRATE
            .<CoinDrafterBlockEntity>blockEntity("coin_drafter", (type, pos, state) -> new CoinDrafterBlockEntity(type, pos, state))
            .validBlocks(COIN_DRAFTER)
            .register();

    public static void register() {}
}