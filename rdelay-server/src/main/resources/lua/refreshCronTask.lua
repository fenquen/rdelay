local NORMAL_ZSET = KEYS[1];
local TEMP_ZSET = KEYS[2];
local taskId = ARGV[1];
local taskExecutionTime = ARGV[2];
local taskJsonStr = ARGV[3];

-- remove from TEMP_ZSET
redis.call('ZREM', TEMP_ZSET, taskId);

-- update
local reaminLifeMs = redis.call("PTTL", taskId);
-- ttl -1 means immortal,-2 meanings expired or keys not exisit
if (reaminLifeMs == -2) then
    return true;
end;
local psetexResult = true;
if (reaminLifeMs > 0) then
    psetexResult = redis.call("PSETEX", taskId, reaminLifeMs, taskJsonStr)
end;
if (not psetexResult) then
    return false;
end;

-- add to NORMAL_ZSET
redis.call("ZADD", NORMAL_ZSET, taskExecutionTime, taskId)
return true;