package net.flaulox.create_currency_shops.network;

import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.flaulox.create_currency_shops.blocks.CoinSafeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public record CoinSafeNamePacket(BlockPos pos, String name) implements ServerboundPacketPayload {
    public static final CustomPacketPayload.Type<CoinSafeNamePacket> TYPE = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath("create_currency_shops", "coin_safe_name")
    );
    
    public static final StreamCodec<RegistryFriendlyByteBuf, CoinSafeNamePacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, CoinSafeNamePacket::pos,
        ByteBufCodecs.STRING_UTF8, CoinSafeNamePacket::name,
        CoinSafeNamePacket::new
    );

    @Override
    public PacketTypeProvider getTypeProvider() {
        return new PacketTypeProvider() {
            @Override
            public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
                return (CustomPacketPayload.Type<T>) TYPE;
            }
        };
    }

    @Override
    public void handle(ServerPlayer player) {
        BlockEntity be = player.level().getBlockEntity(pos);
        if (be instanceof CoinSafeBlockEntity coinSafe) {
            coinSafe.setCustomName(name);
        }
    }
}