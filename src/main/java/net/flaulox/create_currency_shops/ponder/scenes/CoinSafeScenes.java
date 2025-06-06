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
import net.flaulox.create_currency_shops.items.WalletItem;
import net.flaulox.create_currency_shops.blocks.CoinSafeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

public class CoinSafeScenes {
    public static void intro(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("coin_safe_intro", "Using the Coin Safe");
        scene.configureBasePlate(0, 0, 5);

        BlockPos coin_safe = util.grid().at(2, 1, 2);
        Selection coin_safeS = util.select().position(coin_safe);


        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(coin_safeS, Direction.DOWN);
        scene.idle(10);

        ItemStack brass_coin = new ItemStack(CreateCurrencyShopsItems.BRASS_COIN.asItem());
        scene.overlay()
                .showControls(util.vector().of(3, 1.5,2), Pointing.RIGHT, 60)
                .withItem(brass_coin);
        scene.idle(10);
        scene.overlay()
                .showText(60)
                .text("Coin Safes securely store Coins")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(coin_safe, Direction.WEST));
        scene.idle(80);







        scene.overlay()
                .showControls(util.vector()
                        .topOf(coin_safe), Pointing.DOWN, 60)
                .rightClick();
        scene.idle(5);
        scene.world().modifyBlockEntity(coin_safe, CoinSafeBlockEntity.class, be -> be.openTracker.openCount++);
        scene.idle(10);
        scene.overlay()
                .showText(60)
                .text("Open the Interface to deposit or withdraw Coins")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(coin_safe, Direction.WEST));
        scene.idle(50);
        scene.world().modifyBlockEntity(coin_safe, CoinSafeBlockEntity.class, be -> be.openTracker.openCount = 0);
        scene.idle(30);







        ItemStack iron_coin = new ItemStack(CreateCurrencyShopsItems.IRON_COIN.asItem());
        scene.overlay()
                .showControls(util.vector().of(3, 2.2,2), Pointing.RIGHT, 60)
                .withItem(iron_coin);
        ItemStack copper_coin = new ItemStack(CreateCurrencyShopsItems.COPPER_COIN.asItem());
        scene.overlay()
                .showControls(util.vector().of(3, 0.7,2), Pointing.RIGHT, 60)
                .withItem(copper_coin);

        scene.idle(10);
        scene.overlay()
                .showText(60)
                .text("Stored coins can be withdrawn in any Denomination")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(coin_safe, Direction.WEST));
        scene.idle(80);






        ItemStack walletItem = CreateCurrencyShopsItems.WALLET.asStack();
        WalletItem.addCoins(walletItem, CreateCurrencyShopsItems.BRASS_COIN.get(), 1);
        scene.overlay()
                .showControls(util.vector()
                        .blockSurface(coin_safe.above(), Direction.DOWN), Pointing.DOWN, 80)
                .rightClick()
                .withItem(walletItem);
        scene.idle(10);
        scene.overlay()
                .showText(80)
                .text("Shift-Right-Click a Wallet onto a Coin Safe to deposit all its content")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(coin_safe, Direction.WEST));
        scene.idle(100);







        AABB bb1 = new AABB(coin_safe);
        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.RED, coin_safe, bb1.deflate(0.25), 10);
        scene.idle(1);

        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.RED, coin_safe, bb1, 270);
        scene.idle(10);
        scene.overlay()
                .showText(60)
                .text("The Coin Safe can be locked from within the Interface")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(coin_safe, Direction.WEST));
        scene.idle(80);











        scene.overlay()
                .showControls(util.vector().of(3, 1.5,2), Pointing.RIGHT, 70)
                .rightClick();
        scene.idle(10);
        scene.effects().indicateRedstone(coin_safe);
        scene.idle(10);
        scene.overlay()
                .showText(60)
                .text("Once locked, only the Owner can access and rename the Coin Safe")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(coin_safe, Direction.WEST));
        scene.idle(80);





        ItemStack pickaxeItem = Items.NETHERITE_PICKAXE.getDefaultInstance();
        scene.overlay()
                .showControls(util.vector().of(3, 1.5,2), Pointing.RIGHT, 60)
                .leftClick()
                .withItem(pickaxeItem);
        scene.idle(5);
        ItemStack barrierItem = Items.BARRIER.getDefaultInstance();
        scene.overlay()
                .showControls(util.vector().blockSurface(coin_safe.above(), Direction.DOWN), Pointing.DOWN, 60)
                .withItem(barrierItem);
        scene.idle(10);
        scene.overlay()
                .showText(60)
                .text("It also cannot be broken by explosions, contraptions, or other Players")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(coin_safe, Direction.WEST));
        scene.idle(80);


    }


    public static void shopping(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("coin_safe_shopping", "Using the Coin Safe");
        scene.configureBasePlate(0, 0, 7);

        BlockPos coin_safe = util.grid().at(4, 1, 2);
        Selection coin_safeS = util.select().position(coin_safe);

        BlockPos stockKeeper = util.grid().at(3, 1, 5);
        Selection stockKeeperS = util.select().fromTo(stockKeeper, stockKeeper.west());

        BlockPos tableCloth = util.grid().at(1, 2, 2);
        Selection tableClothS = util.select().fromTo(tableCloth, tableCloth.below());

        BlockPos stockLink = util.grid().at(2, 1, 2);
        Selection stockLinkS = util.select().fromTo(stockLink, stockLink.east());


        scene.showBasePlate();
        scene.idle(10);
        ElementLink<WorldSectionElement> coin_safeL = scene.world().showIndependentSection(coin_safeS, Direction.DOWN);
        scene.world().moveSection(coin_safeL, util.vector().of(-1, 0, 1), 0);
        scene.idle(10);








        ItemStack creditCardItem = new ItemStack(CreateCurrencyShopsItems.CREDIT_CARD.asItem());
        scene.overlay()
                .showControls(util.vector().of(4, 1.5,3), Pointing.RIGHT, 60)
                .rightClick()
                .withItem(creditCardItem);
        scene.idle(10);
        scene.overlay()
                .showText(60)
                .text("Credit Cards can be linked to an unlocked Coin Safe")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(coin_safe.west().south(), Direction.WEST));
        scene.idle(80);






        scene.world().moveSection(coin_safeL, util.vector().of(1, 0, -1), 10);
        scene.idle(5);
        scene.world().showIndependentSection(stockKeeperS, Direction.DOWN);
        scene.idle(5);
        scene.special().createBirb(util.vector().centerOf(stockKeeper), ParrotPose.FacePointOfInterestPose::new);
        scene.idle(5);
        ElementLink<WorldSectionElement> tableClothL = scene.world().showIndependentSection(tableClothS, Direction.DOWN);
        scene.idle(10);
        scene.overlay()
                .showControls(util.vector().centerOf(stockKeeper.above()), Pointing.DOWN, 100)
                .withItem(creditCardItem);
        scene.idle(10);

        AABB bb1 = new AABB(coin_safe);
        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.GREEN, coin_safe, bb1, 100);

        scene.overlay()
                .showLine(PonderPalette.GREEN, util.vector()
                                .centerOf(coin_safe)
                                .subtract(0, 1 / 4f, 0),
                        util.vector()
                                .centerOf(stockKeeper.west())
                                .subtract(0, 1 / 4f, 0),
                        100);
        scene.idle(10);
        scene.overlay()
                .showText(100)
                .text("Buying from Shops with a Credit Card inside the Inventory automatically draws Coins from the linked Coin Safe")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(tableCloth));
        scene.idle(120);









        scene.overlay()
                .showText(100)
                .text("Payments are deducted from the Player in the following order:\nCoins → Wallet → Credit Card")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(tableCloth));
        scene.idle(120);






        scene.world().hideIndependentSection(tableClothL, Direction.UP);
        scene.idle(20);
        scene.world().showIndependentSection(stockLinkS, Direction.DOWN);
        scene.idle(10);
        AABB bb2 = new AABB(stockLink);
        bb2 = bb2.deflate(1 / 16f)
                .contract(8 / 16f, 0, 0);
        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.BLUE, stockLink, bb2.move(0.5, 0, 0), 150);
        AABB bb3 = new AABB(stockKeeper.west());
        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.BLUE, stockKeeper.west(), bb3, 150);
        scene.overlay()
                .showLine(PonderPalette.BLUE, util.vector()
                                .blockSurface(stockLink, Direction.EAST)
                                .subtract(0, 1 / 4f, 0),
                        util.vector()
                                .centerOf(stockKeeper.west())
                                .subtract(0, 1 / 4f, 0),
                        150);
        scene.idle(10);
        scene.overlay()
                .showText(150)
                .text("When attaching the Coin Safe to a Stock Network ...")
                .colored(PonderPalette.BLUE)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(stockLink));

        scene.idle(40);

        ItemStack currencyItem = new ItemStack(CreateCurrencyShopsItems.CURRENCY_ITEM.asItem());
        scene.overlay()
                .showControls(util.vector().centerOf(stockKeeper.above()), Pointing.DOWN, 120)
                .withItem(currencyItem);
        scene.idle(10);
        scene.overlay()
                .showText(120)
                .text("... It creates a Bank Account in the Stock Keeper Interface")
                .colored(PonderPalette.BLUE)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(stockKeeper.west(), Direction.WEST));

        scene.idle(80);
        PonderHilo.linkEffect(scene, stockLink);
        ItemStack box = PackageStyles.getDefaultBox()
                .copy();
        PackageItem.addAddress(box, "Warehouse");
        PonderHilo.packagerCreate(scene, stockLink.east(), box);
        scene.idle(15);
        ItemStack copperCoinItem = new ItemStack(CreateCurrencyShopsItems.COPPER_COIN.asItem());
        scene.overlay()
                .showControls(util.vector().centerOf(stockLink.east().below()), Pointing.UP, 30)
                .withItem(copperCoinItem);
        scene.idle(40);

        PonderHilo.packagerClear(scene, stockLink.east());
        scene.idle(20);









        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.RED, stockKeeper.west(), bb3.deflate(0.25), 10);
        scene.idle(1);
        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.RED, stockKeeper.west(), bb3, 120);
        scene.idle(10);

        scene.overlay()
                .showText(120)
                .text("When locking the Stock Keeper ...")
                .colored(PonderPalette.RED)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(stockKeeper.west(), Direction.WEST));
        scene.idle(40);
        scene.effects().indicateRedstone(coin_safe);
        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.RED, coin_safe, bb1.deflate(0.25), 10);
        scene.idle(1);
        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.RED, coin_safe, bb1, 90);
        scene.idle(10);
        scene.overlay()
                .showText(90)
                .text("... It also locks the Coin Safe")
                .colored(PonderPalette.RED)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(coin_safe, Direction.WEST));
        scene.idle(110);








        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.RED, coin_safe, bb1.deflate(0.25), 10);
        scene.idle(1);
        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.RED, coin_safe, bb1, 150);
        scene.idle(10);
        scene.overlay()
                .showText(150)
                .text("When locking only the Coin Safe ...")
                .colored(PonderPalette.RED)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(coin_safe, Direction.WEST));
        scene.idle(40);
        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.GREEN, stockKeeper.west(), bb3.deflate(0.25), 10);
        scene.idle(1);
        scene.effects().indicateSuccess(stockKeeper);
        scene.overlay()
                .chaseBoundingBoxOutline(PonderPalette.GREEN, stockKeeper.west(), bb3, 120);
        scene.idle(10);
        scene.overlay()
                .showText(120)
                .text("... It keeps the Stock Keeper accessible, but restricts Bank Account access to the Owner")
                .colored(PonderPalette.GREEN)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(stockKeeper.west(), Direction.UP));
        scene.idle(140);





    }

}
