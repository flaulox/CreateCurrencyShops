package net.flaulox.create_currency_shops;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public class CreateCurrencyShopsPartialModels {
    public static final PartialModel COIN_SAFE_DOOR = PartialModel.of(ResourceLocation.fromNamespaceAndPath(CreateCurrencyShops.MODID, "block/coin_safe/door"));
    public static final PartialModel COIN_SAFE_WHEEL = PartialModel.of(ResourceLocation.fromNamespaceAndPath(CreateCurrencyShops.MODID, "block/coin_safe/wheel"));
    public static final PartialModel COIN_DRAFTER_TRAY = PartialModel.of(ResourceLocation.fromNamespaceAndPath(CreateCurrencyShops.MODID, "block/coin_drafter/tray"));
    public static final PartialModel COIN_DRAFTER_HATCH = PartialModel.of(ResourceLocation.fromNamespaceAndPath(CreateCurrencyShops.MODID, "block/coin_drafter/hatch"));
    public static final PartialModel COIN_DRAFTER_HATCH_OPEN = PartialModel.of(ResourceLocation.fromNamespaceAndPath(CreateCurrencyShops.MODID, "block/coin_drafter/hatch_open"));
    
    public static void init() {}
}
