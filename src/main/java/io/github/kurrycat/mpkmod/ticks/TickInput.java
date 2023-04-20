package io.github.kurrycat.mpkmod.ticks;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.util.Copyable;

import java.util.Objects;

public class TickInput implements Copyable<TickInput> {
    public int value = 0;
    public int excludingOppositesValue = 0;

    public TickInput() {

    }

    public TickInput(String input) {
        this(input.contains("W"), input.contains("A"), input.contains("S"), input.contains("D"), input.contains("P"), input.contains("N"), input.contains("J"));
    }

    public TickInput(int value) {
        this.value = value;
        calcExcludingOpposites();
    }

    public TickInput(boolean W, boolean A, boolean S, boolean D, boolean P, boolean N, boolean J) {
        value |= bit(W) | bit(A) << 1 | bit(S) << 2 | bit(D) << 3 | bit(P) << 4 | bit(N) << 5 | bit(J) << 6;
        calcExcludingOpposites();
    }

    public TickInput(Player.KeyInput k) {
        this(k.forward, k.left, k.back, k.right, k.sprint, k.sneak, k.jump);
    }

    private int bit(boolean v) {
        return v ? 1 : 0;
    }

    public String toString() {
        String res = "";
        if (getW()) res += "W";
        if (getA()) res += "A";
        if (getS()) res += "S";
        if (getD()) res += "D";
        if (getSprint()) res += "P";
        if (getSneak()) res += "N";
        if (getJump()) res += "J";
        return res;
    }

    public String hex() {
        return String.format("%02x", excludingOppositesValue);
    }

    private void calcExcludingOpposites() {
        excludingOppositesValue |=
                bit(getW() && !getS()) |
                        bit(getA() && !getD()) << 1 |
                        bit(getS() && !getW()) << 2 |
                        bit(getD() && !getA()) << 3 |
                        bit(getSprint()) << 4 |
                        bit(getSneak()) << 5 |
                        bit(getJump()) << 6;
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

    public void updateWithJumpTick(boolean isJumpTick) {
        value = (value & ~(1 << 6)) | bit(getJump() && isJumpTick) << 6;
    }

    public TickInput mirror() {
        //set second and fourth bit to 0, then set to input with OR
        int mirrorValue = (value & ~(1 << 1) & ~(1 << 3)) | bit(getD()) << 1 | bit(getA()) << 3;
        return new TickInput(mirrorValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TickInput tickInput = (TickInput) o;
        return value == tickInput.value;
    }

    public boolean isMoving() {
        return (getW() ^ getS()) || (getA() ^ getD()) || getJump();
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public TickInput copy() {
        return new TickInput(value);
    }
}