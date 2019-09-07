local NORMAL_ZSET = KEYS[1];

local taskId = ARGV[1];
local taskJsonStr = ARGV[2];
local taskTTLMs = ARGV[3];
local taskExecuteTime = ARGV[4];

-- PSETEX fail
if (not redis.call("PSETEX", taskId, taskTTLMs, taskJsonStr)) then
    return false;
end;

redis.call("ZADD", NORMAL_ZSET, taskExecuteTime, taskId)