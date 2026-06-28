package com.priyaanshu.url_shortener.dto;

import java.time.LocalDateTime;

public class UrlAnalyticsDto {

	private String originalUrl;
	private String shortUrl;
	private String shortCode;
	private Long clickCount=0L;
	private LocalDateTime createdAt;
	private LocalDateTime lastAccessedAt;
	
	public UrlAnalyticsDto(String originalUrl, String shortUrl, String shortCode, Long clickCount,
			LocalDateTime createdAt, LocalDateTime lastAccessedAt) {
		super();
		this.originalUrl = originalUrl;
		this.shortUrl = shortUrl;
		this.shortCode = shortCode;
		this.clickCount = clickCount;
		this.createdAt = createdAt;
		this.lastAccessedAt = lastAccessedAt;
	}

	public String getOriginalUrl() {
		return originalUrl;
	}

	public String getShortUrl() {
		return shortUrl;
	}

	public String getShortCode() {
		return shortCode;
	}

	public Long getClickCount() {
		return clickCount;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getLastAccessedAt() {
		return lastAccessedAt;
	}


	
}
