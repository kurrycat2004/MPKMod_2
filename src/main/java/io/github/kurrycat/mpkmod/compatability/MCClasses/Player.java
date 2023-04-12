package io.github.kurrycat.mpkmod.compatability.MCClasses;

import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Vector3D;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("unused")
public class Player {
    public static ArrayList<Player> tickHistory = new ArrayList<>();
    public static int maxSavedTicks = 20;
    public static Player displayInstance = new Player();
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
    private Vector3D lastLanding = new Vector3D(0, 0, 0);

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
                if (o instanceof Integer && (Integer) o == 0) continue;
                if (o instanceof Float && (Float) o == 0F) continue;

                if (o instanceof Vector3D) f.set(displayInstance, ((Vector3D) o).copy());
                else if (o instanceof KeyInput) f.set(displayInstance, ((KeyInput) o).copy());
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

    public static Player getLatest() {
        if (tickHistory.isEmpty()) return null;
        return tickHistory.get(tickHistory.size() - 1);
    }

    public static Player getBeforeLatest() {
        if(tickHistory.size() < 2) return null;
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

    public boolean isOnGround() {
        return onGround;
    }

    public Player setOnGround(boolean onGround) {
        this.onGround = onGround;
        return this;
    }

    public String getFacing() {
        double yaw = getYaw();
        int xz = (int) Math.floor(Math.abs(yaw / 45));
        return Arrays.asList(0, 3, 4).contains(xz) ? "Z" : "X";
    }

    public Vector3D getPos() {
        return pos;
    }

    public Player setPos(Vector3D pos) {
        this.pos = pos;
        return this;
    }

    public Vector3D getLastPos() {
        return lastPos;
    }

    public Player setLastPos(Vector3D lastPos) {
        this.lastPos = lastPos;
        return this;
    }

    public Float getTrueYaw() {
        return trueYaw;
    }

    public Player setTrueYaw(Float trueYaw) {
        this.trueYaw = trueYaw;
        return this;
    }

    public Float getYaw() {
        if (trueYaw == null) return null;
        else return wrapDegrees(trueYaw);
    }

    public Float getTruePitch() {
        return truePitch;
    }

    public Player setTruePitch(Float truePitch) {
        this.truePitch = truePitch;
        return this;
    }

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

    public Float getDeltaYaw() {
        return deltaYaw;
    }

    public Float getDeltaPitch() {
        return deltaPitch;
    }

    public Vector3D getMotion() {
        return motion;
    }

    public Player setMotion(Vector3D motion) {
        this.motion = motion;
        return this;
    }

    public Vector3D getSpeed() {
        return pos.sub(lastPos);
    }

    public int getAirtime() {
        return airtime;
    }

    public int getTier() {
        return -(airtime - 12);
    }

    public Vector3D getLastLanding() {
        return lastLanding;
    }

    public Player buildAndSave() {
        Player.savePlayerState(this);
        Player prev = getPrevious();
        if (prev != null) {
            if (prev.onGround) airtime = 0;
            else airtime = prev.airtime + 1;
            if (prev.onGround && !onGround) airtime = 1;

            lastLanding = (!prev.onGround && onGround) ? pos : prev.lastLanding;

            deltaYaw = trueYaw - prev.trueYaw;
            deltaPitch = truePitch - prev.truePitch;

            jumpTick = !onGround && prev.onGround && keyInput.jump;

            if (prev.jumpTick && !prev.keyInput.movingSideways() && keyInput.movingSideways()) {
                last45 = prev.deltaYaw;
            }
        }
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
        boolean forward = false;
        boolean back = false;
        boolean left = false;
        boolean right = false;
        boolean sneak = false;
        boolean sprint = false;
        boolean jump = false;

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

        public KeyInput copy() {
            KeyInput k = new KeyInput();
            k.forward = this.forward;
            k.back = this.back;
            k.left = this.left;
            k.right = this.right;
            k.sneak = this.sneak;
            k.sprint = this.sprint;
            k.jump = this.jump;
            return k;
        }

        public boolean movingSideways() {
            return left ^ right;
        }

        public String toString() {
            return "{W:" + forward + ", A:" + left + ", S:" + back + ", D:" + right + ", N:" + sneak + ", P:" + sprint + ", J:" + jump + "}";
        }
    }
}
