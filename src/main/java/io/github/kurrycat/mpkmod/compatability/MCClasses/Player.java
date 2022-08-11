package io.github.kurrycat.mpkmod.compatability.MCClasses;

import io.github.kurrycat.mpkmod.util.Vector3D;

public class Player {
    private Vector3D pos = null;
    private Vector3D lastPos = null;
    private Float yaw = null;
    private Float pitch = null;
    private Vector3D motion = null;


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

    public Float getYaw() {
        return yaw;
    }

    public Player setYaw(Float yaw) {
        this.yaw = yaw;
        return this;
    }

    public Float getPitch() {
        return pitch;
    }

    public Player setPitch(Float pitch) {
        this.pitch = pitch;
        return this;
    }

    public Vector3D getMotion() {
        return motion;
    }

    public Player setMotion(Vector3D motion) {
        this.motion = motion;
        return this;
    }
}
