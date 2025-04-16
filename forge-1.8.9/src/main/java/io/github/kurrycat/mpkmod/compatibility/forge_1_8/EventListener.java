package io.github.kurrycat.mpkmod.compatibility.forge_1_8;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.InputConstants;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.ticks.ButtonMS;
import io.github.kurrycat.mpkmod.ticks.ButtonMSList;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SuppressWarnings("unused")
public class EventListener {
    private static final ButtonMSList timeQueue = new ButtonMSList();

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onEvent(InputEvent.KeyInputEvent event) {
        int keyCode = Keyboard.getEventKey();
        String key = Keyboard.getKeyName(keyCode);
        boolean pressed = Keyboard.getEventKeyState();

        GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;

        int[] keys = {
                gameSettings.keyBindForward.getKeyCode(),
                gameSettings.keyBindLeft.getKeyCode(),
                gameSettings.keyBindBack.getKeyCode(),
                gameSettings.keyBindRight.getKeyCode(),
                gameSettings.keyBindSprint.getKeyCode(),
                gameSettings.keyBindSneak.getKeyCode(),
                gameSettings.keyBindJump.getKeyCode()
        };

        for (int i = 0; i < keys.length; i++)
            if (keyCode == keys[i])
                timeQueue.add(ButtonMS.of(ButtonMS.Button.values()[i], Keyboard.getEventNanoseconds(), pressed));


        API.Events.onKeyInput(InputConstants.convert(keyCode), key, pressed);

        MPKMod.keyBindingMap.forEach((id, keyBinding) -> {
            if (keyBinding.isPressed()) {
                API.Events.onKeybind(id);
            }
        });
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onMouseEvent(MouseEvent event) {
        API.Events.onMouseInput(
                Mouse.Button.fromInt(event.button),
                event.button == -1 ? Mouse.State.NONE : (event.buttonstate ? Mouse.State.DOWN : Mouse.State.UP),
                event.x, event.y, event.dx, event.dy,
                event.dwheel, event.nanoseconds
        );
    }


    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.isGamePaused() || mc.theWorld == null) return;
        EntityPlayerSP mcPlayer = mc.thePlayer;

        if (e.type != TickEvent.Type.CLIENT) return;
        if (e.side != Side.CLIENT) return;

        if (mcPlayer != null && e.phase == TickEvent.Phase.START) {
            AxisAlignedBB playerBB = mcPlayer.getEntityBoundingBox();
            new Player()
                    .setPos(new Vector3D(mcPlayer.posX, mcPlayer.posY, mcPlayer.posZ))
                    .setLastPos(new Vector3D(mcPlayer.lastTickPosX, mcPlayer.lastTickPosY, mcPlayer.lastTickPosZ))
                    .setMotion(new Vector3D(mcPlayer.motionX, mcPlayer.motionY, mcPlayer.motionZ))
                    .setRotation(mcPlayer.rotationYaw, mcPlayer.rotationPitch)
                    .setOnGround(mcPlayer.onGround)
                    .setSprinting(mcPlayer.isSprinting())
                    .setBoundingBox(new BoundingBox3D(
                        new Vector3D(playerBB.minX, playerBB.minY, playerBB.minZ),
                        new Vector3D(playerBB.maxX, playerBB.maxY, playerBB.maxZ)
                    ))
                    .constructKeyInput()
                    .setKeyMSList(timeQueue.copy())
                    .buildAndSave();
            timeQueue.clear();
        }
        if (e.phase == TickEvent.Phase.START) {
            API.Events.onTickStart();
        } else if (e.phase == TickEvent.Phase.END)
            API.Events.onTickEnd();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent e) {
        if (e.type == RenderGameOverlayEvent.ElementType.TEXT && e instanceof RenderGameOverlayEvent.Text)
            API.Events.onRenderOverlay();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onRenderWorld(RenderWorldLastEvent e) {
        API.Events.onRenderWorldOverlay(e.partialTicks);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onServerConnect(FMLNetworkEvent.ClientConnectedToServerEvent e) {
        API.Events.onServerConnect(e.isLocal);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onServerDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
        API.Events.onServerDisconnect();
    }
}
