local NORMAL_ZSET = KEYS[1];
local TEMP_ZSET = KEYS[2];
local RETRY_ZSET = KEYS[3];
local PAUSE_ZSET = KEYS[4];

local taskId = ARGV[1];


-- the taskid in TEMP_ZSET means it is waiting for execution resp,you can not modify it
local score = redis.call("ZSCORE", TEMP_ZSET, taskId);
if (score) then
    return nil;
end;


local jsonStr = redis.call("GET",taskId);
redis.call("DEL", taskId);

-- the expected states are NORMAL and PAUSED
local arr = { NORMAL_ZSET, RETRY_ZSET, PAUSE_ZSET };
for a = 1, 3 do
    local score = redis.call('ZSCORE', arr[a], taskId);
    if (score) then
        redis.call('ZREM', arr[a], taskId);
        return jsonStr .. "&" .. redis.call('INCR', 'VERSION_NUM');
    end;
end;

-- means the task not in the zsets above,its state has already become ABORTED_MANUALLY, COMPLETED_NORMALLY or ABORTED_WITH_TOO_MANY_RETRIES
return nil;