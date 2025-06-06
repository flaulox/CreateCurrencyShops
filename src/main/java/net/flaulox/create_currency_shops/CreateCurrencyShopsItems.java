package net.flaulox.create_currency_shops;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.flaulox.create_currency_shops.items.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;

public class CreateCurrencyShopsItems {
    private static final CreateCurrencyShopsRegistrate REGISTRATE = CreateCurrencyShops.registrate();

    public static final ItemEntry<CurrencyItem> CURRENCY_ITEM =
            REGISTRATE.item("currency_item", CurrencyItem::new)
                    .lang("Coins")
                    .register();

    public static final ItemEntry<WriteDraftItem> WRITE_DRAFT_ITEM =
            REGISTRATE.item("write_draft_item", WriteDraftItem::new)
                    .lang("Write Coin Draft")
                    .register();

    public static final ItemEntry<CoinDraftItem> COIN_DRAFT =
            REGISTRATE.item("coin_draft", CoinDraftItem::new)
                    .properties(p -> p.stacksTo(1))
                    .lang("Coin Draft")
                    .register();

    public static final ItemEntry<CoinItem> BRASS_COIN =
            REGISTRATE.item("brass_coin", p -> new CoinItem(CreateCurrencyShopsBlocks.COIN_PILE.get(), p))
                    .lang("Brass Coin")
                    .register();

    public static final ItemEntry<CoinItem> COPPER_COIN =
            REGISTRATE.item("copper_coin", p -> new CoinItem(CreateCurrencyShopsBlocks.COIN_PILE.get(), p))
                    .lang("Copper Coin")
                    .register();

    public static final ItemEntry<CoinItem> GOLD_COIN =
            REGISTRATE.item("gold_coin", p -> new CoinItem(CreateCurrencyShopsBlocks.COIN_PILE.get(), p))
                    .lang("Gold Coin")
                    .register();

    public static final ItemEntry<CoinItem> IRON_COIN =
            REGISTRATE.item("iron_coin", p -> new CoinItem(CreateCurrencyShopsBlocks.COIN_PILE.get(), p))
                    .lang("Iron Coin")
                    .register();

    public static final ItemEntry<CoinItem> NETHERITE_COIN =
            REGISTRATE.item("netherite_coin", p -> new CoinItem(CreateCurrencyShopsBlocks.COIN_PILE.get(), p))
                    .lang("Netherite Coin")
                    .register();

    public static final ItemEntry<CoinItem> ZINC_COIN =
            REGISTRATE.item("zinc_coin", p -> new CoinItem(CreateCurrencyShopsBlocks.COIN_PILE.get(), p))
                    .lang("Zinc Coin")
                    .register();

    public static final ItemEntry<WalletItem> WALLET =
            REGISTRATE.item("wallet", WalletItem::new)
                    .properties(p -> p.stacksTo(1).component(DataComponents.CUSTOM_DATA,
                        CustomData.of(new CompoundTag() {{
                            putBoolean("UseCashFirst", false);
                        }})))
                    .lang("Wallet")
                    .register();

    public static final ItemEntry<CreditCardItem> CREDIT_CARD =
            REGISTRATE.item("credit_card", CreditCardItem::new)
                    .properties(p -> p.stacksTo(1))
                    .lang("Credit Card")
                    .register();

    public static void register() {}
}
