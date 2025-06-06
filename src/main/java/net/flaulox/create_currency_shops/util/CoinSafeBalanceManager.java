package net.flaulox.create_currency_shops.util;

import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

public class CoinSafeBalanceManager {
    
    public static int getBalance(ServerLevel level, UUID safeId) {
        return CoinSafeSavedData.get(level).getBalance(safeId);
    }

    public static void setBalance(ServerLevel level, UUID safeId, int amount) {
        CoinSafeSavedData.get(level).setBalance(safeId, amount);
    }

    public static void addBalance(ServerLevel level, UUID safeId, int amount) {
        CoinSafeSavedData.get(level).addBalance(safeId, amount);
    }

    public static void removeBalance(ServerLevel level, UUID safeId, int amount) {
        CoinSafeSavedData.get(level).removeBalance(safeId, amount);
    }
}
