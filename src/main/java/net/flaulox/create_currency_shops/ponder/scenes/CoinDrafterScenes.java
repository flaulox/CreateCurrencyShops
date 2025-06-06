package net.flaulox.create_currency_shops.ponder.scenes;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.infrastructure.ponder.scenes.highLogistics.PonderHilo;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.ParrotPose;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.flaulox.create_currency_shops.CreateCurrencyShopsItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class CoinDrafterScenes {
    public static void intro(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("coin_drafter_intro", "Using the Coin Drafter");
        scene.configureBasePlate(0, 0, 8);

        BlockPos coin_safe = util.grid().at(5, 2, 2);
        Selection coin_safeS = util.select().position(coin_safe);

        BlockPos coin_safe2 = util.grid().at(5, 2, 6);
        Selection coin_safe2S = util.select().fromTo(coin_safe2, coin_safe2.west());

        BlockPos coin_drafter = util.grid().at(4, 2, 2);
        Selection coin_drafterS = util.select().position(coin_drafter);

        BlockPos stockKeeper = util.grid().at(2, 1, 4);
        Selection stockKeeperS = util.select().fromTo(stockKeeper, stockKeeper.west());


        BlockPos stockLink = util.grid().at(3, 2, 2);
        Selection stockLinkS = util.select().position(stockLink);

        Selection largeCogS = util.select().position(8, 0, 2);
        Selection beltSupportS = util.select().fromTo(8,1,2, 4, 1, 6);
        Selection funnelS = util.select().fromTo(4,2,3, 4, 2, 5);



        scene.showBasePlate();



        scene.idle(10);
        ElementLink<WorldSectionElement> coin_safeL = scene.world().showIndependentSection(coin_safeS, Direction.DOWN);
        scene.world().moveSection(coin_safeL, util.vector().of(0, -1, 2), 0);
        scene.idle(10);
        ElementLink<WorldSectionElement> coin_drafterL = scene.world().showIndependentSection(coin_drafterS, Direction.DOWN);
        scene.world().moveSection(coin_drafterL, util.vector().of(0, -1, 2), 0);
        scene.idle(10);
        scene.overlay()
                .showText(80)
                .text("Coin Drafters can be used to securely transfer Coins between Coin Safes")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(coin_drafter.south(2).below(), Direction.WEST));
        scene.idle(100);






        scene.world().moveSection(coin_safeL, util.vector().of(0, 0, -2), 20);
        scene.world().moveSection(coin_drafterL, util.vector().of(0, 0, -2), 20);
        scene.idle(20);
        ElementLink<WorldSectionElement> stockLinkL = scene.world().showIndependentSection(stockLinkS, Direction.DOWN);
        scene.world().moveSection(stockLinkL, util.vector().of(0, -1, 0), 0);
        scene.idle(10);
        ElementLink<WorldSectionElement> stockKeeperL = scene.world().showIndependentSection(stockKeeperS, Direction.DOWN);
        scene.idle(5);
        scene.special().createBirb(util.vector().centerOf(stockKeeper), ParrotPose.FacePointOfInterestPose::new);
        scene.idle(10);
        AABB bb2 = new AABB(stockLink);
        bb2 = bb2.deflate(1 / 16f)
                .contract(8 / 16f, 0, 0);
        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.BLUE, stockLink, bb2.move(0.5, -1, 0), 60);
        AABB bb3 = new AABB(stockKeeper.west());
        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.BLUE, stockKeeper.west(), bb3, 60);
        scene.overlay()
                .showLine(PonderPalette.BLUE, util.vector()
                                .blockSurface(stockLink.below(), Direction.EAST)
                                .subtract(0, 1 / 4f, 0),
                        util.vector()
                                .centerOf(stockKeeper.west())
                                .subtract(0, 1 / 4f, 0),
                        60);
        scene.idle(10);
        scene.overlay()
                .showText(60)
                .text("Link the Coin Drafter to a Storage Network ...")
                .colored(PonderPalette.BLUE)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(stockLink.below()));
        scene.idle(80);






        ItemStack writeDraftItem = new ItemStack(CreateCurrencyShopsItems.WRITE_DRAFT_ITEM.asItem());
        scene.overlay()
                .showControls(util.vector().centerOf(stockKeeper.above()), Pointing.DOWN, 80)
                .withItem(writeDraftItem);
        scene.idle(10);
        scene.overlay()
                .showText(90)
                .text("Open the Stock Keeper Interface to create a Coin Draft")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(stockKeeper.west(), Direction.WEST));
        scene.idle(50);
        PonderHilo.linkEffect(scene, stockLink.below());
        ItemStack box = PackageStyles.getDefaultBox()
                .copy();
        PackageItem.addAddress(box, "Warehouse");
        PonderHilo.packagerCreate(scene, coin_drafter, box);
        scene.idle(60);







        ItemStack draftItem = new ItemStack(CreateCurrencyShopsItems.COIN_DRAFT.asItem());
        scene.overlay()
                .showControls(util.vector().centerOf(stockLink.east().below(2)), Pointing.UP, 80)
                .withItem(draftItem);
        scene.idle(10);
        scene.overlay()
                .showText(80)
                .text("The Package contains a Coin Draft written to a specific Coin Safe")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(coin_drafter.below()));
        scene.idle(100);






        scene.idle(20);
        ElementLink<WorldSectionElement> coin_safe2L = scene.world().showIndependentSection(coin_safe2S, Direction.DOWN);
        scene.world().moveSection(coin_safe2L, util.vector().of(0, -1, 0), 0);
        scene.idle(20);
        scene.world().moveSection(coin_safeL, util.vector().of(0, 1, 0), 10);
        scene.world().moveSection(coin_drafterL, util.vector().of(0, 1, 0), 10);
        scene.world().moveSection(stockLinkL, util.vector().of(0, 1, 0), 10);
        scene.world().moveSection(coin_safe2L, util.vector().of(0, 1, 0), 10);
        scene.idle(5);


        scene.idle(10);
        scene.world().showIndependentSection(funnelS, Direction.DOWN);
        scene.world().showIndependentSection(beltSupportS, Direction.UP);
        scene.world().showIndependentSection(largeCogS, Direction.UP);
        scene.idle(20);

        scene.world().flapFunnel(util.grid().at(4, 2, 3), true);
        scene.world()
                .createItemOnBelt(util.grid()
                        .at(4, 1, 3), Direction.NORTH, box);
        PonderHilo.packagerClear(scene, coin_drafter);
        scene.idle(30);
        
        scene.world().flapFunnel(util.grid().at(4, 2, 5), false);
        scene.world().removeItemsFromBelt(util.grid().at(4, 1, 5));
        PonderHilo.packagerUnpack(scene, util.grid().at(4, 2, 6), box);
        scene.overlay()
                .showText(130)
                .text("It can only be inserted into the defined Coin Safe ...")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(coin_safe2.west(), Direction.WEST));
        scene.idle(60);
        scene.overlay()
                .showText(80)
                .text("... Or back into the one its issued from")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(coin_safe, Direction.WEST));
        


        scene.idle(100);



    }



    public static void writeDraft(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("coin_drafter_write_draft", "Writing a Coin Draft");
        scene.configureBasePlate(0, 0, 7);


        BlockPos coin_safe = util.grid().at(4, 1, 2);
        Selection coin_safeS = util.select().position(coin_safe);


        BlockPos coin_drafter = util.grid().at(3, 1, 2);
        Selection coin_drafterS = util.select().position(coin_drafter);

        BlockPos stockKeeper = util.grid().at(3, 1, 5);
        Selection stockKeeperS = util.select().fromTo(stockKeeper, stockKeeper.west());


        BlockPos stockLink = util.grid().at(2, 1, 2);
        Selection stockLinkS = util.select().position(stockLink);

        BlockPos sign = util.grid().at(3, 2, 2);
        Selection signS = util.select().position(sign);



        scene.showBasePlate();



        scene.idle(10);
        scene.world().showIndependentSection(coin_safeS, Direction.DOWN);
        scene.idle(10);
        scene.world().showIndependentSection(coin_drafterS, Direction.DOWN);
        scene.world().showIndependentSection(stockLinkS, Direction.DOWN);
        scene.idle(10);
        scene.world().showIndependentSection(stockKeeperS, Direction.DOWN);
        scene.idle(5);
        scene.special().createBirb(util.vector().centerOf(stockKeeper), ParrotPose.FacePointOfInterestPose::new);
        scene.idle(20);

        ItemStack writeDraftItem = new ItemStack(CreateCurrencyShopsItems.WRITE_DRAFT_ITEM.asItem());
        scene.overlay()
                .showControls(util.vector().centerOf(stockKeeper.above()), Pointing.DOWN, 80)
                .withItem(writeDraftItem);
        scene.idle(10);
        scene.overlay()
                .showText(110)
                .text("When ordering a Coin Draft ...")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(stockKeeper.west(), Direction.UP));
        scene.idle(40);
        scene.overlay()
                .showText(80)
                .text("specify the Address and Coin Safe ID it should be written to: ...")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(coin_drafter, Direction.WEST));
        scene.idle(100);






        scene.overlay()
                .showText(80)
                .text("Request to:\n\n→ Alice$ABC1")
                .colored(PonderPalette.BLUE)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(stockKeeper.west(), Direction.WEST));
        scene.idle(50);
        PonderHilo.linkEffect(scene, stockLink.below());
        ItemStack box = PackageStyles.getDefaultBox()
                .copy();
        PackageItem.addAddress(box, "Warehouse");
        PonderHilo.packagerCreate(scene, coin_drafter, box);
        scene.idle(60);
        scene.overlay()
                .showText(60)
                .text("Package Address:\n\n→ Alice")
                .colored(PonderPalette.OUTPUT)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(coin_drafter));
        scene.idle(80);
        scene.overlay()
                .showText(60)
                .text("Coin Draft\n\nWritten to: $ABC1")
                .colored(PonderPalette.OUTPUT)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(coin_drafter));
        ItemStack draftItem = new ItemStack(CreateCurrencyShopsItems.COIN_DRAFT.asItem());
        scene.overlay()
                .showControls(util.vector().centerOf(coin_drafter.below()), Pointing.UP, 40)
                .withItem(draftItem);
        scene.idle(40);
        PonderHilo.packagerUnpack(scene, coin_drafter, box);
        scene.idle(60);







        scene.overlay()
                .showText(80)
                .text("Request to:\n\n→ Alice")
                .colored(PonderPalette.BLUE)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(stockKeeper.west(), Direction.WEST));
        scene.idle(50);
        PonderHilo.linkEffect(scene, stockLink.below());
        PonderHilo.packagerCreate(scene, coin_drafter, box);
        scene.idle(60);
        scene.overlay()
                .showText(60)
                .text("Package Address:\n\n→ Alice")
                .colored(PonderPalette.OUTPUT)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(coin_drafter));
        scene.idle(80);
        scene.overlay()
                .showText(60)
                .text("Coin Draft\n\nWritten to: Any")
                .colored(PonderPalette.OUTPUT)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(coin_drafter));
        scene.overlay()
                .showControls(util.vector().centerOf(coin_drafter.below()), Pointing.UP, 40)
                .withItem(draftItem);
        scene.idle(40);
        PonderHilo.packagerUnpack(scene, coin_drafter, box);
        scene.idle(60);







        scene.overlay()
                .showText(80)
                .text("Request to:\n\n→ $ABC1")
                .colored(PonderPalette.BLUE)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(stockKeeper.west(), Direction.WEST));
        scene.idle(50);
        PonderHilo.linkEffect(scene, stockLink.below());
        PonderHilo.packagerCreate(scene, coin_drafter, box);
        scene.idle(60);
        scene.overlay()
                .showText(60)
                .text("Package Address:\n\n→")
                .colored(PonderPalette.OUTPUT)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(coin_drafter));
        scene.idle(80);
        scene.overlay()
                .showText(60)
                .text("Coin Draft\n\nWritten to: $ABC1")
                .colored(PonderPalette.OUTPUT)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(coin_drafter));
        scene.overlay()
                .showControls(util.vector().centerOf(coin_drafter.below()), Pointing.UP, 40)
                .withItem(draftItem);
        scene.idle(40);
        PonderHilo.packagerUnpack(scene, coin_drafter, box);
        scene.idle(80);



        scene.world().showIndependentSection(signS,Direction.DOWN);
        scene.idle(20);
        scene.overlay()
                .showText(60)
                .text("Signs act as Fallback for missing Information")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(sign));
        scene.idle(80);
        scene.overlay()
                .showText(60)
                .text("Bob$XYZ2")
                .colored(PonderPalette.BLUE)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(sign));
        scene.idle(100);



        scene.overlay()
                .showText(80)
                .text("Request to:\n\n→ $ABC1")
                .colored(PonderPalette.BLUE)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(stockKeeper.west(), Direction.WEST));
        scene.idle(50);
        PonderHilo.linkEffect(scene, stockLink.below());
        PonderHilo.packagerCreate(scene, coin_drafter, box);
        scene.idle(60);
        scene.overlay()
                .showText(60)
                .text("Package Address:\n\n→ Bob")
                .colored(PonderPalette.OUTPUT)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(coin_drafter));
        scene.idle(80);
        scene.overlay()
                .showText(60)
                .text("Coin Draft\n\nWritten to: $ABC1")
                .colored(PonderPalette.OUTPUT)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(coin_drafter));
        scene.overlay()
                .showControls(util.vector().centerOf(coin_drafter.below()), Pointing.UP, 40)
                .withItem(draftItem);
        scene.idle(40);
        PonderHilo.packagerUnpack(scene, coin_drafter, box);
        scene.idle(80);


    }
}
