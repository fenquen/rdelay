local NORMAL_ZSET = KEYS[1];

local taskId = ARGV[1];
local taskJsonStr = ARGV[2];
local taskTTLMs = ARGV[3];
local taskExecuteTime = ARGV[4];
-- PSETEX fail
redis.call('PSETEX', taskId, taskTTLMs, taskJsonStr);

if (nil == redis.call('GET', taskId)) then
    redis.log(redis.LOG_NOTICE, 'PSETEX failed when creation');
    return nil;
end;


redis.call('ZADD', NORMAL_ZSET, taskExecuteTime, taskId)
return redis.call('INCR', 'VERSION_NUM');