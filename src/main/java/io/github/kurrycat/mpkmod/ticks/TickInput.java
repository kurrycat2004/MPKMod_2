package io.github.kurrycat.mpkmod.ticks;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;

import java.util.Objects;

public class TickInput {
    public int value = 0;

    public TickInput() {

    }

    public TickInput(int value) {
        this.value = value;
    }

    public TickInput(boolean W, boolean A, boolean S, boolean D, boolean P, boolean N, boolean J) {
        value |= bit(W) | bit(A) << 1 | bit(S) << 2 | bit(D) << 3 | bit(P) << 4 | bit(N) << 5 | bit(J) << 6;
    }

    public TickInput(Player.KeyInput k) {
        this(k.forward, k.left, k.back, k.right, k.sprint, k.sneak, k.jump);
    }

    private int bit(boolean v) {
        return v ? 1 : 0;
    }

    @Override
    public String toString() {
        return String.format("%d: [W:%b,A:%b,S:%b,D:%b,P:%b,N:%b,J:%b]",
                value,
                getW(), getA(), getS(), getD(),
                getSprint(), getSneak(), getJump());
    }

    public boolean getW() {
        return (value & 1) != 0;
    }

    public boolean getA() {
        return (value & 1 << 1) != 0;
    }

    public boolean getS() {
        return (value & 1 << 2) != 0;
    }

    public boolean getD() {
        return (value & 1 << 3) != 0;
    }

    public boolean getSprint() {
        return (value & 1 << 4) != 0;
    }

    public boolean getSneak() {
        return (value & 1 << 5) != 0;
    }

    public boolean getJump() {
        return (value & 1 << 6) != 0;
    }

    public boolean isMovingSideways() {
        return getA() ^ getD();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TickInput tickInput = (TickInput) o;
        return value == tickInput.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public TickInput copy() {
        return new TickInput(value);
    }
}