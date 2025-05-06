package io.github.kurrycat.mpkmod.stonecutter.neoforge;

import io.github.kurrycat.mpkmod.Tags;
import io.github.kurrycat.mpkmod.api.ModPlatform;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Tags.MOD_ID)
@EventBusSubscriber(modid = Tags.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class MPKModNeoForge {
    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        ModPlatform.init();
    }
}
