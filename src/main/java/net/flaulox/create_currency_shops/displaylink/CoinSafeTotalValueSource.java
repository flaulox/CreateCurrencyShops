package net.flaulox.create_currency_shops.displaylink;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.flaulox.create_currency_shops.blocks.CoinSafeBlockEntity;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

public class CoinSafeTotalValueSource extends SingleLineDisplaySource {
    
    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof CoinSafeBlockEntity safe))
            return EMPTY_LINE;
        return Component.literal(String.valueOf(safe.getTotalValue()));
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }

    @Override
    protected String getTranslationKey() {
        return "coin_safe_total_value";
    }


}
