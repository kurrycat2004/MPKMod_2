package io.github.kurrycat.mpkmod.util;

public class Tuple<A, B> {
    private A a;
    private B b;

    public Tuple(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getFirst() {
        return this.a;
    }

    public void setFirst(A a) {
        this.a = a;
    }

    public B getSecond() {
        return this.b;
    }

    public void setSecond(B b) {
        this.b = b;
    }
}
