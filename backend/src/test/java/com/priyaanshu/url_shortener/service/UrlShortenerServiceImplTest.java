package com.priyaanshu.url_shortener.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.priyaanshu.url_shortener.constants.UrlConstants;
import com.priyaanshu.url_shortener.dao.UrlRepository;
import com.priyaanshu.url_shortener.dto.UrlAnalyticsDto;
import com.priyaanshu.url_shortener.dto.UrlRequestDto;
import com.priyaanshu.url_shortener.dto.UrlResponseDto;
import com.priyaanshu.url_shortener.entity.Url;
import com.priyaanshu.url_shortener.enums.GeneratorType;
import com.priyaanshu.url_shortener.exception.UrlNotFoundException;
import com.priyaanshu.url_shortener.service.impl.UrlShortenerServiceImpl;
import com.priyaanshu.url_shortener.util.Base62EncoderShortCodeGeneratorUtil;
import com.priyaanshu.url_shortener.util.GetGeneratorTypeUtil;
import com.priyaanshu.url_shortener.util.UrlShortCodeGeneratorUtil;

@ExtendWith(MockitoExtension.class)
public class UrlShortenerServiceImplTest {
	
	@Mock
	private AnalyticsRedisService analyticsRedisService;
	
	@Mock
	private UrlRepository urlRepository;
	
	@Mock
	private GetGeneratorTypeUtil getGeneratorType;

	@Mock
	private Base62EncoderShortCodeGeneratorUtil base62EncoderShortCodeGenerator;

	@Mock
	private Map<String, UrlShortCodeGeneratorUtil> generators;
	
	@Mock
	private UrlShortCodeGeneratorUtil generator;
	
	@InjectMocks
	private UrlShortenerServiceImpl urlShortenerServiceImpl;
	
	private UrlRequestDto urlRequestDto;
	
	private Url url;
	
	@BeforeEach
	public void init()
	{
		urlRequestDto=new UrlRequestDto();
		urlRequestDto.setOriginalUrl("www.google.com");
		urlRequestDto.setGeneratorType(GeneratorType.HASH);
		
		url=new Url("www.google.com","abc123",7L,LocalDateTime.now(),LocalDateTime.now());
	}
	
	@Test
	public void UrlShortenerServiceImpl_shortenUrl_redisHitShortcode()
	{
		//Arrange
		when(analyticsRedisService.getShortCodeByOriginalUrl("www.google.com")).thenReturn("abc123");
		//Act
		UrlResponseDto response=urlShortenerServiceImpl.shortenUrl(urlRequestDto);
		//Assert
		assertEquals(urlRequestDto.getOriginalUrl(),response.getOriginalUrl());
		assertEquals("abc123",response.getShortCode());
		assertEquals(UrlConstants.BASE_SHORT_URL + "abc123",response.getShortUrl());
		
		// verify
		verify(analyticsRedisService).getShortCodeByOriginalUrl("www.google.com");
		verifyNoInteractions(urlRepository);
		verifyNoInteractions(getGeneratorType);
		verifyNoInteractions(base62EncoderShortCodeGenerator);
		verifyNoInteractions(generators);
		
	}
	
	@Test
	public void UrlShortenerServiceImpl_shortenUrl_redisMissShortcode()
	{
		//Arrange
		when(analyticsRedisService.getShortCodeByOriginalUrl("www.google.com")).thenReturn(null);
		when(urlRepository.findByOriginalUrl("www.google.com")).thenReturn(Optional.of(url));
		//Act
		UrlResponseDto response=urlShortenerServiceImpl.shortenUrl(urlRequestDto);
		//Assert
		assertEquals(urlRequestDto.getOriginalUrl(),response.getOriginalUrl());
		assertEquals("abc123",response.getShortCode());
		assertEquals(UrlConstants.BASE_SHORT_URL + "abc123",response.getShortUrl());
		
		// verify
		verify(analyticsRedisService).getShortCodeByOriginalUrl("www.google.com");
		verify(analyticsRedisService).cacheUrl("abc123", "www.google.com");
		verify(analyticsRedisService).cacheOriginalUrlMapping("www.google.com", "abc123");
		verify(urlRepository).findByOriginalUrl("www.google.com");
		verify(urlRepository, never()).save(any(Url.class));
		verify(analyticsRedisService, never()).incrementClickCount(anyString());
		verifyNoInteractions(getGeneratorType);
		verifyNoInteractions(base62EncoderShortCodeGenerator);
		
		
	}

	
	@Test
	public void UrlShortenerServiceImpl_shortenUrl_redisMissDBMissShortcode()
	{
		//Arrange
		when(analyticsRedisService.getShortCodeByOriginalUrl("www.google.com")).thenReturn(null);
		when(urlRepository.findByOriginalUrl("www.google.com")).thenReturn(Optional.empty());
		when(getGeneratorType.getGenerator(generators,urlRequestDto.getGeneratorType())).thenReturn(generator);
		when(generator.generateShortCode("www.google.com")).thenReturn("abc123");
		when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.empty());
		//Act
		UrlResponseDto response=urlShortenerServiceImpl.shortenUrl(urlRequestDto);
		//Assert
		assertEquals(urlRequestDto.getOriginalUrl(),response.getOriginalUrl());
		assertEquals("abc123",response.getShortCode());
		assertEquals(UrlConstants.BASE_SHORT_URL + "abc123",response.getShortUrl());
		
		// verify
		verify(analyticsRedisService).getShortCodeByOriginalUrl("www.google.com");
		verify(analyticsRedisService).cacheUrl("abc123", "www.google.com");
		verify(analyticsRedisService).cacheOriginalUrlMapping("www.google.com", "abc123");
		verify(urlRepository).findByOriginalUrl("www.google.com");
		verify(getGeneratorType).getGenerator(generators,GeneratorType.HASH);
		verify(generator).generateShortCode("www.google.com");
		verify(urlRepository).findByShortCode("abc123");
		verify(urlRepository).save(any(Url.class));
		verifyNoInteractions(base62EncoderShortCodeGenerator);
		
		
	}
	
	@Test
	public void UrlShortenerServiceImpl_getOriginalUrl_redisHitShortcode()
	{
		//Arrange
		when(analyticsRedisService.getCachedUrl("abc123")).thenReturn("www.google.com");
		when(analyticsRedisService.incrementClickCount("abc123")).thenReturn(6L);
		// Act
		String originalUrl=urlShortenerServiceImpl.getOriginalUrl("abc123");
		
		//Assert
		assertEquals("www.google.com",originalUrl);
		
		//verify
		verify(analyticsRedisService).incrementClickCount("abc123");
		verify(analyticsRedisService).getCachedUrl("abc123");
		verify(analyticsRedisService).updateLastAccessed("abc123");
		verify(analyticsRedisService,never()).cacheExistingUrl(anyString(), anyString(), anyLong());
		verify(analyticsRedisService,never()).cacheOriginalUrlMapping(anyString(), anyString());
		verifyNoInteractions(urlRepository);
		verifyNoInteractions(getGeneratorType);
		verifyNoInteractions(base62EncoderShortCodeGenerator);
		verifyNoInteractions(generators);
		
	}
	
	@Test
	public void UrlShortenerServiceImpl_getOriginalUrl_redisMissDbHitShortcode()
	{
		//Arrange
		when(analyticsRedisService.getCachedUrl("abc123")).thenReturn(null);
		when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(url));
		when(analyticsRedisService.incrementClickCount("abc123")).thenReturn(6L);
		// Act
		String originalUrl=urlShortenerServiceImpl.getOriginalUrl("abc123");
		
		//Assert
		assertEquals("www.google.com",originalUrl);
		
		//verify
		verify(analyticsRedisService).incrementClickCount("abc123");
		verify(urlRepository).findByShortCode("abc123");
		verify(analyticsRedisService).getCachedUrl("abc123");
		verify(analyticsRedisService).updateLastAccessed("abc123");
		verify(analyticsRedisService).cacheExistingUrl("abc123", "www.google.com", 7L);
		verify(analyticsRedisService).cacheOriginalUrlMapping("www.google.com", "abc123");
		verifyNoInteractions(getGeneratorType);
		verifyNoInteractions(base62EncoderShortCodeGenerator);
		verifyNoInteractions(generators);
		
	}
	
	@Test
	public void UrlShortenerServiceImpl_getOriginalUrl_redisMissDbMissShortcode()
	{
		//Arrange
		when(analyticsRedisService.getCachedUrl("abc123")).thenReturn(null);
		when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.empty());

		// Act + Assert
		UrlNotFoundException exception = assertThrows(UrlNotFoundException.class,() -> urlShortenerServiceImpl.getOriginalUrl("abc123"));
		assertEquals("URL not found", exception.getMessage());
		
		//verify
		verify(analyticsRedisService, never()).incrementClickCount("abc123");
		verify(urlRepository).findByShortCode("abc123");
		verify(analyticsRedisService).getCachedUrl("abc123");
		verify(analyticsRedisService,never()).updateLastAccessed("abc123");
		verify(analyticsRedisService,never()).cacheExistingUrl(anyString(),anyString(),anyLong());
		verify(analyticsRedisService,never()).cacheOriginalUrlMapping(anyString(),anyString());
		verifyNoInteractions(getGeneratorType);
		verifyNoInteractions(base62EncoderShortCodeGenerator);
		verifyNoInteractions(generators);
		
	}
	
	@Test
	public void UrlShortenerServiceImpl_getAnalytics_redisHit() {

	    // Arrange
	    LocalDateTime lastAccessed = LocalDateTime.now();

	    when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(url));

	    when(analyticsRedisService.getClickCount("abc123")).thenReturn("7");

	    when(analyticsRedisService.getLastAccessed("abc123")).thenReturn(lastAccessed.toString());

	    // Act
	    UrlAnalyticsDto response =urlShortenerServiceImpl.getAnalytics("abc123");

	    // Assert
	    assertEquals("www.google.com", response.getOriginalUrl());
	    assertEquals("abc123", response.getShortCode());
	    assertEquals(7L, response.getClickCount());
	    assertEquals(UrlConstants.BASE_SHORT_URL + "abc123",response.getShortUrl());
	    assertEquals(url.getCreatedAt(), response.getCreatedAt());
	    assertEquals(lastAccessed, response.getLastAccessedAt());

	    // Verify
	    verify(urlRepository).findByShortCode("abc123");

	    verify(analyticsRedisService).getClickCount("abc123");
	    verify(analyticsRedisService).getLastAccessed("abc123");

	    verify(analyticsRedisService, never()).cacheExistingUrl(anyString(), anyString(), anyLong());

	    verify(analyticsRedisService, never()).cacheOriginalUrlMapping(anyString(), anyString());

	    verify(analyticsRedisService, never()).updateLastAccessedFromDb(anyString(), any(LocalDateTime.class));
	}
	
	
	@Test
	public void UrlShortenerServiceImpl_getAnalytics_clickCountRedisMiss() {

	    // Arrange
	    LocalDateTime lastAccessed = LocalDateTime.now();

	    when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(url));

	    when(analyticsRedisService.getClickCount("abc123")).thenReturn(null);

	    when(analyticsRedisService.getLastAccessed("abc123")).thenReturn(lastAccessed.toString());

	    // Act
	    UrlAnalyticsDto response =urlShortenerServiceImpl.getAnalytics("abc123");

	    // Assert
	    assertEquals("www.google.com",response.getOriginalUrl());
	    assertEquals("abc123",response.getShortCode());
	    assertEquals(7L,response.getClickCount());
	    assertEquals(lastAccessed,response.getLastAccessedAt());

	    // Verify
	    verify(urlRepository).findByShortCode("abc123");

	    verify(analyticsRedisService).getClickCount("abc123");

	    verify(analyticsRedisService).getLastAccessed("abc123");

	    verify(analyticsRedisService).cacheExistingUrl("abc123","www.google.com",7L);

	    verify(analyticsRedisService).cacheOriginalUrlMapping("www.google.com","abc123");

	    verify(analyticsRedisService).updateLastAccessedFromDb("abc123",lastAccessed);
	}
	
	@Test
	public void UrlShortenerServiceImpl_getAnalytics_lastAccessedRedisMiss() {

	    // Arrange
	    when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(url));

	    when(analyticsRedisService.getClickCount("abc123")).thenReturn("7");

	    when(analyticsRedisService.getLastAccessed("abc123")).thenReturn(null);

	    // Act
	    UrlAnalyticsDto response =urlShortenerServiceImpl.getAnalytics("abc123");

	    // Assert
	    assertEquals("www.google.com",response.getOriginalUrl());
	    assertEquals("abc123",response.getShortCode());
	    assertEquals(7L,response.getClickCount());
	    assertEquals(url.getLastAccessedAt(),response.getLastAccessedAt());

	    // Verify
	    verify(urlRepository).findByShortCode("abc123");

	    verify(analyticsRedisService).getClickCount("abc123");

	    verify(analyticsRedisService).getLastAccessed("abc123");

	    verify(analyticsRedisService).updateLastAccessedFromDb("abc123",url.getLastAccessedAt());

	    verify(analyticsRedisService,never()).cacheExistingUrl(anyString(),anyString(),anyLong());

	    verify(analyticsRedisService,never()).cacheOriginalUrlMapping(anyString(),anyString());
	}
	
	@Test
	public void UrlShortenerServiceImpl_getAnalytics_dbMiss() {

	    // Arrange
	    when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.empty());

	    // Act + Assert
	    UrlNotFoundException exception =assertThrows(UrlNotFoundException.class,() -> urlShortenerServiceImpl.getAnalytics("abc123"));

	    assertEquals("URL not found",exception.getMessage());

	    // Verify
	    verify(urlRepository).findByShortCode("abc123");

	    verify(analyticsRedisService,never()).getClickCount(anyString());

	    verify(analyticsRedisService,never()).getLastAccessed(anyString());

	    verify(analyticsRedisService,never()).cacheExistingUrl(anyString(),anyString(),anyLong());
	    
	    verify(analyticsRedisService,never()).cacheOriginalUrlMapping(anyString(),anyString());

	    verify(analyticsRedisService,never()).updateLastAccessedFromDb(anyString(),any(LocalDateTime.class));
	}

}
