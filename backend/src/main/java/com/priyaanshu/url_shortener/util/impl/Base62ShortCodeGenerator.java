package com.priyaanshu.url_shortener.util.impl;



import org.springframework.stereotype.Component;

import com.priyaanshu.url_shortener.util.Base62EncoderShortCodeGeneratorUtil;


@Component("Base62ShortCodeGenerator")
public class Base62ShortCodeGenerator implements Base62EncoderShortCodeGeneratorUtil{
	

	private static final String BASE62 ="ABCDEFGHIJKLMNOPQRSTUVWXYZ"+"abcdefghijklmnopqrstuvwxyz"+"0123456789";
	
	
	@Override
	public String generateShortCode(Long id) {
		
		if(id == null || id <= 0) {
		    throw new IllegalArgumentException(
		            "Id must be greater than 0");
		}
		 StringBuilder sb =new StringBuilder();

		    while(id > 0) {

		        sb.append(BASE62.charAt((int)(id % 62)));

		        id /= 62;
		    }

		    String shortCode =sb.reverse().toString();

		    while(shortCode.length() < 6) {
		        shortCode = "A" + shortCode;
		    }

		    return shortCode;
	}


}
