package com.priyaanshu.url_shortener.service;

import com.priyaanshu.url_shortener.enums.RateLimitType;

public interface RateLimiterService {
	public boolean isAllowed(String ip,RateLimitType type);
}
