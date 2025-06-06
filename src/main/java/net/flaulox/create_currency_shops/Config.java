package net.flaulox.create_currency_shops;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    
    public static final ModConfigSpec.BooleanValue ALLOW_COIN_PILE_PLACEMENT = BUILDER
            .comment("Allow placing coins into coin piles")
            .define("allowCoinPilePlacement", true);
    
    public static final ModConfigSpec.BooleanValue ENABLE_COIN_DRAFTER = BUILDER
            .comment("Enable the Coin Drafter block")
            .define("enableCoinDrafter", true);
    
    public static final ModConfigSpec SPEC = BUILDER.build();
}
