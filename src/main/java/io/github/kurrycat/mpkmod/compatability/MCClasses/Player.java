package io.github.kurrycat.mpkmod.compatability.MCClasses;

import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.client.Minecraft;

import java.util.Arrays;

@SuppressWarnings("unused")
public class Player {
    private Vector3D pos = null;
    private Vector3D lastPos = null;
    private Float trueYaw = null;
    private Float truePitch = null;
    private Vector3D motion = null;

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

    public Vector3D getMotion() {
        return motion;
    }

    public Player setMotion(Vector3D motion) {
        this.motion = motion;
        return this;
    }
}
