package com.fenquen.rdelay.server.zset_consumer;

import com.fenquen.rdelay.server.config.Config;
import com.fenquen.rdelay.server.redis.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@EnableScheduling
@Component
public class ZsetConsumer4RETRY_ZSET extends ZsetConsumerBase {

    @Autowired
    private RedisOperator redisOperator;

    @Scheduled(fixedDelay = RETRY_ZSET_PROCESS_INTERVAL_MS)
    @Override
    public void consume() {
        long now = System.currentTimeMillis();

        // TASK_EXPIRE_MS
        long begin = now - Config.TASK_EXPIRE_MS;

        Set<String> taskIds = redisOperator.getTaskIdsFromZset(Config.RETRY_ZSET, 0, now);
        for (String taskId : taskIds) {
            redisOperator.retry2Normal(taskId, System.currentTimeMillis());
        }
    }
}