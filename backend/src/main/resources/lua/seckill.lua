-- KEYS[1] = seckill:stock:{seckillProductId}  — 库存 key
-- KEYS[2] = seckill:users:{seckillProductId}  — 已购买用户集合 key
-- ARGV[1] = userId                             — 当前用户 ID

local stock = redis.call('GET', KEYS[1])       -- 1. 读取当前库存
if not stock or tonumber(stock) <= 0 then       -- 2. 库存不存在或已归零
    return -1                                   -- 返回 -1 表示 "已售罄"
end

local exists = redis.call('SISMEMBER', KEYS[2], ARGV[1])  -- 3. 检查用户是否已买过
if exists == 1 then
    return -2                                   -- 返回 -2 表示 "重复购买"
end

redis.call('DECR', KEYS[1])                    -- 4. 库存减一（原子操作）
redis.call('SADD', KEYS[2], ARGV[1])           -- 5. 把用户 ID 加入已购集合
return 1                                        -- 返回 1 表示 "抢购成功"
