package io.github.kurrycat.mpkmod.compatibility.MCClasses;

import io.github.kurrycat.mpkmod.gui.screens.LandingBlockGuiScreen;
import io.github.kurrycat.mpkmod.landingblock.LandingBlock;
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

@SuppressWarnings("unused")
public class Player {
    public static ArrayList<Player> tickHistory = new ArrayList<>();
    public static int maxSavedTicks = 100;
    @InfoString.AccessInstance
    public static Player displayInstance = new Player();
    public TimingInput timingInput = new TimingInput("");
    public KeyInput keyInput = null;
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
    @InfoString.Field
    public Vector3D lastLanding = new Vector3D(0, 0, 0);
    @InfoString.Field
    public Vector3D lastHit = new Vector3D(0, 0, 0);
    @InfoString.Field
    public Vector3D lastJump = new Vector3D(0, 0, 0);
    private String lastTiming = "None";
    private boolean sprinting = false;

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
                if (o instanceof Float && (Float) o == 0F) continue;

                if (o instanceof Vector3D) f.set(displayInstance, ((Vector3D) o).copy());
                else if (o instanceof Copyable) f.set(displayInstance, ((Copyable<?>) o).copy());
                else f.set(displayInstance, o);
            }
        } catch (IllegalAccessException ignored) {
        }
    }

    public static int ticksSince(CheckPlayerFunction f) {
        if (tickHistory.isEmpty()) return -1;
        for (int i = 0; i < tickHistory.size(); i++) {
            if (f.apply(tickHistory.get(tickHistory.size() - i - 1)))
                return i;
        }
        return -1;
    }

    public static Player findInState(CheckPlayerFunction f) {
        for (int i = tickHistory.size() - 1; i >= 0; i--) {
            if (f.apply(tickHistory.get(i))) return tickHistory.get(i);
        }
        return null;
    }

    @InfoString.Getter
    public static LandingBlock getLatestLB() {
        return LandingBlockGuiScreen.lbs.stream()
                .filter(lb -> lb.pb != null)
                .min(Comparator.comparing(o -> o.lastTimeOffsetSaved))
                .orElse(new LandingBlock(BoundingBox3D.ZERO));
    }

    public static Player getLatest() {
        if (tickHistory.isEmpty()) return null;
        return tickHistory.get(tickHistory.size() - 1);
    }

    public static List<TimingInput> getInputHistory() {
        return tickHistory.stream().map(p -> p.timingInput).collect(Collectors.toList());
    }

    public static List<String> getInputList() {
        ArrayList<Tuple<TimingInput, Integer>> inputList = new ArrayList<>();
        for (TimingInput p : getInputHistory()) {
            if (inputList.isEmpty())
                inputList.add(new Tuple<>(p, 1));

            Tuple<TimingInput, Integer> last = inputList.get(inputList.size() - 1);
            if (last.getFirst().equals(p))
                last.setSecond(last.getSecond() + 1);
            else inputList.add(new Tuple<>(p, 1));
        }

        return inputList.stream().map(t -> t.getSecond() + "*" + t.getFirst().toString()).collect(Collectors.toList());
    }

    public static Player getBeforeLatest() {
        if (tickHistory.size() < 2) return null;
        return tickHistory.get(tickHistory.size() - 2);
    }

    public static float wrapDegrees(float value) {
        value = value % 360.0F;

        if (value >= 180.0F) {
            value -= 360.0F;
        }

        if (value < -180.0F) {
            value += 360.0F;
        }

        return value;
    }

    @InfoString.Getter
    public Float getLast45() {
        return last45;
    }

    public Player getPrevious() {
        if (!tickHistory.contains(this)) return null;
        int i = tickHistory.indexOf(this);
        if (i == 0) return null;
        return tickHistory.get(i - 1);
    }

    public Player constructKeyInput() {
        keyInput = KeyInput.construct();
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
    public Vector3D getPos() {
        return pos;
    }

    public Player setPos(Vector3D pos) {
        this.pos = pos;
        return this;
    }

    @InfoString.Getter
    public Vector3D getLastPos() {
        return lastPos;
    }

    public Player setLastPos(Vector3D lastPos) {
        this.lastPos = lastPos;
        return this;
    }

    @InfoString.Getter
    public Float getTrueYaw() {
        return trueYaw;
    }

    public Player setTrueYaw(Float trueYaw) {
        this.trueYaw = trueYaw;
        return this;
    }

    @InfoString.Getter
    public Float getYaw() {
        if (trueYaw == null) return null;
        else return wrapDegrees(trueYaw);
    }

    @InfoString.Getter
    public Float getTruePitch() {
        return truePitch;
    }

    public Player setTruePitch(Float truePitch) {
        this.truePitch = truePitch;
        return this;
    }

    @InfoString.Getter
    public Float getPitch() {
        if (truePitch == null) return null;
        else return wrapDegrees(truePitch);
    }

    public Player setRotation(Float yaw, Float pitch) {
        this.trueYaw = yaw;
        this.truePitch = pitch;
        return this;
    }

    public Float getPrevYaw() {
        Player prev = getPrevious();
        if (prev == null || prev.trueYaw == null) return null;
        else return wrapDegrees(prev.trueYaw);
    }

    public Float getPrevPitch() {
        Player prev = getPrevious();
        if (prev == null || prev.truePitch == null) return null;
        else return wrapDegrees(prev.truePitch);
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

    public Player buildAndSave() {
        Player.savePlayerState(this);
        Player prev = getPrevious();
        if (prev != null) {
            if (prev.onGround) airtime = 0;
            else airtime = prev.airtime + 1;
            if (prev.onGround && !onGround) airtime = 1;

            landTick = (!prev.onGround && onGround);
            jumpTick = !onGround && prev.onGround && keyInput.jump;

            lastLanding = landTick ? pos : prev.lastLanding;
            lastHit = prev.landTick ? pos : prev.lastHit;
            lastJump = jumpTick ? pos : prev.lastJump;

            deltaYaw = trueYaw - prev.trueYaw;
            deltaPitch = truePitch - prev.truePitch;

            //TODO: use mc inputs instead of keys (e.g. player not being able to sprint because of hunger but it still saving sprint when button is pressed)
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

            if (prev.jumpTick && !prev.keyInput.isMovingSideways() && keyInput.isMovingSideways()) {
                last45 = prev.deltaYaw;
            }

            lastTiming = TimingStorage.match(getInputHistory());
        }

        //lastTiming = InputPatternStorage.match(getInputHistory());

        Player.updateDisplayInstance();
        return this;
    }

    public BoundingBox3D getBB() {
        return new BoundingBox3D(
                getPos().add(-0.6F / 2D, 0D, -0.6F / 2D), //TODO: use dynamic player size
                getPos().add(0.6F / 2D, 1.8F, 0.6F / 2D)
        );
    }

    public BoundingBox3D getLastBB() {
        return new BoundingBox3D(
                getLastPos().add(-0.6F / 2D, 0D, -0.6F / 2D), //TODO: use dynamic player size
                getLastPos().add(0.6F / 2D, 1.8F, 0.6F / 2D)
        );
    }

    @FunctionalInterface
    public interface CheckPlayerFunction {
        boolean apply(Player p);
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
