package com.priyaanshu.url_shortener.util.impl;

import java.util.Map;

import org.springframework.stereotype.Component;


import com.priyaanshu.url_shortener.enums.GeneratorType;
import com.priyaanshu.url_shortener.util.GetGeneratorTypeUtil;
import com.priyaanshu.url_shortener.util.UrlShortCodeGeneratorUtil;

@Component
public class GetGeneratorTypeImpl implements GetGeneratorTypeUtil{

	@Override
	public UrlShortCodeGeneratorUtil getGenerator(Map<String,UrlShortCodeGeneratorUtil> generators,GeneratorType type) {
		switch(type) {

        case UUID:
            return generators.get(
                    "RandomUUIDShortCodeGenerator");

        case RANDOM:
            return generators.get(
                    "RandomAlphaNumericShortCodeGenerator");

        case HASH:
            return generators.get(
                    "HashBasedShortCodeGenerator");

        default:
            throw new IllegalArgumentException(
                    "Unsupported generator type");
    }
	}
	
	

}
