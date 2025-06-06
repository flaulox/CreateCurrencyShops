package net.flaulox.create_currency_shops.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientBalanceCache {
    private static final Map<UUID, Integer> BALANCES = new HashMap<>();

    public static void updateBalances(Map<UUID, Integer> balances) {
        BALANCES.putAll(balances);
    }

    public static int getBalance(UUID safeId) {
        return BALANCES.getOrDefault(safeId, 0);
    }

}
