package com.fenquen.rdelay.utils;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public final class ThreadSafeWeakMap<K, V> {

    private final int thresholdSize;

    private final ConcurrentHashMap<K, V> temp;

    private final WeakHashMap<K, V> longLife;

    public ThreadSafeWeakMap(int size) {
        thresholdSize = size;
        temp = new ConcurrentHashMap<>(size);
        longLife = new WeakHashMap<>(size);
    }

    public void put(K k, V v) {
        if (temp.size() >= thresholdSize) {
            synchronized (longLife) {
                if (temp.size() >= thresholdSize){
                    longLife.putAll(temp);
                    temp.clear();
                }
            }
        }

        temp.put(k, v);
    }

    public V get(K k) {
        V v = temp.get(k);
        if (v == null) {
            synchronized (longLife) {
                v = longLife.get(k);
            }
            if (v != null) {
                temp.put(k, v);
            }
        }
        return v;
    }

}
