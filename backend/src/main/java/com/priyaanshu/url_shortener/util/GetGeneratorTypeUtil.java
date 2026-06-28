package com.priyaanshu.url_shortener.util;

import java.util.Map;

import com.priyaanshu.url_shortener.dto.UrlRequestDto;
import com.priyaanshu.url_shortener.dto.UrlResponseDto;
import com.priyaanshu.url_shortener.enums.GeneratorType;


public interface GetGeneratorTypeUtil {
	public  UrlShortCodeGeneratorUtil getGenerator(Map<String,UrlShortCodeGeneratorUtil> generators,GeneratorType type); 
	

}
