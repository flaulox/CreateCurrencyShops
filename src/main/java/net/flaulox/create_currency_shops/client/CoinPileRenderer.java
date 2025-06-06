package net.flaulox.create_currency_shops.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.flaulox.create_currency_shops.blocks.CoinPileBlock;
import net.flaulox.create_currency_shops.blocks.CoinPileBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CoinPileRenderer implements BlockEntityRenderer<CoinPileBlockEntity> {
    private final ItemRenderer itemRenderer;

    public CoinPileRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(CoinPileBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (blockEntity.coins.isEmpty()) return;
        
        float[][] positions = {
            {18/32f, 0f, 18/32f, 0f},
            {8/32f, 0f, 24/32f, 0f},
            {26/32f, 0f, 26/32f, 45f},
            {24/32f, 0f, 8/32f, 0f},
            {10/32f, 0f, 10/32f, 45f},

            {18/32f, 1/32f, 18/32f, 0f},
            {8/32f, 1/32f, 24/32f, 0f},
            {26/32f, 1/32f, 26/32f, 45f},
            {24/32f, 1/32f, 8/32f, 0f},
            {10/32f, 1/32f, 10/32f, 45f},

            {18/32f, 2/32f, 18/32f, 0f},
            {8/32f, 2/32f, 24/32f, 0f},
            {26/32f, 2/32f, 26/32f, 45f},
            {24/32f, 2/32f, 8/32f, 0f},
            {18/32f, 3/32f, 18/32f, 0f},

            {18/32f, 4/32f, 18/32f, 0f},
            {8/32f, 3/32f, 24/32f, 0f},
            {26/32f, 3/32f, 26/32f, 45f},
            {18/32f, 5/32f, 18/32f, 0f},
            {8/32f, 4/32f, 24/32f, 0f},

            {18/32f, 6/32f, 18/32f, 0f},
            {8/32f, 5/32f, 24/32f, 0f},
            {26/32f, 4/32f, 26/32f, 45f},
            {24/32f, 3/32f, 8/32f, 0f},
            {10/32f, 2/32f, 10/32f, 45f},

            {18/32f, 7/32f, 18/32f, 0f},
            {8/32f, 6/32f, 24/32f, 0f},
            {26/32f, 5/32f, 26/32f, 45f},
            {24/32f, 4/32f, 8/32f, 0f},
            {18/32f, 8/32f, 18/32f, 0f},

            {18/32f, 9/32f, 18/32f, 0f},
            {8/32f, 7/32f, 24/32f, 0f}




        };
        
        Direction facing = blockEntity.getBlockState().getValue(CoinPileBlock.FACING);
        float rotation = switch (facing) {
            case NORTH -> 180;
            case SOUTH -> 0;
            case WEST -> 270;
            case EAST -> 90;
            default -> 0;
        };
        
        long seed = blockEntity.getBlockPos().asLong();
        java.util.Random random = new java.util.Random(seed);
        
        for (int i = 0; i < Math.min(blockEntity.coins.size(), positions.length); i++) {
            ItemStack coinStack = new ItemStack(blockEntity.coins.get(i));
            float[] pos = positions[i];
            
            float offsetX = (random.nextInt(3) - 1) / 64f;
            float offsetZ = (random.nextInt(3) - 1) / 64f;
            float rotOffset = (random.nextFloat() - 0.5f) * 10f;
            
            poseStack.pushPose();
            poseStack.translate(0.5, 0, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.translate(-0.5, 0, -0.5);
            poseStack.translate(pos[0] + offsetX, pos[1] + 1/64f, pos[2] + offsetZ);
            poseStack.mulPose(Axis.YP.rotationDegrees(pos[3] + rotOffset));
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            poseStack.translate(0, -4/32f, 0);
            poseStack.scale(1f, 1f, 1f);
            
            itemRenderer.renderStatic(coinStack, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, poseStack, buffer, blockEntity.getLevel(), 0);
            
            poseStack.popPose();
        }
    }
}
