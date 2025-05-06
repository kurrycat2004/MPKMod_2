package io.github.kurrycat.mpkmod.stonecutter.lex_forge;

import io.github.kurrycat.mpkmod.stonecutter.Tags;
import io.github.kurrycat.mpkmod.compatibility.API;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Tags.MOD_ID)
public class MPKModLexForge {
    @SubscribeEvent
    public void init(FMLCommonSetupEvent event) {
        API.LOGGER.info("Hello from LexForge!");
    }
}
