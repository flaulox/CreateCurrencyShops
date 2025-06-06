package net.flaulox.create_currency_shops.client;

import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.content.logistics.packager.PackagerRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.createmod.catnip.math.AngleHelper;
import net.flaulox.create_currency_shops.CreateCurrencyShopsPartialModels;
import net.flaulox.create_currency_shops.blocks.CoinDrafterBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class CoinDrafterVisual extends AbstractBlockEntityVisual<CoinDrafterBlockEntity> implements SimpleDynamicVisual {
    
    public final TransformedInstance hatch;
    public final TransformedInstance tray;
    public float lastTrayOffset = Float.NaN;
    public PartialModel lastHatchPartial;

    public CoinDrafterVisual(VisualizationContext ctx, CoinDrafterBlockEntity blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);
        
        lastHatchPartial = getCoinDrafterHatchModel(blockEntity);
        hatch = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(lastHatchPartial))
            .createInstance();
        tray = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(CreateCurrencyShopsPartialModels.COIN_DRAFTER_TRAY))
            .createInstance();
        
        Direction facing = blockState.getValue(PackagerBlock.FACING).getOpposite();
        var lowerCorner = Vec3.atLowerCornerOf(facing.getNormal());
        
        hatch.setIdentityTransform()
            .translate(getVisualPosition())
            .translate(lowerCorner.scale(.49999f))
            .rotateYCenteredDegrees(AngleHelper.horizontalAngle(facing))
            .rotateXCenteredDegrees(AngleHelper.verticalAngle(facing))
            .setChanged();
        
        animate(partialTick);
    }

    @Override
    public void beginFrame(Context ctx) {
        animate(ctx.partialTick());
    }

    public void animate(float partialTick) {
        var hatchPartial = getCoinDrafterHatchModel(blockEntity);
        if (hatchPartial != this.lastHatchPartial) {
            instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(hatchPartial))
                .stealInstance(hatch);
            this.lastHatchPartial = hatchPartial;
        }
        
        float trayOffset = blockEntity.getTrayOffset(partialTick);
        if (trayOffset == lastTrayOffset)
            return;
        
        Direction facing = blockState.getValue(PackagerBlock.FACING).getOpposite();
        var lowerCorner = Vec3.atLowerCornerOf(facing.getNormal());
        
        tray.setIdentityTransform()
            .translate(getVisualPosition())
            .translate(lowerCorner.scale(trayOffset))
            .rotateYCenteredDegrees(facing.toYRot())
            .setChanged();
        
        lastTrayOffset = trayOffset;
    }

    private static PartialModel getCoinDrafterHatchModel(CoinDrafterBlockEntity be) {
        return PackagerRenderer.isHatchOpen(be)
            ? CreateCurrencyShopsPartialModels.COIN_DRAFTER_HATCH_OPEN 
            : CreateCurrencyShopsPartialModels.COIN_DRAFTER_HATCH;
    }

    @Override
    protected void _delete() {
        hatch.delete();
        tray.delete();
    }

    @Override
    public void collectCrumblingInstances(java.util.function.Consumer<dev.engine_room.flywheel.api.instance.Instance> consumer) {
        consumer.accept(hatch);
        consumer.accept(tray);
    }

    @Override
    public void updateLight(float partialTick) {
        relight(hatch, tray);
    }
}
