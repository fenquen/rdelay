local NORMAL_ZSET = KEYS[1];
local TEMP_ZSET = KEYS[2];
local RETRY_ZSET = KEYS[3];
local taskId = ARGV[1];

redis.call("DEL", taskId);
redis.call("ZREM", NORMAL_ZSET, taskId);
redis.call("ZREM", TEMP_ZSET, taskId);
redis.call("ZREM", RETRY_ZSET, taskId);