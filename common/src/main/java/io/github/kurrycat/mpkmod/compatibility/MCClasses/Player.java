package io.github.kurrycat.mpkmod.compatibility.MCClasses;

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

    public TimingInput timingInput = new TimingInput("");
    public KeyInput keyInput = null;
    public ButtonMSList keyMSList = null;
    private Vector3D pos = null;
    private Vector3D lastPos = null;
    private Float trueYaw = null;
    private Float truePitch = null;
    private Vector3D motion = null;
    private boolean onGround = false;
    private Float deltaYaw = 0F;
    private Float deltaPitch = 0F;
    private int airtime = 0;
    private Float last45 = 0F;
    private boolean jumpTick = false;
    private boolean landTick = false;
    private String lastTiming = "None";
    private boolean sprinting = false;

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

    public static Player getBeforeLatest() {
        if (tickHistory.size() < 2) return null;
        return tickHistory.get(tickHistory.size() - 2);
    }

    @InfoString.Getter
    public Float getLast45() {
        return last45;
    }

    public Player constructKeyInput() {
        keyInput = KeyInput.construct();
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
        return deltaYaw;
    }

    @InfoString.Getter
    public Float getDeltaPitch() {
        return deltaPitch;
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
        Player.savePlayerState(this);
        Player prev = getPrevious();
        if (prev != null) {
            if (prev.onGround) airtime = 0;
            else airtime = prev.airtime + 1;
            if (prev.onGround && !onGround) airtime = 1;

            landTick = (!prev.onGround && onGround);
            jumpTick = !onGround && prev.onGround && keyInput.jump;

            lastLanding = landTick ? new PosAndAngle(pos, trueYaw, truePitch) : prev.lastLanding;
            lastHit = prev.landTick ? new PosAndAngle(pos, trueYaw, truePitch) : prev.lastHit;
            lastJump = jumpTick ? new PosAndAngle(prev.pos, prev.trueYaw, prev.truePitch) : prev.lastJump;

            deltaYaw = trueYaw - prev.trueYaw;
            deltaPitch = truePitch - prev.truePitch;

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
        }

        //lastTiming = InputPatternStorage.match(getInputHistory());

        Player.updateDisplayInstance();
        return this;
    }

    public static void savePlayerState(Player player) {
        tickHistory.add(player);
        if (tickHistory.size() > maxSavedTicks)
            tickHistory.remove(0);
    }

    public Player getPrevious() {
        int i = tickHistory.indexOf(this);
        if (i <= 0) return null;
        return tickHistory.get(i - 1);
    }

    public static void updateDisplayInstance() {
        try {
            for (Field f : Player.class.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                f.setAccessible(true);
                Object o = f.get(getLatest());
                if (o == null) continue;
                if (o instanceof Integer && (Integer) o == 0) continue;
                if (o instanceof Float && (Float) o == 0F) continue;

                if (o instanceof Vector3D) f.set(displayInstance, ((Vector3D) o).copy());
                else if (o instanceof Copyable) f.set(displayInstance, ((Copyable<?>) o).copy());
                else f.set(displayInstance, o);
            }
        } catch (IllegalAccessException ignored) {
        }
    }

    public static Player getLatest() {
        if (tickHistory.isEmpty()) return null;
        return tickHistory.get(tickHistory.size() - 1);
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
    public static class PosAndAngle implements FormatDecimals {
        @InfoString.Field
        public final Vector3D pos;
        @InfoString.Field
        public final float yaw;
        @InfoString.Field
        public final float pitch;

        public PosAndAngle(Vector3D pos, float yaw, float pitch) {
            this.pos = pos;
            this.yaw = yaw;
            this.pitch = pitch;
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
