local taskId = KEYS[1];

local taskJsonStr = ARGV[1];

local reaminLifeMs = redis.call("PTTL", taskId);
-- ttl -1 means immortal,-2 meanings expired or keys not exisit
if (tonumber(reaminLifeMs) > 0 ) then
    redis.call("PSETEX", taskId, reaminLifeMs, taskJsonStr)
end;
return redis.call('INCR', 'VERSION_NUM');