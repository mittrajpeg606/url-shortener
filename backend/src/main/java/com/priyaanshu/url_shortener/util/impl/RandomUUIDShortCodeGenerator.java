package com.priyaanshu.url_shortener.util.impl;

import com.priyaanshu.url_shortener.util.UrlShortCodeGeneratorUtil;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component("RandomUUIDShortCodeGenerator")
public class RandomUUIDShortCodeGenerator implements UrlShortCodeGeneratorUtil{

	@Override
	public String generateShortCode() {
		return null;
	}

	@Override
	public String generateShortCode(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String generateShortCode(String originalUrl) {
		return UUID.randomUUID().toString().substring(0, 6);	
	}

}
