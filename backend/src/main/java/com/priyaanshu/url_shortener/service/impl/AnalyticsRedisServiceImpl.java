package com.priyaanshu.url_shortener.service.impl;

import static com.priyaanshu.url_shortener.constants.RedisConstants.*;

import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.priyaanshu.url_shortener.service.AnalyticsRedisService;

@Service
public class AnalyticsRedisServiceImpl implements AnalyticsRedisService {

    private static final Logger logger =
            LoggerFactory.getLogger(AnalyticsRedisServiceImpl.class);

    private final RedisTemplate<String, String> redisTemplate;

    public AnalyticsRedisServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Long incrementClickCount(String shortCode) {

        try {
            return redisTemplate.opsForValue().increment(CLICK_PREFIX + shortCode);
        } catch (Exception ex) {
            logger.warn("Redis unavailable while incrementing click count for {}", shortCode);
            return null;
        }
    }

    @Override
    public void cacheUrl(String shortCode, String originalUrl) {

        try {
            redisTemplate.opsForValue().set(shortCode, originalUrl);
            redisTemplate.opsForValue().set(CLICK_PREFIX + shortCode, "0");
            redisTemplate.opsForValue().set(LAST_ACCESSED_PREFIX + shortCode, NULL_VALUE);
        } catch (Exception ex) {
            logger.warn("Redis unavailable while caching URL {}", shortCode);
        }
    }

    @Override
    public void cacheExistingUrl(String shortCode, String originalUrl, Long clickCount) {

        try {
            redisTemplate.opsForValue().set(shortCode, originalUrl);
            redisTemplate.opsForValue().setIfAbsent(CLICK_PREFIX + shortCode, String.valueOf(clickCount));
            redisTemplate.opsForValue().setIfAbsent(LAST_ACCESSED_PREFIX + shortCode, NULL_VALUE);
        } catch (Exception ex) {
            logger.warn("Redis unavailable while caching existing URL {}", shortCode);
        }
    }

    @Override
    public void cacheOriginalUrlMapping(String originalUrl, String shortCode) {

        try {
            redisTemplate.opsForValue().set(ORIGINAL_URL_PREFIX + originalUrl, shortCode);
        } catch (Exception ex) {
            logger.warn("Redis unavailable while caching original URL mapping");
        }
    }

    @Override
    public String getShortCodeByOriginalUrl(String originalUrl) {

        try {
            return redisTemplate.opsForValue().get(ORIGINAL_URL_PREFIX + originalUrl);
        } catch (Exception ex) {
            logger.warn("Redis unavailable while fetching shortCode");
            return null;
        }
    }

    @Override
    public String getCachedUrl(String shortCode) {

        try {
            return redisTemplate.opsForValue().get(shortCode);
        } catch (Exception ex) {
            logger.warn("Redis unavailable while fetching cached URL {}", shortCode);
            return null;
        }
    }

    @Override
    public void updateLastAccessed(String shortCode) {

        try {
            redisTemplate.opsForValue().set(
                    LAST_ACCESSED_PREFIX + shortCode,
                    LocalDateTime.now().toString());
        } catch (Exception ex) {
            logger.warn("Redis unavailable while updating last accessed for {}", shortCode);
        }
    }

    @Override
    public void updateLastAccessedFromDb(String shortCode, LocalDateTime lastAccessedAt) {

        try {
            redisTemplate.opsForValue().set(
                    LAST_ACCESSED_PREFIX + shortCode,
                    lastAccessedAt.toString());
        } catch (Exception ex) {
            logger.warn("Redis unavailable while updating last accessed from DB");
        }
    }

    @Override
    public String getLastAccessed(String shortCode) {

        try {
            return redisTemplate.opsForValue().get(LAST_ACCESSED_PREFIX + shortCode);
        } catch (Exception ex) {
            logger.warn("Redis unavailable while fetching last accessed for {}", shortCode);
            return null;
        }
    }

    @Override
    public String getClickCount(String shortCode) {

        try {
            return redisTemplate.opsForValue().get(CLICK_PREFIX + shortCode);
        } catch (Exception ex) {
            logger.warn("Redis unavailable while fetching click count for {}", shortCode);
            return null;
        }
    }

    @Override
    public Long incrementRateLimitCounter(String key) {

        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception ex) {
            logger.warn("Redis unavailable while incrementing rate limit counter");
            return null;
        }
    }

    @Override
    public void initializeRateLimitWindow(String key, Long windowSeconds) {

        try {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        } catch (Exception ex) {
            logger.warn("Redis unavailable while initializing rate limit window");
        }
    }
}