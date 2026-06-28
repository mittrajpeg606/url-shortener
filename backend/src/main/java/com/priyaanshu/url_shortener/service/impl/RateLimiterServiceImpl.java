package com.priyaanshu.url_shortener.service.impl;

// IP Based Rate Limiter Implementation , uses Redis 

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import com.priyaanshu.url_shortener.enums.RateLimitType;
import com.priyaanshu.url_shortener.service.AnalyticsRedisService;
import com.priyaanshu.url_shortener.service.RateLimiterService;

@Service
public class RateLimiterServiceImpl implements RateLimiterService{
	
	private final AnalyticsRedisService analyticsRedisService;
	
	@Value("${rate.limit.shorten.max-requests}")
	private Long shortenMaxRequests;

	@Value("${rate.limit.shorten.window-seconds}")
	private Long shortenWindowSeconds;

	@Value("${rate.limit.analytics.max-requests}")
	private Long analyticsMaxRequests;

	@Value("${rate.limit.analytics.window-seconds}")
	private Long analyticsWindowSeconds;
	
	

	public RateLimiterServiceImpl(AnalyticsRedisService analyticsRedisService) {
		this.analyticsRedisService = analyticsRedisService;
	}



	@Override
	public boolean isAllowed(String ip,RateLimitType type) {
		
		
		String key =buildRateLimitKey(ip, type);
		
		
		Long currentRequestCount =analyticsRedisService.incrementRateLimitCounter(key);
		
		if (currentRequestCount == null) {
		    return true;
		}
		
		Long maxRequests;
	    Long windowSeconds;

	    switch(type) {

        case SHORTEN -> {
            maxRequests = shortenMaxRequests;

            windowSeconds = shortenWindowSeconds;
        }

        case ANALYTICS -> {
            maxRequests = analyticsMaxRequests;

            windowSeconds = analyticsWindowSeconds;
        }

        default ->
                throw new IllegalArgumentException(
                        "Unsupported rate limit type");
    }

	    
	    if(currentRequestCount != null && currentRequestCount == 1) {

	        analyticsRedisService.initializeRateLimitWindow(key,windowSeconds);
	    }
		
		return currentRequestCount!=null && currentRequestCount<=maxRequests;
		
	}
	
	private String buildRateLimitKey(String ip,RateLimitType type) {

	    return "rate_limit:" + type.name() + ":" + ip;
	}

}
