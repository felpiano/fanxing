/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package com.ruoyi.common.core.redis;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis工具类
 *
 * @author Mark sunlightcs@gmail.com
 */
@Component
public class RedisUtils {
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    private ValueOperations<Object, Object> valueOperations;

    /**  默认过期时长，单位：秒 */
    public final static long DEFAULT_EXPIRE = 60 * 60 * 24;
    /**  不设置过期时长 */
    public final static long NOT_EXPIRE = -1;
    private final static Gson gson = new Gson();

    public List<Object> range(String key, long start, long end){
        return redisTemplate.opsForList().range(key, start, end);
    }
    public long leftPush(String key, Object value){
        return redisTemplate.opsForList().leftPush(key, value);
    }
    public void trim(String key, long start, long end){
        redisTemplate.opsForList().trim(key, start,end);
    }


    public void set(String key, Object value, long expire){
        valueOperations.set(key, toJson(value));
        if(expire != NOT_EXPIRE){
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
    }

    public void set(String key, Object value){
        set(key, value, DEFAULT_EXPIRE);
    }

    public <T> T get(String key, Class<T> clazz, long expire) {
        String value = valueOperations.get(key).toString();
        if(expire != NOT_EXPIRE){
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
        return value == null ? null : fromJson(value, clazz);
    }

    public <T> T get(String key, Class<T> clazz) {
        return get(key, clazz, NOT_EXPIRE);
    }

    public String get(String key, long expire) {
        Object value = valueOperations.get(key);
        if (Objects.isNull(value)) {
            return null;
        }
        if(expire != NOT_EXPIRE){
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
        return value.toString();
    }

    public String get(String key) {
        return get(key, NOT_EXPIRE);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public boolean expKey(String key,long expire) {
        return redisTemplate.expire(key, expire, TimeUnit.SECONDS);
    }
    public long getExpTimeBySecond(String key){return redisTemplate.opsForValue().getOperations().getExpire(key);}


    /**
     * Object转成JSON数据
     */
    private String toJson(Object object){
        if(object instanceof Integer || object instanceof Long || object instanceof Float ||
                object instanceof Double || object instanceof Boolean || object instanceof String){
            return String.valueOf(object);
        }
        return gson.toJson(object);
    }

    /**
     * JSON数据，转成Object
     */
    private <T> T fromJson(String json, Class<T> clazz){
        return gson.fromJson(json, clazz);
    }

    public Long incr(String key, long delta) throws Exception {
        if(delta<0){
            throw new Exception("递增因子必须大于0");
        } else {
            return redisTemplate.opsForValue().increment(key, delta);
        }
    }
    //批量添加
    public void batchSet(Map<String, String> map) {
        redisTemplate.opsForValue().multiSet(map);
    }

    /**
     *@author admin
     *@Date 2024/9/15 23:15
     *@Description 从队列中获取指定长度的列表
     *
     */
    public List<Object> rangeAnddel(String listkey, long start, long end){
        return redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) {
                operations.multi();
                ListOperations<String, Object> listOps = operations.opsForList();
                List<Object> list = listOps.range(listkey, start, end);
                listOps.trim(listkey, end + 1, -1);
                List<Object> results = (List<Object>) operations.exec();
                return (List<Object>)results.get(0);
            }
        });
    }

    /**
     *@author admin
     *@Date 2024/9/15 23:16
     *@Description 插入队列
     *
     */
    public long setRange(String listkey,Object value) {
        return redisTemplate.boundListOps(listkey).rightPush(value);
    }

    /**
     * 添加积分
     * @param key
     * @param countCoin
     */
    public boolean addMonery(String key, String countCoin){
        if(!redisTemplate.hasKey(key)){
            set(key,0);
        }
        String script = "if redis.call('exists',KEYS[1]) == 1 then  redis.call('incrbyfloat',KEYS[1],ARGV[1])" +
                "  redis.call('set',KEYS[1],string.format('%0.2f',redis.call('get',KEYS[1]))) return 1" +
                " else return 0  end";
        //构建redisScript对象,构造方法参数1 执行的lua脚本   参数2 结果返回类型
        DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>(script,Long.class);
        //参数1 redisScript对象  参数2 keys,可以是多个,取决于你lua里的业务, 参数3 args 需要给lua传入的参数 也是多个
        Long result = (Long) redisTemplate.execute(defaultRedisScript, Arrays.asList(key), countCoin);
        return true;
    }


    /**
     * 扣减积分
     * @param key
     * @param countCoin
     */
    public boolean payMonery(String key, String countCoin){
        boolean flag  = false;
        if(!redisTemplate.hasKey(key)){
            set(key,0);
        }
//        String script = "if redis.call('exists',KEYS[1]) == 1 then if (redis.call('get',KEYS[1]) + ARGV[1]) >= 0 then" +
//                " redis.call('incrbyfloat',KEYS[1],ARGV[1])  redis.call('set',KEYS[1],string.format('%0.2f',redis.call('get',KEYS[1]))) return 1" +
//                "  else return 0 end else return 0  end";
        String script = "if redis.call('exists',KEYS[1]) == 1 then " +
                " redis.call('incrbyfloat',KEYS[1],ARGV[1]) return redis.call('set',KEYS[1],string.format('%0.2f',redis.call('get',KEYS[1])))" +
                " else return 0  end";
        //构建redisScript对象,构造方法参数1 执行的lua脚本   参数2 结果返回类型
        DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>(script,Long.class);
        //参数1 redisScript对象  参数2 keys,可以是多个,取决于你lua里的业务, 参数3 args 需要给lua传入的参数 也是多个
        redisTemplate.execute(defaultRedisScript, Arrays.asList(key), "-" + countCoin);
        return true;
    }



    public double processOrder(String key, long orderTimeout, int maxDrop) {
        long currentTimeMillis = System.currentTimeMillis() / 1000;

        String luaScript = "local userKey = KEYS[1]\n" +
                "local orderTimeout = tonumber(ARGV[1])\n" +
                "local maxDrop = tonumber(ARGV[2])\n" +
                "local orderTime = tonumber(ARGV[3])\n" +
                "local currentTime = tonumber(ARGV[4])\n" +
                "-- 先获取现有的过期时间\n" +
                "local ttl = redis.call('TTL', userKey)\n" +
                "if redis.call('EXISTS', userKey) == 0 then\n" +
                "    redis.call('RPUSH', userKey, cjson.encode({1, orderTime}))\n" +
                "    redis.call('EXPIRE', userKey, orderTimeout)\n" +
                "    return 1\n" +
                "else\n" +
                "    local orders = redis.call('LRANGE', userKey, 0, -1)\n" +
                "    local validDrops = {}\n" +
                "    for i, order in ipairs(orders) do\n" +
                "        local orderData = cjson.decode(order)\n" +
                "        local orderTime = orderData[2]\n" +
                "        local orderDrop = orderData[1]\n" +
                "        if currentTime - orderTime > orderTimeout then\n" +
                "            redis.call('LREM', userKey, 1, order)\n" +
                "        else\n" +
                "            table.insert(validDrops, orderDrop)\n" +
                "        end\n" +
                "    end\n" +
                "    redis.call('DEL', userKey)\n" +
                "    for _, order in ipairs(orders) do\n" +
                "        local orderData = cjson.decode(order)\n" +
                "        local orderTime = orderData[2]\n" +
                "        if currentTime - orderTime <= orderTimeout then\n" +
                "            redis.call('RPUSH', userKey, order)\n" +
                "        end\n" +
                "    end\n" +
                "    if #validDrops < maxDrop then\n" +
                "        local occupied = {}\n" +
                "        for _, drop in ipairs(validDrops) do\n" +
                "            if drop <= maxDrop then\n" +
                "                occupied[drop] = true\n" +
                "            end\n" +
                "        end\n" +
                "        for i = 1, maxDrop do\n" +
                "            if not occupied[i] then\n" +
                "                redis.call('EXPIRE', userKey, orderTimeout)\n" +
                "                redis.call('RPUSH', userKey, cjson.encode({i, orderTime}))\n" +
                "                return i\n" +
                "            end\n" +
                "        end\n" +
                "    end\n" +
                "    if ttl > 0 then\n" +
                "        redis.call('EXPIRE', userKey, ttl)\n" +
                "    end\n" +
                "    return 0\n" +
                "end\n";

        DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Long result = redisTemplate.execute(
                defaultRedisScript,
                Arrays.asList(key),
                String.valueOf(orderTimeout),
                String.valueOf(maxDrop),
                String.valueOf(currentTimeMillis),
                String.valueOf(currentTimeMillis)
        );
        long lastResult = result != null ? result : 0;
        return lastResult;
    }

    public long getNext(String key, Integer maxDrop){
        String luaScript = "local key = KEYS[1]\n" +
                "local increment = tonumber(ARGV[1])\n" +
                "local maxValue = tonumber(ARGV[2])\n" +
                "\n" +
                "local currentValue = redis.call('get', key)\n" +
                "if currentValue == false then\n" +
                "    currentValue = 0\n" +
                "end\n" +
                "\n" +
                "currentValue = currentValue + increment\n" +
                "if currentValue > maxValue then\n" +
                "    currentValue = 0\n" +
                "end\n" +
                "\n" +
                "redis.call('set', key, currentValue)\n" +
                "return currentValue";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);
        // 步长和最大值
        String increment = "1";
        String maxValue = maxDrop.toString();
        return redisTemplate.execute(redisScript, Arrays.asList(key), new Object[]{increment, maxValue});
    }

    /**
     * 批量获取key和value
     * @param keys
     * @return
     */
    public Map<Object, Object> batchQueryKeysAndValues(List<Object> keys) {
        // 批量查询values
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        // 构建key-value映射
        Map<Object, Object> keyValueMap = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            keyValueMap.put(keys.get(i), values.get(i));
        }

        return keyValueMap;
    }

    public Set<Object> keys(final String pattern)
    {
        return redisTemplate.keys(pattern);
    }
}
