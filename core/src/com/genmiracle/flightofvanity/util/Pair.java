package com.genmiracle.flightofvanity.util;

public class Pair<A, B> {
    public A first;
    public B latter;

    public Pair(A a, B b) {
        first = a;
        latter = b;
    }

    public void set(A a, B b) {
        first = a;
        latter = b;
    }

    public void set(Pair<A, B> other) {
        first = other.first;
        latter = other.latter;
    }
}
