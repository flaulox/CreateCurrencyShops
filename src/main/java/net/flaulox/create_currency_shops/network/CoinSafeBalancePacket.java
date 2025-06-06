package net.flaulox.create_currency_shops.network;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.flaulox.create_currency_shops.util.ClientBalanceCache;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record CoinSafeBalancePacket(Map<UUID, Integer> balances) implements ClientboundPacketPayload {
    public static final Type<CoinSafeBalancePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("create_currency_shops", "coin_safe_balance"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, CoinSafeBalancePacket> STREAM_CODEC = StreamCodec.of(
        CoinSafeBalancePacket::write,
        CoinSafeBalancePacket::read
    );

    private static void write(RegistryFriendlyByteBuf buf, CoinSafeBalancePacket packet) {
        buf.writeInt(packet.balances.size());
        packet.balances.forEach((uuid, balance) -> {
            buf.writeUUID(uuid);
            buf.writeInt(balance);
        });
    }

    private static CoinSafeBalancePacket read(RegistryFriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<UUID, Integer> balances = new HashMap<>();
        for (int i = 0; i < size; i++) {
            balances.put(buf.readUUID(), buf.readInt());
        }
        return new CoinSafeBalancePacket(balances);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return new PacketTypeProvider() {
            @Override
            public <T extends CustomPacketPayload> Type<T> getType() {
                return (Type<T>) TYPE;
            }
        };
    }

    @Override
    public void handle(LocalPlayer player) {
        ClientBalanceCache.updateBalances(balances);
    }


}
