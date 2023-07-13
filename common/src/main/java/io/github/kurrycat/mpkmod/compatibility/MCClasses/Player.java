package io.github.kurrycat.mpkmod.compatibility.MCClasses;

import io.github.kurrycat.mpkmod.Main;
import io.github.kurrycat.mpkmod.gui.infovars.InfoString;
import io.github.kurrycat.mpkmod.gui.screens.LandingBlockGuiScreen;
import io.github.kurrycat.mpkmod.landingblock.LandingBlock;
import io.github.kurrycat.mpkmod.ticks.ButtonMSList;
import io.github.kurrycat.mpkmod.ticks.TimingInput;
import io.github.kurrycat.mpkmod.ticks.TimingStorage;
import io.github.kurrycat.mpkmod.util.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Player {
    public static ArrayList<Player> tickHistory = new ArrayList<>();
    public static int maxSavedTicks = 100;
    @InfoString.AccessInstance
    public static Player displayInstance = new Player();

    @InfoString.Field
    public PosAndAngle lastLanding = null;
    @InfoString.Field
    public PosAndAngle lastHit = null;
    @InfoString.Field
    public PosAndAngle lastJump = null;
    @InfoString.Field
    public Blip lastBlip = null;

    public TimingInput timingInput = new TimingInput("");
    public KeyInput keyInput = null;
    public ButtonMSList keyMSList = null;
    public Vector3D pos = null;
    public Vector3D lastPos = null;
    public Float trueYaw = null;
    public Float truePitch = null;
    public Vector3D motion = null;
    public boolean onGround = false;
    public Float deltaYaw = null;
    public Float deltaPitch = null;
    public int[] deltaMouseX = null;
    public int[] deltaMouseY = null;
    public int airtime = 0;
    public Float last45 = null;
    public boolean jumpTick = false;
    public boolean landTick = false;
    public String lastTiming = "None";
    public boolean sprinting = false;


    @InfoString.Getter
    public static LandingBlock getLatestLB() {
        return LandingBlockGuiScreen.lbs.stream()
                .filter(lb -> lb.pb != null)
                .min(Comparator.comparing(o -> o.lastTimeOffsetSaved))
                .orElse(new LandingBlock(BoundingBox3D.ZERO));
    }

    @InfoString.Getter
    public static List<String> compressedInputHistory() {
        return getInputHistory().stream()
                .reduce(new ArrayList<Tuple<Integer, TimingInput>>(), (l, t) -> {
                    if (l.size() == 0) {
                        l.add(new Tuple<>(1, t));
                        return l;
                    }
                    Tuple<Integer, TimingInput> last = l.get(l.size() - 1);
                    if (last.getSecond().equals(t)) last.setFirst(last.getFirst() + 1);
                    else l.add(new Tuple<>(1, t));
                    return l;
                }, (l1, l2) -> {
                    ArrayList<Tuple<Integer, TimingInput>> list = new ArrayList<>(l1);
                    list.addAll(l2);
                    return list;
                })
                .stream().map(Object::toString).collect(Collectors.toList());

    }

    public static List<TimingInput> getInputHistory() {
        return tickHistory.stream().map(p -> p.timingInput).collect(Collectors.toList());
    }

    @InfoString.Getter
    public Float getLast45() {
        return last45;
    }

    public Player constructKeyInput() {
        keyInput = KeyInput.construct();
        return this;
    }

    public Player setKeyInput(KeyInput keyInput) {
        this.keyInput = keyInput;
        return this;
    }

    public Player setKeyMSList(ButtonMSList keyMS) {
        this.keyMSList = keyMS;
        return this;
    }

    @InfoString.Getter
    public boolean isOnGround() {
        return onGround;
    }

    public Player setOnGround(boolean onGround) {
        this.onGround = onGround;
        return this;
    }

    @InfoString.Getter
    public String getFacing() {
        double yaw = getYaw();
        int xz = (int) Math.floor(Math.abs(yaw / 45));
        return Arrays.asList(0, 3, 4).contains(xz) ? "Z" : "X";
    }

    @InfoString.Getter
    public Float getYaw() {
        if (trueYaw == null) return null;
        else return MathUtil.wrapDegrees(trueYaw);
    }

    @InfoString.Getter
    public Float getTrueYaw() {
        return trueYaw;
    }

    @InfoString.Getter
    public Float getTruePitch() {
        return truePitch;
    }

    @InfoString.Getter
    public Float getPitch() {
        if (truePitch == null) return null;
        else return MathUtil.wrapDegrees(truePitch);
    }

    public Player setRotation(Float yaw, Float pitch) {
        this.trueYaw = yaw;
        this.truePitch = pitch;
        return this;
    }

    @InfoString.Getter
    public String getLastInputs() {
        return keyInput == null ? "" :
                ((keyInput.forward ? "W" : "") +
                        (keyInput.left ? "A" : "") +
                        (keyInput.back ? "S" : "") +
                        (keyInput.right ? "D" : "") +
                        (keyInput.sprint ? "P" : "") +
                        (keyInput.sneak ? "N" : "") +
                        (keyInput.jump ? "J" : "")
                );
    }

    @InfoString.Getter
    public Float getDeltaYaw() {
        return deltaYaw == null ? 0 : deltaYaw;
    }

    @InfoString.Getter
    public Float getDeltaPitch() {
        return deltaPitch == null ? 0 : deltaPitch;
    }

    @InfoString.Getter
    public Vector3D getMotion() {
        return motion;
    }

    public Player setMotion(Vector3D motion) {
        this.motion = motion;
        return this;
    }

    @InfoString.Getter
    public Vector3D getSpeed() {
        return pos.sub(lastPos);
    }

    @InfoString.Getter
    public int getAirtime() {
        return airtime;
    }

    @InfoString.Getter
    public int getTier() {
        return -(airtime - 12);
    }

    @InfoString.Getter
    public boolean isSprinting() {
        return sprinting;
    }

    public Player setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
        return this;
    }

    @InfoString.Getter
    public String getLastTiming() {
        return lastTiming;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Player buildAndSave() {
        Player prev = getLatest();
        Player pprev = getBeforeLatest();
        Player.savePlayerState(this);
        if (prev == null) {
            Player.updateDisplayInstance();
            return this;
        }
        if (prev.onGround) airtime = 0;
        else airtime = prev.airtime + 1;
        if (prev.onGround && !onGround) airtime = 1;

        landTick = (!prev.onGround && onGround);
        jumpTick = !onGround && prev.onGround && keyInput.jump;

        lastLanding = landTick ? new PosAndAngle(prev.pos, prev.trueYaw, prev.truePitch) : prev.lastLanding;
        lastHit = prev.landTick ? new PosAndAngle(prev.pos, prev.trueYaw, prev.truePitch) : prev.lastHit;
        lastJump = jumpTick ? new PosAndAngle(prev.pos, prev.trueYaw, prev.truePitch) : prev.lastJump;

        deltaYaw = trueYaw - prev.trueYaw;
        if (deltaYaw == 0) deltaYaw = null;
        deltaPitch = truePitch - prev.truePitch;
        if (deltaPitch == 0) deltaPitch = null;

        deltaMouseX = new int[Main.mouseMovements.size()];
        deltaMouseY = new int[Main.mouseMovements.size()];
        for (int i = 0; i < Main.mouseMovements.size(); i++) {
            deltaMouseX[i] = Main.mouseMovements.get(i).getXI();
            deltaMouseY[i] = Main.mouseMovements.get(i).getYI();
        }
        Main.mouseMovements.clear();

        timingInput = new TimingInput(
                keyInput.forward,
                keyInput.left,
                keyInput.back,
                keyInput.right,
                sprinting,
                keyInput.sneak,
                jumpTick,
                onGround
        );
        if (keyMSList != null)
            timingInput.msList.addAll(keyMSList);

        if (prev.jumpTick && !prev.keyInput.isMovingSideways() && keyInput.isMovingSideways()) {
            last45 = prev.deltaYaw;
        }

        lastTiming = TimingStorage.match(getInputHistory());

        if (pprev == null) {
            Player.updateDisplayInstance();
            return this;
        }

        lastBlip = prev.lastBlip;
        if (onGround && !prev.onGround && pos.getY() == prev.pos.getY() && !prev.jumpTick) {
            if (lastBlip == null) lastBlip = new Blip(1, pos);
            else lastBlip = new Blip(lastBlip.chainedBlips + 1, pos);
        } else if (onGround) {
            lastBlip = null;
        }

        Player.updateDisplayInstance();
        return this;
    }

    public static Player getLatest() {
        if (tickHistory.isEmpty()) return null;
        return tickHistory.get(tickHistory.size() - 1);
    }

    public static Player getBeforeLatest() {
        if (tickHistory.size() < 2) return null;
        return tickHistory.get(tickHistory.size() - 2);
    }

    public static void savePlayerState(Player player) {
        tickHistory.add(player);
        if (tickHistory.size() > maxSavedTicks)
            tickHistory.remove(0);
    }

    public static void updateDisplayInstance() {
        try {
            for (Field f : Player.class.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                f.setAccessible(true);
                Object o = f.get(getLatest());
                if (o == null) continue;
                if (o instanceof Integer && (Integer) o == 0) continue;
                /*if (o instanceof Float && (Float) o == 0F) continue;*/

                if (o instanceof Vector3D) f.set(displayInstance, ((Vector3D) o).copy());
                else if (o instanceof Copyable) f.set(displayInstance, ((Copyable<?>) o).copy());
                else f.set(displayInstance, o);
            }
        } catch (IllegalAccessException ignored) {
        }
    }

    public Player getPrevious() {
        int i = tickHistory.indexOf(this);
        if (i <= 0) return null;
        return tickHistory.get(i - 1);
    }

    public BoundingBox3D getBB() {
        return new BoundingBox3D(
                getPos().add(-0.6F / 2D, 0D, -0.6F / 2D), //TODO: use dynamic player size
                getPos().add(0.6F / 2D, 1.8F, 0.6F / 2D)
        );
    }

    @InfoString.Getter
    public Vector3D getPos() {
        return pos;
    }

    public Player setPos(Vector3D pos) {
        this.pos = pos;
        return this;
    }

    public BoundingBox3D getLastBB() {
        return new BoundingBox3D(
                getLastPos().add(-0.6F / 2D, 0D, -0.6F / 2D), //TODO: use dynamic player size
                getLastPos().add(0.6F / 2D, 1.8F, 0.6F / 2D)
        );
    }

    @InfoString.Getter
    public Vector3D getLastPos() {
        return lastPos;
    }

    public Player setLastPos(Vector3D lastPos) {
        this.lastPos = lastPos;
        return this;
    }

    @InfoString.DataClass
    public static class Blip implements FormatDecimals {
        @InfoString.Field
        public final int chainedBlips;
        @InfoString.Field
        public final Vector3D pos;

        public Blip(int chainedBlips, Vector3D pos) {
            this.chainedBlips = chainedBlips;
            this.pos = pos;
        }

        public String formatDecimals(int decimals, boolean keepZeros) {
            return "[" +
                    chainedBlips + ", " +
                    pos.formatDecimals(decimals, keepZeros) +
                    "]";
        }
    }

    @InfoString.DataClass
    public static class PosAndAngle implements FormatDecimals {
        @InfoString.Field
        public final Vector3D pos;
        @InfoString.Field
        public final float trueYaw;
        @InfoString.Field
        public final float truePitch;
        @InfoString.Field
        public final float yaw;
        @InfoString.Field
        public final float pitch;

        public PosAndAngle(Vector3D pos, float trueYaw, float truePitch) {
            this.pos = pos;
            this.trueYaw = trueYaw;
            this.truePitch = truePitch;
            this.yaw = MathUtil.wrapDegrees(trueYaw);
            this.pitch = MathUtil.wrapDegrees(truePitch);
        }

        @Override
        public String toString() {
            return "[" + yaw + ", " + pitch + ", " + pos.toString() + "]";
        }

        @Override
        public String formatDecimals(int decimals, boolean keepZeros) {
            return "[" +
                    MathUtil.formatDecimals(yaw, decimals, keepZeros) + ", " +
                    MathUtil.formatDecimals(pitch, decimals, keepZeros) + ", " +
                    pos.formatDecimals(decimals, keepZeros) +
                    "]";
        }
    }

    public static class KeyInput {
        public boolean forward = false;
        public boolean left = false;
        public boolean back = false;
        public boolean right = false;
        public boolean sprint = false;
        public boolean sneak = false;
        public boolean jump = false;

        public KeyInput() {

        }

        public KeyInput(boolean forward, boolean left, boolean back, boolean right, boolean sprint, boolean sneak, boolean jump) {
            this.forward = forward;
            this.left = left;
            this.back = back;
            this.right = right;
            this.sprint = sprint;
            this.sneak = sneak;
            this.jump = jump;
        }

        public static KeyInput construct() {
            KeyInput k = new KeyInput();
            for (Field f : KeyInput.class.getDeclaredFields()) {
                KeyBinding b = KeyBinding.getByName("key." + f.getName());
                if (b == null) continue;
                try {
                    f.set(k, b.isKeyDown());
                } catch (IllegalAccessException ignored) {
                }
            }
            return k;
        }

        public String toString() {
            return "{W:" + forward + ", A:" + left + ", S:" + back + ", D:" + right + ", N:" + sneak + ", P:" + sprint + ", J:" + jump + "}";
        }

        public boolean isMovingSideways() {
            return left ^ right;
        }
    }
}
