package net.flaulox.create_currency_shops;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.createmod.ponder.foundation.PonderIndex;
import net.flaulox.create_currency_shops.blocks.CoinDrafterBlockEntity;
import net.flaulox.create_currency_shops.blocks.CoinSafeBlockEntity;
import net.flaulox.create_currency_shops.client.*;
import net.flaulox.create_currency_shops.network.CoinSafeBalancePacket;
import net.flaulox.create_currency_shops.network.CoinSafeLockPacket;
import net.flaulox.create_currency_shops.network.CoinSafeNamePacket;
import net.flaulox.create_currency_shops.ponder.CurrencyShopsPonderPlugin;
import net.flaulox.create_currency_shops.util.CoinSafeSavedData;
import net.flaulox.create_currency_shops.util.CurrencyValues;
import net.flaulox.create_currency_shops.util.FeatureToggle;
import net.flaulox.create_currency_shops.util.CoinTooltipHandler;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

@Mod(CreateCurrencyShops.MODID)
public class CreateCurrencyShops {
    public static final String MODID = "create_currency_shops";

    public static final CreateCurrencyShopsRegistrate CREATE_CURRENCY_SHOPS_REGISTRATE = CreateCurrencyShopsRegistrate.create(MODID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null);





    public CreateCurrencyShops(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        
        FeatureToggle.register(modEventBus);
        
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);

        NeoForge.EVENT_BUS.register(this);

        CREATE_CURRENCY_SHOPS_REGISTRATE.registerEventListeners(modEventBus);

        CreateCurrencyShopsDisplaySources.register();
        CreateCurrencyShopsItems.register();
        CreateCurrencyShopsBlocks.register();
        CreateCurrencyShopsMenus.register();
        CreateCurrencyShopsCreativeModeTab.register(modEventBus);
        CreateCurrencyShopsPartialModels.init();
        CreateCurrencyShopsSoundEvents.SOUND_EVENTS.register(modEventBus);
        NeoForge.EVENT_BUS.register(CoinTooltipHandler.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CurrencyValues.init();
            CatnipPacketRegistry registry = new CatnipPacketRegistry(MODID, 1);
            registry.registerPacket(new CatnipPacketRegistry.PacketType<>(
                CoinSafeNamePacket.TYPE,
                CoinSafeNamePacket.class,
                CoinSafeNamePacket.STREAM_CODEC
            ));
            registry.registerPacket(new CatnipPacketRegistry.PacketType<>(
                CoinSafeBalancePacket.TYPE,
                CoinSafeBalancePacket.class,
                CoinSafeBalancePacket.STREAM_CODEC
            ));
            registry.registerPacket(new CatnipPacketRegistry.PacketType<>(
                CoinSafeLockPacket.TYPE,
                CoinSafeLockPacket.class,
                CoinSafeLockPacket.STREAM_CODEC
            ));
            registry.registerAllPackets();
        });
    }

    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        CoinSafeBlockEntity.registerCapabilities(event);
        CoinDrafterBlockEntity.registerCapabilities(event);
    }



    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level() instanceof ServerLevel serverLevel) {
            CoinSafeSavedData savedData =
                CoinSafeSavedData.get(serverLevel);
            CoinSafeBalancePacket packet =
                new CoinSafeBalancePacket(savedData.getAllBalances());
            PacketDistributor.sendToPlayer(
                (ServerPlayer) event.getEntity(), packet);
        }
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            PonderIndex.addPlugin(new CurrencyShopsPonderPlugin());
            
            event.enqueueWork(() -> {
                WalletModelPredicates.register();
                BlockEntityRenderers.register(
                    CreateCurrencyShopsBlocks.COIN_SAFE_BLOCK_ENTITY.get(),
                        CoinSafeRenderer::new
                );
                BlockEntityRenderers.register(
                    CreateCurrencyShopsBlocks.COIN_PILE_BLOCK_ENTITY.get(),
                    CoinPileRenderer::new
                );
                BlockEntityRenderers.register(
                    CreateCurrencyShopsBlocks.COIN_DRAFTER_BLOCK_ENTITY.get(),
                    CoinDrafterRenderer::new
                );
                dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer.builder(CreateCurrencyShopsBlocks.COIN_DRAFTER_BLOCK_ENTITY.get())
                    .factory(CoinDrafterVisual::new)
                    .skipVanillaRender($1 -> false)
                    .apply();
            });
        }
    }

    public static CreateCurrencyShopsRegistrate registrate() {

        return CREATE_CURRENCY_SHOPS_REGISTRATE;
    }

    @Nullable
    public static KineticStats create(Item item) {
        return null;
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
