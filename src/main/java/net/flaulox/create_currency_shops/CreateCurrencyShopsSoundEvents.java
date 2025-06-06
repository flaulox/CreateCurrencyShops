package net.flaulox.create_currency_shops;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreateCurrencyShopsSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = 
        DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, CreateCurrencyShops.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> COIN_SAFE_DOOR_OPEN = 
        SOUND_EVENTS.register("coin_safe_door_open", () -> 
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(CreateCurrencyShops.MODID, "coin_safe_door_open")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COIN_SAFE_DOOR_CLOSE = 
        SOUND_EVENTS.register("coin_safe_door_close", () -> 
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(CreateCurrencyShops.MODID, "coin_safe_door_close")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COIN_SAFE_WHEEL_OPEN = 
        SOUND_EVENTS.register("coin_safe_wheel_open", () -> 
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(CreateCurrencyShops.MODID, "coin_safe_wheel_open")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COIN_SAFE_WHEEL_CLOSE = 
        SOUND_EVENTS.register("coin_safe_wheel_close", () -> 
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(CreateCurrencyShops.MODID, "coin_safe_wheel_close")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COIN_SHAKE = 
        SOUND_EVENTS.register("coin_shake", () -> 
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(CreateCurrencyShops.MODID, "coin_shake")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COIN_PING = 
        SOUND_EVENTS.register("coin_ping", () -> 
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(CreateCurrencyShops.MODID, "coin_ping")));
}
