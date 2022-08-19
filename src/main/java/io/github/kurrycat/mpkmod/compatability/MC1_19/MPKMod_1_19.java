package io.github.kurrycat.mpkmod.compatability.MC1_19;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.functions.FunctionRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(API.MODID)
public class MPKMod_1_19 {
    public KeyMapping keyBinding;
    public PoseStack poseStack = new PoseStack();
    private MPKGuiScreen_1_19 gui;

    public MPKMod_1_19() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerKeyBinding);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
    }

    public MPKGuiScreen_1_19 getGui() {
        if (gui == null)
            gui = new MPKGuiScreen_1_19(API.getGuiScreen());
        return gui;
    }

    public void registerKeyBinding(RegisterKeyMappingsEvent e) {
        keyBinding = new KeyMapping(
                API.MODID + ".key.gui.desc",
                -1,
                API.KEYBINDING_CATEGORY
        );
        e.register(keyBinding);
    }

    public void init(FMLCommonSetupEvent event) {
        FunctionRegistry.registerDrawString(
                (text, x, y, color, dropShadow) -> {
                    Minecraft.getInstance().font.draw(poseStack, text, x, y, color.getRGB());
                }
        );
        FunctionRegistry.registerGetIP(
                () -> {
                    ServerData d = Minecraft.getInstance().getCurrentServer();
                    if (d == null) return "Multiplayer";
                    else return d.ip;
                }
        );

        MinecraftForge.EVENT_BUS.register(new EventListener());
        MinecraftForge.EVENT_BUS.register(this);

        System.out.println("Registering Keybindings...");
        for (KeyMapping k : Minecraft.getInstance().options.keyMappings) {
            new io.github.kurrycat.mpkmod.compatability.MCClasses.KeyBinding(
                    () -> k.getKey().getDisplayName().getString(),
                    k.getName(),
                    k::isDown
            );
        }

        API.init(SharedConstants.getCurrentVersion().getName());
    }

    public void loadComplete(FMLLoadCompleteEvent e) {
        API.Events.onLoadComplete();
    }

    @SubscribeEvent
    public void onEvent(InputEvent.Key event) {
        if (keyBinding.consumeClick()) {
            Minecraft.getInstance().setScreen(getGui());
        }
    }
}
