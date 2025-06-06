package net.flaulox.create_currency_shops.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CoinSafeSavedData extends SavedData {
    private static final String DATA_NAME = "create_currency_shops_balances";
    private final Map<UUID, Integer> balances = new HashMap<>();
    private final Map<String, UUID> codeToUUID = new HashMap<>();
    public final Map<UUID, String> uuidToCode = new HashMap<>();
    private final Map<UUID, String> uuidToName = new HashMap<>();

    public static CoinSafeSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new Factory<>(CoinSafeSavedData::new, CoinSafeSavedData::load),
            DATA_NAME
        );
    }

    private static CoinSafeSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        CoinSafeSavedData data = new CoinSafeSavedData();
        CompoundTag balancesTag = tag.getCompound("Balances");
        for (String key : balancesTag.getAllKeys()) {
            data.balances.put(UUID.fromString(key), balancesTag.getInt(key));
        }
        CompoundTag codesTag = tag.getCompound("Codes");
        for (String code : codesTag.getAllKeys()) {
            UUID uuid = UUID.fromString(codesTag.getString(code));
            data.codeToUUID.put(code, uuid);
            data.uuidToCode.put(uuid, code);
        }
        CompoundTag namesTag = tag.getCompound("Names");
        for (String uuidStr : namesTag.getAllKeys()) {
            data.uuidToName.put(UUID.fromString(uuidStr), namesTag.getString(uuidStr));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag balancesTag = new CompoundTag();
        balances.forEach((uuid, balance) -> balancesTag.putInt(uuid.toString(), balance));
        tag.put("Balances", balancesTag);
        CompoundTag codesTag = new CompoundTag();
        codeToUUID.forEach((code, uuid) -> codesTag.putString(code, uuid.toString()));
        tag.put("Codes", codesTag);
        CompoundTag namesTag = new CompoundTag();
        uuidToName.forEach((uuid, name) -> namesTag.putString(uuid.toString(), name));
        tag.put("Names", namesTag);
        return tag;
    }

    public int getBalance(UUID safeId) {
        return balances.getOrDefault(safeId, 0);
    }

    public void setBalance(UUID safeId, int amount) {
        balances.put(safeId, Math.max(0, amount));
        setDirty();
    }

    public void addBalance(UUID safeId, int amount) {
        balances.merge(safeId, amount, Integer::sum);
        setDirty();
    }

    public void removeBalance(UUID safeId, int amount) {
        int current = balances.getOrDefault(safeId, 0);
        balances.put(safeId, Math.max(0, current - amount));
        setDirty();
    }

    public void deleteSafe(UUID safeId) {
        balances.remove(safeId);
        String code = uuidToCode.remove(safeId);
        if (code != null) {
            codeToUUID.remove(code);
        }
        uuidToName.remove(safeId);
        setDirty();
    }

    public void registerSafe(UUID safeId, String code, String name) {
        String oldCode = uuidToCode.get(safeId);
        if (oldCode != null) {
            codeToUUID.remove(oldCode);
        }
        codeToUUID.put(code.toUpperCase(), safeId);
        uuidToCode.put(safeId, code.toUpperCase());
        uuidToName.put(safeId, name);
        setDirty();
    }

    public String getSafeName(UUID safeId) {
        return uuidToName.getOrDefault(safeId, "Unknown");
    }

    public UUID findSafeByCode(String code) {
        return codeToUUID.get(code.toUpperCase());
    }

    public Map<UUID, Integer> getAllBalances() {
        return new HashMap<>(balances);
    }
}
