package net.flaulox.create_currency_shops.items;

import com.simibubi.create.foundation.item.ItemDescription;
import net.createmod.catnip.lang.FontHelper;
import net.flaulox.create_currency_shops.CreateCurrencyShops;
import net.flaulox.create_currency_shops.util.FeatureToggle;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

import static com.simibubi.create.foundation.item.TooltipHelper.styleFromColor;

public class CurrencyItem extends Item {
    public CurrencyItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("SafeName")) {
            String safeName = tag.getString("SafeName");
            String safeCode = tag.getString("SafeCode");
            if (FeatureToggle.isEnabled(CreateCurrencyShops.asResource("coin_drafter"))) {
                tooltipComponents.add(Component.translatable("tooltip.create_currency_shops.coin_safe_source")
                        .append(" ").append(Component.literal(safeName).withStyle(styleFromColor(0xF1DD79)))
                        .append(" ").append(Component.literal("$" + safeCode).withStyle(ChatFormatting.DARK_GRAY))
                        .withStyle(ChatFormatting.GRAY));
            } else {
                tooltipComponents.add(Component.translatable("tooltip.create_currency_shops.coin_safe_source")
                        .append(" ").append(Component.literal(safeName).withStyle(styleFromColor(0xF1DD79)))
                        .withStyle(ChatFormatting.GRAY));
            }

        }

        ItemDescription description = ItemDescription.create(this, FontHelper.Palette.STANDARD_CREATE);
        if (description != null) {
            tooltipComponents.addAll(description.getCurrentLines());
        }
    }

}
