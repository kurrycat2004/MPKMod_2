package io.github.kurrycat.mpkmod.compatibility.forge_1_14_4;

import io.github.kurrycat.mpkmod.compatibility.API;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.SharedConstants;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.HashMap;
import java.util.Map;

@Mod(API.MODID)
public class MPKMod {
    public static Map<String, KeyBinding> keyBindingMap = new HashMap<>();

    public MPKMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
    }

    public static void registerKeyBinding(String id) {
        KeyBinding keyBinding = new KeyBinding(
                API.MODID + ".key." + id + ".desc",
                -1,
                API.KEYBINDING_CATEGORY
        );
        keyBindingMap.put(id, keyBinding);
        ClientRegistry.registerKeyBinding(keyBinding);
    }

    public void init(FMLCommonSetupEvent event) {
        //Has to be called before preInit, because LabelConfig / keybinding labels get loaded there
        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Registering Keybindings...");
        for (KeyBinding k : Minecraft.getInstance().gameSettings.keyBindings) {
            new io.github.kurrycat.mpkmod.compatibility.MCClasses.KeyBinding(
                    k::getLocalizedName,
                    k.getKeyDescription(),
                    k::isKeyDown
            );
        }
        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Registered {} Keybindings",
                io.github.kurrycat.mpkmod.compatibility.MCClasses.KeyBinding.getKeyMap().size());

        API.preInit(getClass());

        API.guiScreenMap.forEach((id, guiScreen) -> {
            if (guiScreen.shouldCreateKeyBind())
                registerKeyBinding(id);
        });

        API.keyBindingMap.forEach((id, consumer) -> registerKeyBinding(id));

        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Registering compatibility functions...");
        API.registerFunctionHolder(new FunctionCompatibility());
        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Done");

        MinecraftForge.EVENT_BUS.register(new EventListener());
        MinecraftForge.EVENT_BUS.register(this);

        API.init(SharedConstants.getVersion().getName());
    }

    public void loadComplete(FMLLoadCompleteEvent e) {
        API.Events.onLoadComplete();
    }
}

