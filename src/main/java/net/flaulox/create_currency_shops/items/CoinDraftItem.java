package net.flaulox.create_currency_shops.items;

import com.simibubi.create.foundation.item.ItemDescription;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;
import java.util.UUID;

import static com.simibubi.create.foundation.item.TooltipHelper.styleFromColor;

public class CoinDraftItem extends Item {
    public CoinDraftItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {

        
        int value = getValue(stack);
        UUID writtenTo = getWrittenTo(stack);
        UUID issuedFrom = getIssuedFrom(stack);
        
        tooltipComponents.add(Component.translatable("gui.create_currency_shops.value")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(" " + String.format("%,d", value))
            .withStyle(ChatFormatting.GOLD)));

        tooltipComponents.add(Component.literal(""));
        
        if (writtenTo != null) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            String fullText = tag.getString("WrittenToReadableID");
            String name = tag.getString("WrittenToName");
            String code = tag.getString("WrittenToCode");
            tooltipComponents.add(Component.translatable("tooltip.create_currency_shops.written_to").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(" ").append(formatNameAndCode(name, code))));
        }
        
        if (issuedFrom != null) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            String name = tag.getString("IssuedFromName");
            String code = tag.getString("IssuedFromCode");
            tooltipComponents.add(Component.translatable("tooltip.create_currency_shops.issued_from").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(" ").append(formatNameAndCode(name, code))));
        }

        ItemDescription description = ItemDescription.create(this, FontHelper.Palette.STANDARD_CREATE);
        if (description != null) {
            tooltipComponents.addAll(description.getCurrentLines());
        }
    }

    private static Component formatNameAndCode(String name, String code) {
        if (name.isEmpty() || code.isEmpty()) {
            return Component.literal(name.isEmpty() ? code : name).withStyle(styleFromColor(0xF1DD79));
        }
        return Component.literal(name).withStyle(styleFromColor(0xF1DD79))
                .append(Component.literal(" " + code).withStyle(ChatFormatting.DARK_GRAY));
    }

    public static int getValue(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getInt("Value");
    }

    public static UUID getWrittenTo(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.hasUUID("WrittenTo") ? tag.getUUID("WrittenTo") : null;
    }

    public static UUID getIssuedFrom(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.hasUUID("IssuedFrom") ? tag.getUUID("IssuedFrom") : null;
    }

    public static void setValue(ItemStack stack, int value) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("Value", value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void setWrittenTo(ItemStack stack, UUID uuid, String name, String code) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putUUID("WrittenTo", uuid);
        tag.putString("WrittenToName", name);
        tag.putString("WrittenToCode", code);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void setIssuedFrom(ItemStack stack, UUID uuid, String name, String code) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putUUID("IssuedFrom", uuid);
        tag.putString("IssuedFromName", name);
        tag.putString("IssuedFromCode", code);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

}
