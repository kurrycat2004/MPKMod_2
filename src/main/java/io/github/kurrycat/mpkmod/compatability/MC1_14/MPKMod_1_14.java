package io.github.kurrycat.mpkmod.compatability.MC1_14;

import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.functions.FunctionRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ControlsScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(API.MODID)
public class MPKMod_1_14 {
    public KeyBinding keyBinding;
    private MPKGuiScreen_1_14 gui;

    public MPKMod_1_14() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
    }

    public MPKGuiScreen_1_14 getGui() {
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

        FunctionRegistry.registerDrawString(
                (text, x, y, color, dropShadow) -> {
                    if (dropShadow)
                        Minecraft.getInstance().fontRenderer.drawStringWithShadow(text, x, y, color.getRGB());
                    else
                        Minecraft.getInstance().fontRenderer.drawString(text, x, y, color.getRGB());
                }
        );
        FunctionRegistry.registerGetIP(
                () -> {
                    ServerData d = Minecraft.getInstance().getCurrentServerData();
                    if (d == null) return "Multiplayer";
                    else return d.serverIP;
                }
        );

        ClientRegistry.registerKeyBinding(keyBinding);

        MinecraftForge.EVENT_BUS.register(new EventListener());
        MinecraftForge.EVENT_BUS.register(this);

        System.out.println("Registering Keybindings...");
        for (KeyBinding k : Minecraft.getInstance().gameSettings.keyBindings) {
            new io.github.kurrycat.mpkmod.compatability.MCClasses.KeyBinding(
                    k::getLocalizedName,
                    k.getKeyDescription(),
                    k::isKeyDown
            );
        }

        API.init(Minecraft.getInstance().getVersion());
    }

    public void loadComplete(FMLLoadCompleteEvent e) {
        API.Events.onLoadComplete();
    }

    @SubscribeEvent
    public void onEvent(InputEvent.KeyInputEvent event) {
        if (keyBinding.isPressed()) {
            Minecraft.getInstance().displayGuiScreen(getGui());
        }
    }
}
