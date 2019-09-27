package com.fenquen.rdelay.model.common;

public class Pair<LEFT, RIGHT> {
    public LEFT left;

    public RIGHT right;

    public Pair() {

    }

    public Pair(LEFT left, RIGHT right) {
        this.left = left;
        this.right = right;
    }
}
