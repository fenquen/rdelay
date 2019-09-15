local NORMAL_ZSET = KEYS[1];
local TEMP_ZSET = KEYS[2];
local RETRY_ZSET = KEYS[3];
local PAUSE_ZSET = KEYS[4];

-- alreay paused
if (redis.call('ZSCORE', PAUSE_ZSET, taskId)) then
    return 0;
end;


-- the taskid in TEMP_ZSET means it is waiting for execution resp,you can not modify it
local score = redis.call("ZSCORE", TEMP_ZSET, taskId);
if (score) then
    return 0;
end;

local taskId = ARGV[1];

-- the expected state is NORMAL
local arr = { NORMAL_ZSET, RETRY_ZSET };
for a = 1, 2 do
    local score = redis.call('ZSCORE', arr[a], taskId);
    if (score) then
        redis.call('ZREM', arr[a], taskId);
        redis.call('ZADD', PAUSE_ZSET, tonumber(score), taskId);
        return redis.call('INCR', 'VERSION_NUM');
    end;
end;
-- the task is already ABORTED_MANUALLY ,COMPLETED_NORMALLY or ABORTED_WITH_TOO_MANY_RETRIES
return 0;