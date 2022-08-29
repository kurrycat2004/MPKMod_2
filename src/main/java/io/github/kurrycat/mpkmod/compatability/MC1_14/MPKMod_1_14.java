package io.github.kurrycat.mpkmod.compatability.MC1_14;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.functions.FunctionRegistry;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.opengl.GL11;

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
                (text, pos, color, dropShadow) -> {
                    if (dropShadow)
                        Minecraft.getInstance().fontRenderer.drawStringWithShadow(text, pos.getXF(), pos.getYF(), color.getRGB());
                    else
                        Minecraft.getInstance().fontRenderer.drawString(text, pos.getXF(), pos.getYF(), color.getRGB());
                }
        );
        FunctionRegistry.registerGetIP(
                () -> {
                    ServerData d = Minecraft.getInstance().getCurrentServerData();
                    if (d == null) return "Multiplayer";
                    else return d.serverIP;
                }
        );
        FunctionRegistry.registerDrawRect(
                (pos, size, color) -> {
                    Screen.fill(
                            pos.getXI(),
                            pos.getYI(),
                            pos.getXI() + size.getXI(),
                            pos.getYI() + size.getYI(),
                            color.getRGB()
                    );
                }
        );
        FunctionRegistry.registerDrawBox(
                (bb, color, player, partialTicks) -> {
                    int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();

                    GlStateManager.pushMatrix();
                    GlStateManager.clear(256, Minecraft.IS_RUNNING_ON_MAC);

                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder wr = tessellator.getBuffer();
                    Vector3D trans = player.getLastPos().add(player.getPos().sub(player.getLastPos()).mult(partialTicks));
                    wr.setTranslation(-trans.getX(), -trans.getY(), -trans.getZ());

                    GlStateManager.enableBlend();
                    GlStateManager.blendFuncSeparate(770, 771, 1, 0);
                    GL11.glEnable(GL11.GL_LINE_SMOOTH);

                    wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

                    wr.pos(bb.minX(), bb.maxY(), bb.minZ()).color(r, g, b, a).endVertex();
                    wr.pos(bb.maxX(), bb.maxY(), bb.minZ()).color(r, g, b, a).endVertex();
                    wr.pos(bb.maxX(), bb.minY(), bb.minZ()).color(r, g, b, a).endVertex();
                    wr.pos(bb.minX(), bb.minY(), bb.minZ()).color(r, g, b, a).endVertex();

                    wr.pos(bb.minX(), bb.minY(), bb.maxZ()).color(r, g, b, a).endVertex();
                    wr.pos(bb.maxX(), bb.minY(), bb.maxZ()).color(r, g, b, a).endVertex();
                    wr.pos(bb.maxX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).endVertex();
                    wr.pos(bb.minX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).endVertex();

                    wr.pos(bb.minX(), bb.minY(), bb.minZ()).color(r, g, b, a).endVertex();
                    wr.pos(bb.maxX(), bb.minY(), bb.minZ()).color(r, g, b, a).endVertex();
                    wr.pos(bb.maxX(), bb.minY(), bb.maxZ()).color(r, g, b, a).endVertex();
                    wr.pos(bb.minX(), bb.minY(), bb.maxZ()).color(r, g, b, a).endVertex();

                    wr.pos(bb.minX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).endVertex();
                    wr.pos(bb.maxX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).endVertex();
                    wr.pos(bb.maxX(), bb.maxY(), bb.minZ()).color(r, g, b, a).endVertex();
                    wr.pos(bb.minX(), bb.maxY(), bb.minZ()).color(r, g, b, a).endVertex();

                    wr.pos(bb.minX(), bb.minY(), bb.maxZ()).color(r, g, b, a).endVertex();
                    wr.pos(bb.minX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).endVertex();
                    wr.pos(bb.minX(), bb.maxY(), bb.minZ()).color(r, g, b, a).endVertex();
                    wr.pos(bb.minX(), bb.minY(), bb.minZ()).color(r, g, b, a).endVertex();

                    wr.pos(bb.maxX(), bb.minY(), bb.minZ()).color(r, g, b, a).endVertex();
                    wr.pos(bb.maxX(), bb.maxY(), bb.minZ()).color(r, g, b, a).endVertex();
                    wr.pos(bb.maxX(), bb.maxY(), bb.maxZ()).color(r, g, b, a).endVertex();
                    wr.pos(bb.maxX(), bb.minY(), bb.maxZ()).color(r, g, b, a).endVertex();

                    tessellator.draw();
                    GlStateManager.disableBlend();
                    GL11.glDisable(GL11.GL_LINE_SMOOTH);
                    wr.setTranslation(0, 0, 0);

                    GlStateManager.popMatrix();
                }
        );
        FunctionRegistry.registerGetScaledSize(
                () -> new Vector2D(
                        Minecraft.getInstance().mainWindow.getScaledWidth(),
                        Minecraft.getInstance().mainWindow.getScaledHeight()
                )
        );
        FunctionRegistry.registerGetStringSize(
                text -> new Vector2D(
                        Minecraft.getInstance().fontRenderer.getStringWidth(text),
                        Minecraft.getInstance().fontRenderer.FONT_HEIGHT
                )
        );
        FunctionRegistry.registerPlayButtonSound(() ->
                Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F))
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

        API.init(SharedConstants.getVersion().getName());
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
