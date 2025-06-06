package net.flaulox.create_currency_shops.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.flaulox.create_currency_shops.CreateCurrencyShopsPartialModels;
import net.flaulox.create_currency_shops.blocks.CoinSafeBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CoinSafeRenderer extends SmartBlockEntityRenderer<CoinSafeBlockEntity> {

    public CoinSafeRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(CoinSafeBlockEntity blockEntity, float partialTicks, PoseStack ms,
                               MultiBufferSource buffer, int light, int overlay) {
        BlockState blockState = blockEntity.getBlockState();
        float doorAngle = blockEntity.door.getValue(partialTicks);
        float wheelAngle = blockEntity.wheel.getValue(partialTicks);
        Direction facing = blockState.getValue(HorizontalDirectionalBlock.FACING).getOpposite();
        
        SuperByteBuffer door = CachedBuffers.partial(CreateCurrencyShopsPartialModels.COIN_SAFE_DOOR, blockState);
        VertexConsumer builder = buffer.getBuffer(RenderType.cutoutMipped());
        
        door.center()
            .rotateYDegrees(-facing.toYRot())
            .uncenter()
            .translate(14/16f, 1/16f, 1/16f)
            .rotateYDegrees(-45 * doorAngle)
            .translate(-14/16f, -1/16f, -1/16f)
            .light(light)
            .renderInto(ms, builder);
        
        SuperByteBuffer wheel = CachedBuffers.partial(CreateCurrencyShopsPartialModels.COIN_SAFE_WHEEL, blockState);
        wheel.center()
            .rotateYDegrees(-facing.toYRot())
            .uncenter()
            .translate(14/16f, 1/16f, 1/16f)
            .rotateYDegrees(-45 * doorAngle)
            .translate(-14/16f, -1/16f, -1/16f)
            .translate(8/16f, 9/16f, 0.5f/16f)
            .rotateZDegrees(wheelAngle * -180)
            .translate(-8/16f, -9/16f, -0.5f/16f)
            .light(light)
            .renderInto(ms, builder);
    }
}
