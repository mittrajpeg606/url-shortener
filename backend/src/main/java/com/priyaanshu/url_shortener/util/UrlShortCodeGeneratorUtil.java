package com.priyaanshu.url_shortener.util;

public interface UrlShortCodeGeneratorUtil {
	String generateShortCode();
	String generateShortCode(Long id);
	String generateShortCode(String originalUrl);

}
