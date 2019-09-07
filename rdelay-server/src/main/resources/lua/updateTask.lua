local taskId = KEYS[1];

local taskJsonStr = ARGV[1];

local reaminLifeMs = redis.call("PTTL",taskId);
redis.call("PSETEX", taskId, reaminLifeMs, taskJsonStr)