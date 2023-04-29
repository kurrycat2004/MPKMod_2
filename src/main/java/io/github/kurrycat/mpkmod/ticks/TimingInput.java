package io.github.kurrycat.mpkmod.ticks;

import io.github.kurrycat.mpkmod.util.Copyable;

import java.util.Objects;

public class TimingInput implements Copyable<TimingInput> {
    public boolean W, A, S, D, P, N, J;
    public Boolean G = null;

    public TimingInput(String inputString) {
        W = inputString.contains("W");
        A = inputString.contains("A");
        S = inputString.contains("S");
        D = inputString.contains("D");
        P = inputString.contains("P");
        N = inputString.contains("N");
        J = inputString.contains("J");
        if (inputString.contains("G"))
            G = !inputString.contains("!G");
    }

    public TimingInput(boolean W, boolean A, boolean S, boolean D, boolean P, boolean N, boolean J, Boolean G) {
        this.W = W;
        this.A = A;
        this.S = S;
        this.D = D;
        this.P = P;
        this.N = N;
        this.J = J;
        this.G = G;
    }

    public boolean isStopTick() {
        return !W && !A && !S && !D && !P && !N && !J && G != null && G;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimingInput that = (TimingInput) o;
        return W == that.W && A == that.A && S == that.S && D == that.D && P == that.P && N == that.N && J == that.J &&
                (G == null || that.G == null || this.G == that.G);
    }

    @Override
    public int hashCode() {
        return Objects.hash(W, A, S, D, P, N, J, G);
    }

    @Override
    public TimingInput copy() {
        return new TimingInput(W, A, S, D, P, N, J, G);
    }

    @Override
    public String toString() {
        return (W ? "W" : "") +
                (A ? "A" : "") +
                (S ? "S" : "") +
                (D ? "D" : "") +
                (P ? "P" : "") +
                (N ? "N" : "") +
                (J ? "J" : "") +
                (G == null ? "" : (G ? "G" : "!G"));
    }
}
