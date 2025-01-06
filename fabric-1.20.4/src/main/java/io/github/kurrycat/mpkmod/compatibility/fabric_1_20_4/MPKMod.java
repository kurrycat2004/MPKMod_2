package io.github.kurrycat.mpkmod.compatibility.fabric_1_20_4;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.KeyBinding;
import io.github.kurrycat.mpknetapi.common.MPKNetworking;
import io.github.kurrycat.mpknetapi.common.network.packet.MPKPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class MPKMod implements ModInitializer {
    public static final MPKMod INSTANCE = new MPKMod();
    public static Map<String, net.minecraft.client.option.KeyBinding> keyBindingMap = new HashMap<>();
    public final EventHandler eventHandler = new EventHandler();
    public MatrixStack matrixStack;

    public static final Identifier NETWORKING_IDENTIFIER = Identifier.of(MPKNetworking.CHANNEL_NAMESPACE, MPKNetworking.CHANNEL_PATH);

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        API.LOGGER.info("Loading " + API.NAME + " " + API.VERSION);
        API.preInit(getClass());
        registerKeybindingsFromGUIs();

        HudRenderCallback.EVENT.register(eventHandler::onInGameOverlayRender);
        ClientTickEvents.START_CLIENT_TICK.register(eventHandler::onClientTickStart);
        ClientTickEvents.END_CLIENT_TICK.register(eventHandler::onClientTickEnd);
        ClientPlayConnectionEvents.JOIN.register(eventHandler::onServerConnect);
        ClientPlayConnectionEvents.DISCONNECT.register(eventHandler::onServerDisconnect);

        ClientPlayNetworking.registerGlobalReceiver(NETWORKING_IDENTIFIER, ((client, handler, buf, responseSender) -> {
            MPKPacket packet = MPKPacket.handle(API.PACKET_LISTENER_CLIENT, buf.array(), null);
            if (packet != null) {
                API.Events.onPluginMessage(packet);
            }
        }));
    }

    private void registerKeybindingsFromGUIs() {
        API.guiScreenMap.forEach((id, guiScreen) -> {
            if (guiScreen.shouldCreateKeyBind())
                registerKeyBinding(id);
        });

        API.keyBindingMap.forEach((id, consumer) -> registerKeyBinding(id));
        keyBindingMap.forEach((id, key) -> KeyBindingHelper.registerKeyBinding(key));
    }

    public void registerKeyBinding(String id) {
        net.minecraft.client.option.KeyBinding keyBinding = new net.minecraft.client.option.KeyBinding(
                API.MODID + ".key." + id + ".desc",
                -1,
                API.KEYBINDING_CATEGORY
        );

        keyBindingMap.put(id, keyBinding);
    }

    public void init() {
        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Registering compatibility functions...");
        API.registerFunctionHolder(new FunctionCompatibility());
        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Registered compatibility functions.");

        registerKeyBindings();
        API.init(SharedConstants.getGameVersion().getName());

        API.Events.onLoadComplete();
    }

    private void registerKeyBindings() {
        for (net.minecraft.client.option.KeyBinding k : MinecraftClient.getInstance().options.allKeys) {
            new io.github.kurrycat.mpkmod.compatibility.MCClasses.KeyBinding(
                    () -> k.getBoundKeyLocalizedText().getString(),
                    k.getTranslationKey(),
                    k::isPressed
            );
        }

        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Registered {} Keybindings", KeyBinding.getKeyMap().size());
    }
}
