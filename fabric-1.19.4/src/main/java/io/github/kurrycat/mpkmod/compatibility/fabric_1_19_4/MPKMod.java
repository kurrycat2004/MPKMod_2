package io.github.kurrycat.mpkmod.compatibility.fabric_1_19_4;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MPKMod implements ModInitializer {
	public static Map<String, net.minecraft.client.option.KeyBinding> keyBindingMap = new HashMap<>();
	public static final Logger LOGGER = LoggerFactory.getLogger("MPKMod");
	public static final MPKMod INSTANCE = new MPKMod();
	public final EventHandler eventHandler = new EventHandler();
	public MatrixStack matrixStack;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		LOGGER.info("Loading " + API.NAME + " " + API.VERSION);
		API.preInit(getClass());
		registerKeybindingsFromGUIs();

		HudRenderCallback.EVENT.register(eventHandler::onInGameOverlayRender);
		ClientTickEvents.START_CLIENT_TICK.register(eventHandler::onClientTickStart);
		ClientTickEvents.END_CLIENT_TICK.register(eventHandler::onClientTickEnd);
	}

	public void init() {
		API.LOGGER.info(API.COMPATIBILITY_MARKER, "Registering compatibility functions...");
		API.registerFunctionHolder(new FunctionCompatibility());
		API.LOGGER.info(API.COMPATIBILITY_MARKER, "Registered compatibility functions.");

		registerKeyBindings();
		API.init(SharedConstants.getGameVersion().getName());

		API.Events.onLoadComplete();
	}

	public void registerKeyBinding(String id) {
		net.minecraft.client.option.KeyBinding keyBinding = new net.minecraft.client.option.KeyBinding(
				API.MODID + ".key." + id + ".desc",
				-1,
				API.KEYBINDING_CATEGORY
		);

		keyBindingMap.put(id, keyBinding);
	}

	private void registerKeyBindings() {
		for(net.minecraft.client.option.KeyBinding k : MinecraftClient.getInstance().options.allKeys) {
			new io.github.kurrycat.mpkmod.compatibility.MCClasses.KeyBinding(
					() -> k.getBoundKeyLocalizedText().getString(),
					k.getTranslationKey(),
					k::isPressed
			);
		}

		API.LOGGER.info(API.COMPATIBILITY_MARKER, "Registered {} Keybindings", KeyBinding.getKeyMap().size());
	}

	private void registerKeybindingsFromGUIs() {
		API.guiScreenMap.forEach((id, guiScreen) -> {
			if (guiScreen.shouldCreateKeyBind())
				registerKeyBinding(id);
		});

		API.keyBindingMap.forEach((id, consumer) -> registerKeyBinding(id));
		keyBindingMap.forEach((id, key) -> KeyBindingHelper.registerKeyBinding(key));
	}
}
