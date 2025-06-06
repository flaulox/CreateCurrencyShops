package net.flaulox.create_currency_shops.util;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import net.flaulox.create_currency_shops.blocks.CoinSafeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class LogisticsHelper {
    
    public static boolean isNetworkLocked(Level level, BlockPos pos, Player player) {
        // Check coin safe internal lock first
        if (level.getBlockEntity(pos) instanceof CoinSafeBlockEntity coinSafe) {
            if (!coinSafe.canAccess(player)) {
                if (!level.isClientSide) {
                    AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
                    player.displayClientMessage(Component.translatable("block.create_currency_shops.coin_safe_protected")
                        .withColor(ChatFormatting.RED.getColor()) ,true);
                }
                return true;
            }
        }
        
        // Check logistics network lock
        for (Direction direction : Direction.values()) {
            BlockPos packagerPos = pos.relative(direction);
            if (level.getBlockEntity(packagerPos) instanceof PackagerBlockEntity packager) {
                if (packager.targetInventory.getTarget().getFace() == direction.getOpposite()) {
                    for (Direction linkDir : Direction.values()) {
                        BlockPos linkPos = packagerPos.relative(linkDir);
                        if (level.getBlockEntity(linkPos) instanceof PackagerLinkBlockEntity stockLink) {
                            if (stockLink.getPackager() == packager && stockLink.behaviour != null && !stockLink.behaviour.mayInteract(player)) {
                                if (!level.isClientSide) {
                                    AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
                                    player.displayClientMessage(CreateLang.translate("logistically_linked.protected")
                                        .style(ChatFormatting.RED)
                                        .component(), true);
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
