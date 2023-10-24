package com.zj.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengjie
 * @version 1.0
 * @date 2023/10/24 13:17
 */
@Component
public class RedisUtil{

    public static final String TYPE_NX = "nx";

    private Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    @Autowired
    private RedisTemplate redisTemplate;

    public Boolean set(final byte[] key, final byte[] value, final long expireTime, final String type) {
        return (Boolean) redisTemplate.execute(new RedisCallback() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                if (value == null) {
                    return Boolean.FALSE;
                }
                if ("".equals(type)) {
                    connection.set(key, value);
                }
                if (TYPE_NX.equals(type)) {
                    return connection.setNX(key, value);
                }
                if (expireTime > 0) {
                    connection.expire(key, expireTime);
                }
                return Boolean.TRUE;
            }
        });
    }

    public Boolean set(String key, Object value, int expireTime) {
        redisTemplate.opsForValue().set(key, value);
        if (expireTime > 0) {
            this.setExpire(key, expireTime);
        }
        return Boolean.TRUE;
    }

    /**
     * 不存在就设置值，返回true
     * 存在返回false，不设置值
     * @param key
     * @param value
     * @param expire
     * @param timeUnit
     * @return
     */
    public Boolean setIfAbsent(String key, String value, long expire, TimeUnit timeUnit) {
        boolean flag;
        BoundValueOperations<String, String> boundValueOperations = redisTemplate.boundValueOps(key);
        flag = Boolean.TRUE.equals(boundValueOperations.setIfAbsent(value));
        if (flag && expire > 0) {
            boundValueOperations.expire(expire, timeUnit);
        }
        return flag;
    }

    public Boolean set(String key, Object value) {
        return this.set(key, value, 0);
    }

    public Boolean setNx(String key, Object value) {
        return this.set(redisTemplate.getKeySerializer().serialize(key), redisTemplate.getValueSerializer().serialize(value), 0, "nx");
    }

    public Boolean setNx(String key, Object value, int expireTime) {
        return this.set(redisTemplate.getKeySerializer().serialize(key), redisTemplate.getValueSerializer().serialize(value), expireTime, "nx");
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public <T> T get(String key, Class<T> clazz) {
        return clazz.cast(redisTemplate.opsForValue().get(key));
    }

    public Boolean del(String... keys) {
        Set<String> keySet = new HashSet<>();
        for (String key : keys) {
            keySet.add(key);
        }
        redisTemplate.delete(keySet);
        return Boolean.TRUE;
    }

    public Boolean delKey(String key) {
        redisTemplate.delete(key);
        return Boolean.TRUE;
    }

    public Boolean sAdd(String key, Object value, int expireTime) {
        redisTemplate.opsForSet().add(key, value);
        if (expireTime > 0) {
            this.setExpire(key, expireTime);
        }
        return Boolean.TRUE;
    }

    public Boolean sAdd(String key, Object value) {
        return this.sAdd(key, value, 0);
    }

    public Boolean hSet(String key, String field, Object value, int expireTime) {
        redisTemplate.opsForHash().put(key, field, value);
        if (expireTime > 0) {
            this.setExpire(key, expireTime);
        }
        return Boolean.TRUE;
    }

    public Boolean hSet(String key, String field, Object value) {
        return this.hSet(key, field, value, 0);
    }

    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    public <T> T hGet(String key, String field, Class<T> clazz) {
        return clazz.cast(redisTemplate.opsForHash().get(key, field));
    }

    public Boolean hDel(String key, String... fields) {
        Set<String> fieldSet = new HashSet<>();
        for (String field : fields) {
            fieldSet.add(field);
        }
        redisTemplate.opsForHash().delete(key, fieldSet);
        return Boolean.TRUE;
    }

    public Set<String> keys(String key) {
        return redisTemplate.keys(key);
    }

    public boolean setExpire(String key, int expireTime) {
        return redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
    }

    public Boolean lPush(String key, Object value) {
        return this.lPush(key, value, 0);
    }

    public Boolean lPush(String key, Object value, int expireTime) {
        redisTemplate.opsForList().leftPush(key, value);
        if (expireTime > 0) {
            this.setExpire(key, expireTime);
        }
        return Boolean.TRUE;
    }

    public List<String> lRange(String key) {
        return lRange(key, 0, -1);
    }

    public List<String> lRange(String key, long start, long len) {
        return redisTemplate.opsForList().range(key, start, len);
    }

    public Object lPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    public <T> T lPop(String key, Class<T> clazz) {
        return clazz.cast(redisTemplate.opsForList().leftPop(key));
    }

    public Boolean rPush(String key, Object value) {
        return this.rPush(key, value, 0);
    }

    public Boolean rPush(String key, Object value, int expireTime) {
        redisTemplate.opsForList().rightPush(key, value);
        if (expireTime > 0) {
            this.setExpire(key, expireTime);
        }
        return Boolean.TRUE;
    }

    public Object rPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    public <T> T rPop(String key, Class<T> clazz) {
        return clazz.cast(redisTemplate.opsForList().rightPop(key));
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean zSet(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    public Set<String> zGet(String key) {
        return redisTemplate.opsForZSet().range(key, 0, -1);
    }

    public boolean delZset(String key) {
        redisTemplate.delete(key);
        return Boolean.TRUE;
    }

}
