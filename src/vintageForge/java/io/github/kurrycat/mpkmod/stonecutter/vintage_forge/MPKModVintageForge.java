package io.github.kurrycat.mpkmod.stonecutter.vintage_forge;

import io.github.kurrycat.mpkmod.Tags;
import io.github.kurrycat.mpkmod.api.ModPlatform;
import io.github.kurrycat.mpkmod.api.render.CommandReceiver;
import io.github.kurrycat.mpkmod.api.render.Render2D;
import io.github.kurrycat.mpkmod.api.util.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.profiler.Profiler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.CoreModManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

@Mod(
        modid = Tags.MOD_ID,
        version = Tags.MOD_VERSION,
        name = Tags.MOD_NAME,
        acceptedMinecraftVersions = "*"
)
public class MPKModVintageForge {
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File configDir;
        try {
            configDir = event.getModConfigurationDirectory().getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get canonical file for mod configuration directory", e);
        }
        FileEnvImpl.INSTANCE.setGamePath(configDir.getParentFile().toPath());
        FileEnvImpl.INSTANCE.setGameConfigPath(configDir.toPath());
        Path sourcePath = event.getSourceFile().toPath();
        Path rootPath;
        try {
            rootPath = FileUtil.INSTANCE.getRootPath(sourcePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to get root path for source file", e);
        }
        FileEnvImpl.INSTANCE.setRootPaths(Collections.singletonList(rootPath));
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModPlatform.init();
        MinecraftForge.EVENT_BUS.register(this);
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
        for (int i = 0; i < 500; i++) {
            for (int j = 0; j < 500; j++) {
                Render2D.INSTANCE.pushRect(i * 50, j * 50, 50, 50, (i + j) % 2 == 0 ? 0x05000000 : 0x05FFFFFF);
            }
        }
        profiler.endStartSection("mpkmod:flush");
        CommandReceiver.INSTANCE.flushDrawCommands();
        profiler.endSection();
    }

}
