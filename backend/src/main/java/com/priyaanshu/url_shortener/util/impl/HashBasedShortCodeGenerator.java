package com.priyaanshu.url_shortener.util.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Component;

import com.priyaanshu.url_shortener.util.UrlShortCodeGeneratorUtil;

@Component("HashBasedShortCodeGenerator")
public class HashBasedShortCodeGenerator implements UrlShortCodeGeneratorUtil {
	
	private static final int SHORT_CODE_LENGTH = 8;

	@Override
	public String generateShortCode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String generateShortCode(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String generateShortCode(String originalUrl) {
		  try {

	            MessageDigest digest =MessageDigest.getInstance("SHA-256");

	            byte[] hashBytes =digest.digest(originalUrl.getBytes(StandardCharsets.UTF_8));

	            StringBuilder hexHash =new StringBuilder();

	            for (byte b : hashBytes) {

	                hexHash.append(String.format("%02x", b));
	            }

	            return hexHash.substring(0,SHORT_CODE_LENGTH);

	        } catch (NoSuchAlgorithmException ex) {

	            throw new RuntimeException(
	                    "Error generating short code",
	                    ex);
	}
  }
}
