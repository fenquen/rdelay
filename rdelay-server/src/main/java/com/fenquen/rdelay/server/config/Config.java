package com.fenquen.rdelay.server.config;

public class Config {
    public static final String NORMAL_ZSET = "NORMAL_ZSET";
    public static final String TEMP_ZSET = "TEMP_ZSET";
    public static final String RETRY_ZSET = "RETRY_ZSET";

    public static final long TASK_EXPIRE_MS = Integer.MAX_VALUE * 1000L;//
    public static final int ZSET_PROCESS_BATCH_SIZE = 20;
    public static final int RETRY_INTERVAL_SECOND = 10;

    public static final int NORMAL_ZSET_CONSUME_QUEUE_COUNT = 8;
}
