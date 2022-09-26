package io.github.kurrycat.mpkmod.compatability.MC1_8;

import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.functions.FunctionRegistry;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

@Mod(
        modid = API.MODID,
        version = API.VERSION,
        name = API.NAME,
        acceptedMinecraftVersions = "*"//,
        //updateJSON = "https://raw.githubusercontent.com/kurrycat2004/MpkMod/main/update.json",
        //guiFactory = MPKMod.GUI_FACTORY
)
public class MPKMod_1_8 {
    //public static final String GUI_FACTORY = "io.github.kurrycat.mpkmod.config.GuiFactory";

    public KeyBinding keyBinding;
    public MPKGuiScreen_1_8 gui;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        keyBinding = new KeyBinding(
                API.MODID + ".key.gui.desc",
                Keyboard.KEY_NONE,
                API.KEYBINDING_CATEGORY
        );
        gui = new MPKGuiScreen_1_8(API.getGuiScreen());

        FunctionRegistry.registerDrawString(
                (text, pos, color, dropShadow) ->
                        Minecraft.getMinecraft().fontRendererObj.drawString(text, pos.getXF(), pos.getYF(), color.getRGB(), dropShadow)
        );
        FunctionRegistry.registerGetFPS(
                () -> String.valueOf(Minecraft.getDebugFPS())
        );
        FunctionRegistry.registerGetIP(
                () -> {
                    ServerData d = Minecraft.getMinecraft().getCurrentServerData();
                    if (d == null) return "Multiplayer";
                    else return d.serverIP;
                }
        );
        FunctionRegistry.registerDrawRect(
                (pos, size, color) -> {
                    Gui.drawRect(
                            pos.getXI(),
                            pos.getYI(),
                            pos.getXI() + size.getXI(),
                            pos.getYI() + size.getYI(),
                            color.getRGB()
                    );
                }
        );
        FunctionRegistry.registerDrawBox(
                (bb, color, partialTicks) -> {
                    int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();

                    GlStateManager.pushMatrix();
                    GlStateManager.disableTexture2D();
                    GlStateManager.disableAlpha();
                    GlStateManager.enableBlend();
                    GL11.glLineWidth(2.0F);
                    GL11.glEnable(GL11.GL_LINE_SMOOTH);

                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer wr = tessellator.getWorldRenderer();

                    Entity entity = Minecraft.getMinecraft().getRenderViewEntity();

                    double entityX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
                    double entityY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
                    double entityZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;

                    wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                    wr.setTranslation(-entityX, -entityY, -entityZ);

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

                    wr.setTranslation(0, 0, 0);

                    tessellator.draw();

                    GlStateManager.enableTexture2D();
                    GlStateManager.enableAlpha();
                    GL11.glDisable(GL11.GL_LINE_SMOOTH);
                    GlStateManager.popMatrix();
                }
        );
        FunctionRegistry.registerGetScaledSize(
                () -> {
                    ScaledResolution r = new ScaledResolution(Minecraft.getMinecraft());
                    return new Vector2D(
                            r.getScaledWidth_double(),
                            r.getScaledHeight_double()
                    );
                }
        );
        FunctionRegistry.registerGetStringSize(
                text ->
                        new Vector2D(
                                Minecraft.getMinecraft().fontRendererObj.getStringWidth(text),
                                Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT
                        )
        );
        FunctionRegistry.registerPlayButtonSound(() ->
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F))
        );

        ClientRegistry.registerKeyBinding(keyBinding);

        MinecraftForge.EVENT_BUS.register(new EventListener());
        MinecraftForge.EVENT_BUS.register(this);

        System.out.println("Registering Keybindings...");
        for (KeyBinding k : Minecraft.getMinecraft().gameSettings.keyBindings) {
            new io.github.kurrycat.mpkmod.compatability.MCClasses.KeyBinding(
                    () -> GameSettings.getKeyDisplayString(k.getKeyCode()),
                    k.getKeyDescription(),
                    k::isKeyDown
            );
        }

        API.init(Minecraft.getSessionInfo().get("X-Minecraft-Version"));
    }

    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent e) {
        API.Events.onLoadComplete();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onEvent(InputEvent.KeyInputEvent event) {
        if (keyBinding.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(gui);
        }
    }

}
