package com.fenquen.rdelay.server.zset_consumer;

public abstract class ZsetConsumerBase {
    public static final int NORMAL_ZSET_CONSUME_INTERVAL_MS = 100;
    public static final int RETRY_ZSET_PROCESS_INTERVAL_MS = 1000;

    public abstract void consume();
}
