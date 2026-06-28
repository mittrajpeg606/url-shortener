package com.priyaanshu.url_shortener.util.impl;

import org.springframework.stereotype.Component;

import com.priyaanshu.url_shortener.util.UrlShortCodeGeneratorUtil;

import java.util.Random;

@Component("RandomAlphaNumericShortCodeGenerator")
public class RandomAlphaNumericShortCodeGenerator implements UrlShortCodeGeneratorUtil{

	
	private static final String CHARS ="ABCDEFGHIJKLMNOPQRSTUVWXYZ"+"abcdefghijklmnopqrstuvwxyz"+"0123456789";
	private final int LENGTH=7;
	Random random=new Random();
	
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
		StringBuilder code =new StringBuilder();

	    for(int i=0;i<LENGTH;i++) {

	        code.append(CHARS.charAt(random.nextInt(CHARS.length())));
	    }

	    return code.toString();
	}

}
