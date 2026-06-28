package com.priyaanshu.url_shortener.dto;

public class UrlResponseDto {
	
	private String originalUrl;
	private String shortUrl;
	private String shortCode;
	
	public UrlResponseDto(String originalUrl, String shortCode, String shortUrl) {
		super();
		this.originalUrl = originalUrl;
		this.shortUrl = shortUrl;
		this.shortCode = shortCode;
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


	
}
