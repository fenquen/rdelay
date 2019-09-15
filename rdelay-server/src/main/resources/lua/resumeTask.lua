local NORMAL_ZSET = KEYS[1];
local PAUSE_ZSET = KEYS[2];

local taskId = ARGV[1];

local score = redis.call('ZSCORE', PAUSE_ZSET, taskId);
if (score) then
    redis.call('ZREM', PAUSE_ZSET, taskId);
    redis.call('ZADD', NORMAL_ZSET, tonumber(score), taskId);
    return redis.call('INCR', 'VERSION_NUM');
end;

return 0;

