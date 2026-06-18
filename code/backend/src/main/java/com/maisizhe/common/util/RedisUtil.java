package com.maisizhe.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * 封装常用的Redis操作
 * 
 * @author MaiSiZhe Team
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 设置字符串值
     * 
     * @param key 键
     * @param value 值
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }
    
    /**
     * 设置字符串值和过期时间
     * 
     * @param key 键
     * @param value 值
     * @param timeout 过期时间(秒)
     */
    public void setEx(String key, String value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }
    
    /**
     * 获取字符串值
     * 
     * @param key 键
     * @return 值
     */
    public String get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * 删除键
     * 
     * @param key 键
     * @return true-成功，false-失败
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }
    
    /**
     * 判断键是否存在
     * 
     * @param key 键
     * @return true-存在，false-不存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * 自增(原子操作)
     * 
     * @param key 键
     * @return 自增后的值
     */
    public Long incr(String key) {
        return redisTemplate.opsForValue().increment(key);
    }
    
    /**
     * 设置ZSet成员分数
     * 
     * @param key 键
     * @param value 成员
     * @param score 分数
     */
    public void zAdd(String key, Object value, double score) {
        redisTemplate.opsForZSet().add(key, value, score);
    }
    
    /**
     * 获取ZSet指定范围的成员(按分数从小到大)
     * 
     * @param key 键
     * @param start 开始位置
     * @param end 结束位置
     * @return 成员集合
     */
    public Object[] zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end).toArray();
    }
    
    /**
     * 获取ZSet指定分数范围的成员数量
     * 
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 成员数量
     */
    public Long zCount(String key, double min, double max) {
        return redisTemplate.opsForZSet().count(key, min, max);
    }
    
    /**
     * 删除ZSet成员
     * 
     * @param key 键
     * @param values 成员
     */
    public void zRemove(String key, Object... values) {
        redisTemplate.opsForZSet().remove(key, values);
    }
    
    /**
     * 设置Hash字段值
     * 
     * @param key 键
     * @param hashKey 字段
     * @param value 值
     */
    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }
    
    /**
     * 获取Hash字段值
     * 
     * @param key 键
     * @param hashKey 字段
     * @return 值
     */
    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }
    
    /**
     * 设置键的过期时间
     * 
     * @param key 键
     * @param timeout 过期时间(秒)
     */
    public void expire(String key, long timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }
}
