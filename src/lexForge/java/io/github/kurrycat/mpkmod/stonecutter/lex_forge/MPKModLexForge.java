package io.github.kurrycat.mpkmod.stonecutter.lex_forge;

import io.github.kurrycat.mpkmod.Tags;
import io.github.kurrycat.mpkmod.api.ModPlatform;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Tags.MOD_ID)
public class MPKModLexForge {
    @SubscribeEvent
    public void init(FMLCommonSetupEvent event) {
        ModPlatform.init();
    }
}
