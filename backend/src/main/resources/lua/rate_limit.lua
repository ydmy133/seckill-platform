-- KEYS[1] = rate_limit:{key}      — 限流 key
-- ARGV[1] = capacity              — 令牌桶最大容量
-- ARGV[2] = rate                  — 令牌生成速率（个/秒）
-- ARGV[3] = now                   — 当前时间戳（毫秒）

local capacity = tonumber(ARGV[1])
local rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

local data = redis.call('HMGET', KEYS[1], 'tokens', 'last_time')
local tokens = tonumber(data[1])
local last_time = tonumber(data[2])

if tokens == nil then
    tokens = capacity
    last_time = now
end

local delta = math.max(0, now - last_time)
local new_tokens = math.floor(delta / 1000 * rate)
tokens = math.min(capacity, tokens + new_tokens)
last_time = now

if tokens > 0 then
    tokens = tokens - 1
    redis.call('HMSET', KEYS[1], 'tokens', tokens, 'last_time', last_time)
    redis.call('EXPIRE', KEYS[1], 300)
    return 1   -- 通过
else
    redis.call('HMSET', KEYS[1], 'tokens', tokens, 'last_time', last_time)
    return 0   -- 限流
end
