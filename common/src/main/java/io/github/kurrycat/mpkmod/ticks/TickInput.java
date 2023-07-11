package io.github.kurrycat.mpkmod.ticks;

import io.github.kurrycat.mpkmod.util.Copyable;

public class TickInput implements Copyable<TickInput> {
    public static final int W_FLAG = 1;
    public static final int A_FLAG = 1 << 1;
    public static final int S_FLAG = 1 << 2;
    public static final int D_FLAG = 1 << 3;
    public static final int P_FLAG = 1 << 4;
    public static final int N_FLAG = 1 << 5;
    public static final int J_FLAG = 1 << 6;

    protected final int keyInputs;
    protected final int L, R;
    protected final float YAW, PITCH;
    protected final int COUNT;

    public TickInput(boolean W, boolean A, boolean S, boolean D, boolean P, boolean N, boolean J, int L, int R, float YAW, float PITCH, int COUNT) {
        this.keyInputs = (W ? W_FLAG : 0) |
                (A ? A_FLAG : 0) |
                (S ? S_FLAG : 0) |
                (D ? D_FLAG : 0) |
                (P ? P_FLAG : 0) |
                (N ? N_FLAG : 0) |
                (J ? J_FLAG : 0);
        this.L = L;
        this.R = R;
        this.YAW = YAW;
        this.PITCH = PITCH;
        this.COUNT = COUNT;
    }

    public TickInput() {
        this.keyInputs = 0;
        this.L = 0;
        this.R = 0;
        this.YAW = 0;
        this.PITCH = 0;
        this.COUNT = 1;
    }

    public TickInput(int keyInputs, int L, int R, float YAW, float PITCH, int COUNT) {
        this.keyInputs = keyInputs;
        this.L = L;
        this.R = R;
        this.YAW = YAW;
        this.PITCH = PITCH;
        this.COUNT = COUNT;
    }

    private TickInput(TickInput other) {
        this.keyInputs = other.keyInputs;
        this.L = other.L;
        this.R = other.R;
        this.YAW = other.YAW;
        this.PITCH = other.PITCH;
        this.COUNT = other.COUNT;
    }

    @Override
    public TickInput copy() {
        return new TickInput(this);
    }

    @Override
    public String toString() {
        return (getW() ? "W" : "") +
                (getA() ? "A" : "") +
                (getS() ? "S" : "") +
                (getD() ? "D" : "") +
                (getP() ? "P" : "") +
                (getN() ? "N" : "") +
                (getJ() ? "J" : "") +
                ",L:" + getL() +
                ",R:" + getR() +
                ",YAW:" + getYaw() +
                ",PITCH:" + getPitch() +
                ",COUNT:" + getCount();
    }

    public boolean getW() {
        return get(W_FLAG);
    }

    public boolean getA() {
        return get(A_FLAG);
    }

    public boolean getS() {
        return get(S_FLAG);
    }

    public boolean getD() {
        return get(D_FLAG);
    }

    public boolean getP() {
        return get(P_FLAG);
    }

    public boolean getN() {
        return get(N_FLAG);
    }

    public boolean getJ() {
        return get(J_FLAG);
    }

    public int getL() {
        return L;
    }

    public int getR() {
        return R;
    }

    public float getYaw() {
        return YAW;
    }

    public float getPitch() {
        return PITCH;
    }

    public int getCount() {
        return COUNT;
    }

    public boolean get(int flag) {
        return (keyInputs & flag) != 0;
    }

    public int getKeyInputs() {
        return keyInputs;
    }
}
