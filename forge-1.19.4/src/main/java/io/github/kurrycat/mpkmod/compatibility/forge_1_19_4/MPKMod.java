package io.github.kurrycat.mpkmod.compatibility.forge_1_19_4;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.KeyBinding;
import net.minecraft.SharedConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.HashMap;
import java.util.Map;

@Mod(API.MODID)
public class MPKMod {
    public static Map<String, KeyMapping> keyBindingMap = new HashMap<>();

    public PoseStack poseStack = new PoseStack();
    private boolean registerMCKeyBindingStarted = false;


    public MPKMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerMCKeyBinding);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerParticleProviders);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerOverlay);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
    }

    public void registerParticleProviders(RegisterParticleProvidersEvent e) {
        //Has to be called before preInit, because LabelConfig / keybinding labels get loaded there
        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Registering Keybindings...");
        for (KeyMapping k : Minecraft.getInstance().options.keyMappings) {
            new KeyBinding(
                    () -> k.getKey().getDisplayName().getString(),
                    k.getName(),
                    k::isDown
            );
        }

        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Registered {} Keybindings", KeyBinding.getKeyMap().size());
        //Have to call it here because it's the only forge hook before registerKeyBinding gets called and API.keyBindingMap is filled in preInit
        API.preInit(getClass());
    }

    public void registerMCKeyBinding(RegisterKeyMappingsEvent e) {
        if (!registerMCKeyBindingStarted) {
            API.guiScreenMap.forEach((id, guiScreen) -> {
                if (guiScreen.shouldCreateKeyBind())
                    registerKeyBinding(id);
            });

            API.keyBindingMap.forEach((id, consumer) -> registerKeyBinding(id));
            registerMCKeyBindingStarted = true;
        }
        keyBindingMap.forEach((id, key) -> e.register(key));
    }

    public void registerOverlay(RegisterGuiOverlaysEvent e) {
        e.registerBelow(VanillaGuiOverlay.DEBUG_TEXT.id(), "mpkmod", (gui, poseStack, partialTick, width, height) -> {
            MPKMod.this.poseStack = poseStack;
            API.Events.onRenderOverlay();
        });
    }

    public void registerKeyBinding(String id) {
        KeyMapping keyBinding = new KeyMapping(
                API.MODID + ".key." + id + ".desc",
                -1,
                API.KEYBINDING_CATEGORY
        );
        keyBindingMap.put(id, keyBinding);
    }

    public void init(FMLCommonSetupEvent event) {
        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Registering compatibility functions...");
        API.registerFunctionHolder(new FunctionCompatibility());
        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Done");

        MinecraftForge.EVENT_BUS.register(new EventListener());
        MinecraftForge.EVENT_BUS.register(this);

        API.init(SharedConstants.getCurrentVersion().getName());
    }

    public void loadComplete(FMLLoadCompleteEvent e) {
        API.Events.onLoadComplete();
    }
}
