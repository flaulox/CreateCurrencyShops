package net.flaulox.create_currency_shops;

import com.tterrag.registrate.util.entry.MenuEntry;
import net.flaulox.create_currency_shops.blocks.CoinSafeBlockEntity;
import net.flaulox.create_currency_shops.gui.CoinSafeScreen;
import net.flaulox.create_currency_shops.gui.WalletScreen;
import net.flaulox.create_currency_shops.gui.CoinSafeMenu;
import net.flaulox.create_currency_shops.gui.WalletMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public class CreateCurrencyShopsMenus {
    private static final CreateCurrencyShopsRegistrate REGISTRATE = CreateCurrencyShops.registrate();

    public static final MenuEntry<WalletMenu> WALLET = REGISTRATE
            .menu("wallet", (type, id, inv, buffer) -> {
                int slot = buffer.readInt();
                ItemStack wallet = slot >= 0 ? inv.getItem(slot) : inv.offhand.get(0);
                return new WalletMenu(id, inv, wallet, slot);
            }, () -> WalletScreen::new)
            .register();

    public static final MenuEntry<CoinSafeMenu> COIN_SAFE = REGISTRATE
            .menu("coin_safe", (type, id, inv, buffer) -> {
                BlockPos pos = buffer.readBlockPos();
                CoinSafeBlockEntity safeEntity = (CoinSafeBlockEntity) inv.player.level().getBlockEntity(pos);
                return new CoinSafeMenu(id, inv, safeEntity);
            }, () -> CoinSafeScreen::new)
            .register();

    public static void register() {}
}