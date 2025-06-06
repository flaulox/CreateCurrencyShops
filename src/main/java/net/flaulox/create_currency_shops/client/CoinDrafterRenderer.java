package net.flaulox.create_currency_shops.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.flaulox.create_currency_shops.CreateCurrencyShopsPartialModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CoinDrafterRenderer extends PackagerRenderer {

    public CoinDrafterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(PackagerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
        int light, int overlay) {
        ItemStack renderedBox = be.getRenderedBox();
        float trayOffset = be.getTrayOffset(partialTicks);
        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(PackagerBlock.FACING).getOpposite();
        
        if (!VisualizationManager.supportsVisualization(be.getLevel())) {
            SuperByteBuffer sbb = CachedBuffers.partial(getCoinDrafterHatchModel(be), blockState);
            sbb.translate(Vec3.atLowerCornerOf(facing.getNormal()).scale(.49999f))
                .rotateYCenteredDegrees(AngleHelper.horizontalAngle(facing))
                .rotateXCenteredDegrees(AngleHelper.verticalAngle(facing))
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));

            sbb = CachedBuffers.partial(CreateCurrencyShopsPartialModels.COIN_DRAFTER_TRAY, blockState);
            sbb.translate(Vec3.atLowerCornerOf(facing.getNormal()).scale(trayOffset))
                .rotateYCenteredDegrees(facing.toYRot())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
        }

        if (!renderedBox.isEmpty()) {
            ms.pushPose();
            var msr = TransformStack.of(ms);
            msr.translate(Vec3.atLowerCornerOf(facing.getNormal()).scale(trayOffset))
                .translate(.5f, .5f, .5f)
                .rotateYDegrees(facing.toYRot())
                .translate(0, 2 / 16f, 0)
                .scale(1.49f, 1.49f, 1.49f);
            Minecraft.getInstance().getItemRenderer()
                .renderStatic(null, renderedBox, ItemDisplayContext.FIXED, false, ms, buffer, be.getLevel(), light, overlay, 0);
            ms.popPose();
        }
    }

    private static PartialModel getCoinDrafterHatchModel(PackagerBlockEntity be) {
        return isHatchOpen(be) ? CreateCurrencyShopsPartialModels.COIN_DRAFTER_HATCH_OPEN 
                               : CreateCurrencyShopsPartialModels.COIN_DRAFTER_HATCH;
    }
}
