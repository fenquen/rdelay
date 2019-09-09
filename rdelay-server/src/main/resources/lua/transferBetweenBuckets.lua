local srcZsetName = KEYS[1];
local destZsetName = KEYS[2];

local taskId = ARGV[1];
local score = ARGV[2];

-- taskId is not in srcZset, return directly
if (redis.call('ZRANK', srcZsetName, taskId) == nil) then
    return false;
end;


redis.call('ZADD', destZsetName, score, taskId);
redis.call('ZREM', srcZsetName, taskId)

return true;