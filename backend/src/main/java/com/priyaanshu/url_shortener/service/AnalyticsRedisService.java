package com.priyaanshu.url_shortener.service;

import java.time.LocalDateTime;

public interface AnalyticsRedisService {

    Long incrementClickCount(String shortCode);

    void cacheUrl(String shortCode,String originalUrl);

    void cacheExistingUrl(String shortCode,String originalUrl,Long clickCount);
    
    void cacheOriginalUrlMapping(String originalUrl,String shortCode);

    String getShortCodeByOriginalUrl(String originalUrl);

    String getCachedUrl(String shortCode);

    void updateLastAccessed(String shortCode);

    String getLastAccessed(String shortCode);

    String getClickCount(String shortCode);
    
    Long incrementRateLimitCounter(String key);

    void initializeRateLimitWindow(String key,Long windowSeconds);
    
    public void updateLastAccessedFromDb(String shortCode,LocalDateTime lastAccessedAt);
}