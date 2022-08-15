package io.github.kurrycat.mpkmod.compatability.MC1_19;

import io.github.kurrycat.mpkmod.compatability.API;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(API.MODID)
public class MPKMod_1_19 {
    public KeyBinding keyBinding;
    private MPKGuiScreen_1_14 gui;

    public MPKMod_1_19() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
    }

    public MPKGuiScreen_1_19 getGui() {
        if (gui == null)
            gui = new MPKGuiScreen_1_14(API.getGuiScreen());
        return gui;
    }

    public void init(FMLCommonSetupEvent event) {
        keyBinding = new KeyBinding(
                API.MODID + ".key.gui.desc",
                -1,
                API.KEYBINDING_CATEGORY
        );

        API.init(Minecraft.getInstance().getVersion());
        API.registerDrawString(
                (text, x, y, color, dropShadow) -> {
                    if (dropShadow)
                        Minecraft.getInstance().fontRenderer.drawStringWithShadow(text, x, y, color.getRGB());
                    else
                        Minecraft.getInstance().fontRenderer.drawString(text, x, y, color.getRGB());
                }
        );

        ClientRegistry.registerKeyBinding(keyBinding);

        MinecraftForge.EVENT_BUS.register(new EventListener());
        MinecraftForge.EVENT_BUS.register(this);
    }


    @SubscribeEvent
    public void onEvent(InputEvent.KeyInputEvent event) {
        if (keyBinding.isPressed()) {

            Minecraft.getInstance().displayGuiScreen(getGui());
        }
    }

}
