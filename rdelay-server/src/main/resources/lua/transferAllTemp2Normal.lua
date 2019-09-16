-- transfer all keys with scores in TEMP_ZSET to NORMAL_ZSET
local TEMP_ZSET = KEYS[1];
local NORMAL_ZSET = KEYS[2];

local table = redis.call('ZRANGE', TEMP_ZSET, 0, 100, 'WITHSCORES')

local subKey;
local score;
for tableSubscript, value in pairs(table) do
    if (tonumber(tableSubscript) % 2 == 0) then
        score = value;
        redis.call('ZADD', NORMAL_ZSET, score, subKey)
        redis.call('ZREM', TEMP_ZSET, subKey)
    else
        subKey = value;
    end;
end;
return true;