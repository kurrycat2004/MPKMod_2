package io.github.kurrycat.mpkmod.stonecutter.vintage_forge;

import io.github.kurrycat.mpkmod.Tags;
import io.github.kurrycat.mpkmod.api.ModPlatform;
import io.github.kurrycat.mpkmod.api.minecraft.IFileEnv;
import io.github.kurrycat.mpkmod.api.render.CommandReceiver;
import io.github.kurrycat.mpkmod.api.render.RenderBackend;
import io.github.kurrycat.mpkmod.api.render.text.GlyphProvider;
import io.github.kurrycat.mpkmod.api.render.text.TextRenderer;
import io.github.kurrycat.mpkmod.api.service.ServiceManager;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.util.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.profiler.Profiler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@Mod(
        modid = Tags.MOD_ID,
        version = Tags.MOD_VERSION,
        name = Tags.MOD_NAME,
        acceptedMinecraftVersions = "*"
)
public class MPKModVintageForge {
    public static FileEnvImpl FILE_ENV;

    public record FileEnvImpl(
            Path gamePath,
            Path gameConfigPath,
            List<Path> rootPaths
    ) implements IFileEnv {}

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File configDir;
        try {
            configDir = event.getModConfigurationDirectory().getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get canonical file for mod configuration directory", e);
        }

        Path sourcePath = event.getSourceFile().toPath();
        Path rootPath;
        try {
            rootPath = FileUtil.instance().getRootPath(sourcePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to get root path for source file", e);
        }
        FILE_ENV = new FileEnvImpl(
                configDir.getParentFile().toPath(),
                configDir.toPath(),
                Collections.singletonList(rootPath)
        );
    }

    private static final KeyBinding TEST_KEY = new KeyBinding(
            "test",
            Keyboard.KEY_K,
            Tags.MOD_NAME
    );

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModPlatform.init();
        MinecraftForge.EVENT_BUS.register(this);

        if (event.getSide().isClient()) {
            ClientRegistry.registerKeyBinding(TEST_KEY);
        }
    }

    int renderBackendId = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getMinecraft().currentScreen != null) return;

        if (TEST_KEY.isPressed()) {
            List<ServiceProvider> backends = ServiceManager.instance().getProviders(RenderBackend.class);
            ServiceProvider backend = backends.get(renderBackendId);
            ModPlatform.LOGGER.info("Switching to render backend: {}", backend.name());
            ServiceManager.instance().switchToService(backend);
            renderBackendId = (renderBackendId + 1) % backends.size();
        }
    }

    private Profiler getProfiler() {
        //? if <=1.8.9 {
        /*return Minecraft.getMinecraft().mcProfiler;
         *///?} else
        return Minecraft.getMinecraft().profiler;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent event) {
        //? if <=1.8.9 {
        /*RenderGameOverlayEvent.ElementType type = event.type;
         *///?} else
        RenderGameOverlayEvent.ElementType type = event.getType();

        if (type != RenderGameOverlayEvent.ElementType.TEXT) {
            return;
        }
        if (!(event instanceof RenderGameOverlayEvent.Text)) {
            return;
        }

        Profiler profiler = getProfiler();

        profiler.startSection("mpkmod:push");
        // checkerboard
        /*for (int i = 10; i < 20; i++) {
            for (int j = 10; j < 20; j++) {
                Render2D.INSTANCE.pushRect(i * 10, j * 10, 10, 10, (i + j) % 2 == 0 ? 0xFF000000 : 0x70FFFFFF);
            }
        }*/
        profiler.endStartSection("mpkmod:text");
        runAllTextRendererTests();
        profiler.endStartSection("mpkmod:fps");
        // draw FPS
        TextRenderer.instance().drawFormattedString(
                10, 10,
                0xFFFFFFFF, false,
                "FPS: " + Minecraft.getDebugFPS()
        );
        profiler.endStartSection("mpkmod:flush");
        CommandReceiver.instance().flushDrawCommands();
        profiler.endSection();
    }

    public static void runAllTextRendererTests() {
        TextRenderer tr = TextRenderer.instance();
        GlyphProvider.GlyphData buf = new GlyphProvider.GlyphData();
        float x = 10, y = 30;
        int white = 0xFFFFFFFF;

        tr.drawFormattedString(buf, x, y, white, false,
                "The quick brown fox jumps over the lazy dog");

        y += 15;
        tr.drawFormattedString(buf, x, y, white, false,
                "§1Blue §2Green §3Aqua §4Red §5Purple §6Gold §rNormal");

        y += 15;
        tr.drawFormattedString(buf, x, y, white, false,
                "§lBold §oItalic §nUnderlined §mStruck §rNormal");

        y += 13;
        tr.drawFormattedString(buf, x, y, white, false,
                "§kObfuscated text§r back to normal");

        y += 13;
        tr.drawFormattedString(buf, x, y, white, true,
                "A: \u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229");
        y += 13;
        tr.drawFormattedString(buf, x, y, white, true,
                "ＡＢＣ１２３ ａｂｃ 日本語 漢字 테스트");
        y += 13;
        tr.drawFormattedString(buf, x, y, white, true,
                "§lＡＢＣ１２３ ａｂｃ 日本語 漢字 테스트");
        y += 13;
        tr.drawFormattedString(buf, x, y, white, true,
                "§oＡＢＣ１２３ ａｂｃ 日本語 漢字 테스트");
        y += 13;
        tr.drawFormattedString(buf, x, y, white, true,
                "§l§oＡＢＣ１２３ ａｂｃ 日本語 漢字 테스트");
        y += 13;
        tr.drawFormattedString(buf, x, y, white, true,
                "Shadow enabled");
        y += 13;
        tr.drawFormattedString(buf, x, y, white, false,
                "No shadow");

        y += 13;
        tr.drawFormattedString(buf, x, y, white, true,
                "§cRed §lBold §nUnderlined §oItalic §kCrazy§r§6 Done!");

        y += 13;
        tr.drawFormattedString(buf, x, y, white, false,
                "Unicode: Ω Σ π α β ╔═══╗ ╚═══╝ ░▒▓");

        y += 13;
        tr.drawFormattedString(buf, x, y, white, false,
                "Emoji: 😀 😁 😂 🤣 😃 😄 😅");
    }
}
