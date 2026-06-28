package com.priyaanshu.url_shortener.service;

import com.priyaanshu.url_shortener.dto.UrlAnalyticsDto;
import com.priyaanshu.url_shortener.dto.UrlRequestDto;
import com.priyaanshu.url_shortener.dto.UrlResponseDto;

public interface UrlShortenerService {
	
	UrlResponseDto shortenUrl(UrlRequestDto url);
	String getOriginalUrl(String shortCode);
	UrlAnalyticsDto getAnalytics(String shortCode);

}
