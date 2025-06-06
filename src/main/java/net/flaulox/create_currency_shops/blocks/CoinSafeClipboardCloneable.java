package net.flaulox.create_currency_shops.blocks;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.ClipboardContent;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides.ClipboardType;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public interface CoinSafeClipboardCloneable {
    String getShortID();
    String getCustomName();
    
    default void addAddressToClipboard(Player player, ItemStack clipboard) {
        String address = getShortID();
        if (address == null || address.isBlank())
            return;
        
        String name = getCustomName();
        String fullText = name.isEmpty() ? "#$" + address : "#" + name + "$" + address;

        ClipboardContent content = clipboard.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
        List<List<ClipboardEntry>> list = ClipboardEntry.readAll(content);
        
        for (List<ClipboardEntry> page : list) {
            for (ClipboardEntry entry : page) {
                if (entry.text.getString().equals(fullText))
                    return;
            }
        }

        List<ClipboardEntry> page = null;
        for (List<ClipboardEntry> freePage : list) {
            if (freePage.size() > 11)
                continue;
            page = freePage;
            break;
        }

        if (page == null) {
            page = new ArrayList<>();
            list.add(page);
        }

        page.add(new ClipboardEntry(false, Component.literal(fullText)));
        player.displayClientMessage(CreateLang.translate("clipboard.address_added", fullText.substring(1)).component(), true);

        content = content.setPages(list).setType(ClipboardType.WRITTEN);
        clipboard.set(AllDataComponents.CLIPBOARD_CONTENT, content);
    }
}
