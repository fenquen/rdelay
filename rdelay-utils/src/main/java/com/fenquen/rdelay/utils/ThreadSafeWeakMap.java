/*
 * the code is inspired by org.apache.tomcat.util.collections.ConcurrentCache
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
