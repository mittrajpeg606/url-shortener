package com.priyaanshu.url_shortener.service.impl;

// UrlShortenerService -- contains specific business logic

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.stereotype.Service;

import com.priyaanshu.url_shortener.constants.UrlConstants;

import com.priyaanshu.url_shortener.dao.UrlRepository;
import com.priyaanshu.url_shortener.dto.UrlRequestDto;
import com.priyaanshu.url_shortener.dto.UrlResponseDto;
import com.priyaanshu.url_shortener.entity.Url;
import com.priyaanshu.url_shortener.enums.GeneratorType;
import com.priyaanshu.url_shortener.exception.UrlNotFoundException;
import com.priyaanshu.url_shortener.dto.UrlAnalyticsDto;
import com.priyaanshu.url_shortener.service.AnalyticsRedisService;
import com.priyaanshu.url_shortener.service.UrlShortenerService;
import com.priyaanshu.url_shortener.util.GetGeneratorTypeUtil;
import com.priyaanshu.url_shortener.util.UrlShortCodeGeneratorUtil;
import com.priyaanshu.url_shortener.util.Base62EncoderShortCodeGeneratorUtil;

import jakarta.transaction.Transactional;

@Service
public class UrlShortenerServiceImpl implements UrlShortenerService{
	
	private static final Logger logger =LoggerFactory.getLogger(UrlShortenerServiceImpl.class);
	
	private final Map<String,UrlShortCodeGeneratorUtil> generators;
	
	private final GetGeneratorTypeUtil getGeneratorType;
	
	private final UrlRepository urlRepository;
	
	private final Base62EncoderShortCodeGeneratorUtil base62EncoderShortCodeGenerator;
	
	private final AnalyticsRedisService analyticsRedisService;
	

	public UrlShortenerServiceImpl(Map<String,UrlShortCodeGeneratorUtil> generators,GetGeneratorTypeUtil getGeneratorType, UrlRepository urlRepository,Base62EncoderShortCodeGeneratorUtil base62EncoderShortCodeGenerator,AnalyticsRedisService analyticsRedisService) {
		super();
		this.generators = generators;
		this.getGeneratorType = getGeneratorType;
		this.urlRepository = urlRepository;
		this.base62EncoderShortCodeGenerator=base62EncoderShortCodeGenerator;
		this.analyticsRedisService=analyticsRedisService;
	}
	


	@Override
	public UrlResponseDto shortenUrl(UrlRequestDto url) {
		// check existing url in redis
		
		String cachedShortCode=analyticsRedisService.getShortCodeByOriginalUrl(url.getOriginalUrl());
		
		if(cachedShortCode!=null)
		{
			logger.info("Redis cache hit for URL {}. Returning shortCode {}",url.getOriginalUrl(),cachedShortCode);
			return new UrlResponseDto(url.getOriginalUrl(),cachedShortCode,buildShortUrl(cachedShortCode));
		}
		logger.info("Redis cache miss for URL {}",url.getOriginalUrl());
		// check existing url in DB
		Optional<Url> existingUrl =urlRepository.findByOriginalUrl(url.getOriginalUrl());

		if(existingUrl.isPresent()) {
			Url existing = existingUrl.get();
			
			// populating the redis
			analyticsRedisService.cacheUrl(existing.getShortCode(),existing.getOriginalUrl());
			analyticsRedisService.cacheOriginalUrlMapping(existing.getOriginalUrl(),existing.getShortCode());
			logger.info("Cached existing URL mapping in Redis for shortCode {}",existing.getShortCode());
		    logger.info("URL already exists in DB {}",url.getOriginalUrl());

		    return buildExistingUrlResponse(existing);
		}
		// generates shortcode with base62 algo
		if(url.getGeneratorType()==GeneratorType.BASE62) {
			
			return generateShortCodeBase62(url);
			
		}
		// generates shortcode with other algorithms
		UrlShortCodeGeneratorUtil generator = this.getGeneratorType.getGenerator(generators, url.getGeneratorType());
		logger.info("Generating short code for URL : {} using Algorithm : {}",url.getOriginalUrl(),url.getGeneratorType());
		String shortCode= generateUniqueShortCode(generator,url.getOriginalUrl());
		logger.info("Generated Short Code : {}",shortCode);;
		String originalUrl=url.getOriginalUrl();
		String shortUrl=buildShortUrl(shortCode);
		
	
		UrlResponseDto urlPayload=new UrlResponseDto(originalUrl,shortCode,shortUrl);
		logger.info("Saving short code");
		saveShortUrl(urlPayload);
		
		return urlPayload;
	}
	
	@Override
	public String getOriginalUrl(String shortCode) {
		// redis cache hit
		String cachedUrl =analyticsRedisService.getCachedUrl(shortCode);
		
		if(cachedUrl!=null)
		{
			logger.info("Redis cache hit for shortCode {}",shortCode);
			updateAnalytics(shortCode);
			return cachedUrl;
		}
		// redis cache miss
		logger.info("Redis cache miss for shortCode {}",shortCode);
		logger.info("Searching for short code: {}",shortCode);
		Url urlResponse=getUrlByShortCode(shortCode);
		
		// update analytics of the url
		analyticsRedisService.cacheExistingUrl(shortCode,urlResponse.getOriginalUrl(),urlResponse.getClickCount());
		analyticsRedisService.cacheOriginalUrlMapping(urlResponse.getOriginalUrl(),shortCode);
		logger.info("Click counter initialized in Redis for {}",shortCode);
		updateAnalytics(shortCode);
		
		
		logger.info("Redirecting short code {} to URL {}",shortCode,urlResponse.getOriginalUrl());
		return urlResponse.getOriginalUrl();
	}
	

	@Override
	public UrlAnalyticsDto getAnalytics(String shortCode) {
		// returns analytics of given shortcode
		logger.info("Searching for short code: {}",shortCode);
		Url urlResponse=getUrlByShortCode(shortCode);
		logger.info("Analytics found for shortCode {} in DB",shortCode);
		// redis cache hit
		String countStr =analyticsRedisService.getClickCount(shortCode);
		String lastAccessedStr =analyticsRedisService.getLastAccessed(shortCode);
		Long clickCount;
		LocalDateTime lastAccessedAt;
		if(countStr!=null)
		{
			logger.info("Redis hit cache for analytics click count: {}",shortCode);
			clickCount=resolveClickCount(countStr, urlResponse);
		}else {
			// redis cache miss
			logger.info("Redis Cache miss for analytics click count: {}",shortCode);
			clickCount=urlResponse.getClickCount();
			analyticsRedisService.cacheExistingUrl(shortCode, urlResponse.getOriginalUrl(), clickCount);
			analyticsRedisService.cacheOriginalUrlMapping(urlResponse.getOriginalUrl(),shortCode);
			if(lastAccessedStr!=null)
			{
				analyticsRedisService.updateLastAccessedFromDb(shortCode, LocalDateTime.parse(lastAccessedStr));
			}
		}
		
		if(lastAccessedStr!=null)
		{
			logger.info("Redis hit cache for analytics last accessed: {}",shortCode);
			lastAccessedAt=resolveLastAccessed(lastAccessedStr, urlResponse);
		}else {
			// redis cache miss
			logger.info("Redis Cache miss for analytics last accessed: {}",shortCode);
			lastAccessedAt=urlResponse.getLastAccessedAt();
			analyticsRedisService.updateLastAccessedFromDb(shortCode,lastAccessedAt);
		}
		String shortUrl=buildShortUrl(shortCode);
		return new UrlAnalyticsDto(urlResponse.getOriginalUrl(),shortUrl,urlResponse.getShortCode(),clickCount,urlResponse.getCreatedAt(),lastAccessedAt);
		
	}
	
	@Transactional
	private void saveShortUrl(UrlResponseDto urlPayload)
	{
		// save the url details in redis and DB
		analyticsRedisService.cacheUrl(urlPayload.getShortCode(),urlPayload.getOriginalUrl());
		analyticsRedisService.cacheOriginalUrlMapping(urlPayload.getOriginalUrl(),urlPayload.getShortCode());
		logger.info("Cached shortCode {} in Redis",urlPayload.getShortCode());
		Url url=buildUrlEntity(urlPayload);
		logger.info("Persisting shortCode {}",urlPayload.getShortCode());
		urlRepository.save(url);
		logger.info("Successfully persisted shortCode {}",urlPayload.getShortCode());
	}

	@Transactional
	private UrlResponseDto generateShortCodeBase62(UrlRequestDto url) {
		
		Url requestUrl =buildBase62UrlEntity(url.getOriginalUrl());
		Url savedUrl = urlRepository.save(requestUrl);
		
		logger.info("Generating Base62 shortCode for URL ID {}",savedUrl.getUrlId());
		String shortCode =base62EncoderShortCodeGenerator.generateShortCode(savedUrl.getUrlId());
		
		
		logger.info("Generated Base62 shortCode {}",shortCode);
		
		savedUrl.setShortCode(shortCode);
		urlRepository.save(savedUrl);
		analyticsRedisService.cacheUrl(shortCode,url.getOriginalUrl());
		analyticsRedisService.cacheOriginalUrlMapping(savedUrl.getOriginalUrl(),shortCode);
		logger.info("Cached Base62 shortCode {} in Redis",shortCode);
		
		String shortUrl =buildShortUrl(shortCode);

		return new UrlResponseDto(savedUrl.getOriginalUrl(),shortCode,shortUrl);
			
	}
	
	private String generateUniqueShortCode(UrlShortCodeGeneratorUtil generator,String originalUrl) {

	    String shortCode =generator.generateShortCode(originalUrl);

	    while(urlRepository.findByShortCode(shortCode).isPresent()) {

	        logger.warn("Collision detected for {}",shortCode);

	        shortCode =generator.generateShortCode(originalUrl);
	    }

	    return shortCode;
	}
	
	private Url buildBase62UrlEntity(String originalUrl) {

	    Url url = new Url();

	    url.setOriginalUrl(originalUrl);

	    url.setShortCode(UUID.randomUUID().toString());

	    url.setClickCount(0L);

	    url.setCreatedAt(LocalDateTime.now());

	    return url;
	}
	
	private void updateAnalytics(String shortCode) {

	    Long clickCount = analyticsRedisService.incrementClickCount(shortCode);

	    // Redis unavailable -> Update analytics directly in DB
	    if (clickCount == null) {

	        logger.warn("Redis unavailable. Updating analytics in database for {}", shortCode);

	        Url url = getUrlByShortCode(shortCode);

	        url.setClickCount(url.getClickCount() + 1);
	        url.setLastAccessedAt(LocalDateTime.now());

	        urlRepository.save(url);

	        return;
	    }

	    // Redis is available
	    analyticsRedisService.updateLastAccessed(shortCode);

	    logger.info("Updated analytics in Redis for {}. Click count: {}", shortCode, clickCount);
	}
	
	private Url getUrlByShortCode(String shortCode) {

	    return urlRepository.findByShortCode(shortCode).orElseThrow(() ->new UrlNotFoundException("URL not found"));
	}
	
	private Long resolveClickCount(String countStr,Url url) {

	    if(countStr != null) {
	        return Long.parseLong(countStr);
	    }

	    return url.getClickCount();
	}
	private LocalDateTime resolveLastAccessed(String lastAccessedStr,Url url) {

	    if(lastAccessedStr != null && !"null".equals(lastAccessedStr)) {

	        return LocalDateTime.parse(lastAccessedStr);
	    }

	    return url.getLastAccessedAt();
	}
	
	private UrlResponseDto buildExistingUrlResponse(Url existing) {

	    return new UrlResponseDto(existing.getOriginalUrl(),existing.getShortCode(),buildShortUrl(existing.getShortCode()));
	}
	
	private String buildShortUrl(String shortCode) {

	    return UrlConstants.BASE_SHORT_URL + shortCode;
	}
	
	private Url buildUrlEntity(UrlResponseDto payload) {

	    return new Url(payload.getOriginalUrl(),payload.getShortCode(),0L,LocalDateTime.now(),null);
	}
	
	
}
