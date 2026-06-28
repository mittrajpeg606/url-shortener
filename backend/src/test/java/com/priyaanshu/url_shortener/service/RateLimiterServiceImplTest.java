package com.priyaanshu.url_shortener.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.priyaanshu.url_shortener.enums.RateLimitType;
import com.priyaanshu.url_shortener.service.impl.AnalyticsRedisServiceImpl;
import com.priyaanshu.url_shortener.service.impl.RateLimiterServiceImpl;

@ExtendWith(MockitoExtension.class)
public class RateLimiterServiceImplTest {
	
	@Mock
	private AnalyticsRedisServiceImpl analyticsRedisService;
	
	@InjectMocks
	private RateLimiterServiceImpl rateLimiterService;
	
	@BeforeEach
	public void init()
	{
		    ReflectionTestUtils.setField(rateLimiterService,"shortenMaxRequests",10L);

		    ReflectionTestUtils.setField(rateLimiterService,"shortenWindowSeconds",60L);

		    ReflectionTestUtils.setField(rateLimiterService,"analyticsMaxRequests",20L);

		    ReflectionTestUtils.setField(rateLimiterService,"analyticsWindowSeconds",60L);
	}
	
	@Test
	public void RateLimiterServiceImpl_isAllowed_returnsTrue()
	{
		// Arrange 
			RateLimitType type=RateLimitType.SHORTEN;
			when(analyticsRedisService.incrementRateLimitCounter("rate_limit:SHORTEN:127.0.0.1")).thenReturn(6L);
		// Act
			boolean result=rateLimiterService.isAllowed("127.0.0.1", type);
		// Assert
		assertTrue(result);
		// verify calls
		verify(analyticsRedisService).incrementRateLimitCounter("rate_limit:SHORTEN:127.0.0.1");
		verify(analyticsRedisService,never()).initializeRateLimitWindow(anyString(),anyLong());
	}
	
	@Test
	public void RateLimiterServiceImpl_isAllowedFirstRequest_returnsTrue()
	{
		// Arrange 
			RateLimitType type=RateLimitType.SHORTEN;
			when(analyticsRedisService.incrementRateLimitCounter("rate_limit:SHORTEN:127.0.0.1")).thenReturn(1L);
		// Act
			boolean result=rateLimiterService.isAllowed("127.0.0.1", type);
		// Assert
		assertTrue(result);
		// verify calls
		verify(analyticsRedisService,times(1)).initializeRateLimitWindow("rate_limit:SHORTEN:127.0.0.1",60L);
		verify(analyticsRedisService).incrementRateLimitCounter("rate_limit:SHORTEN:127.0.0.1");
	}
	
	@Test
	public void RateLimiterServiceImpl_isNotAllowed_returnsFalse()
	{
		// Arrange 
			RateLimitType type=RateLimitType.SHORTEN;
			when(analyticsRedisService.incrementRateLimitCounter("rate_limit:SHORTEN:127.0.0.1")).thenReturn(11L);
		// Act
			boolean result=rateLimiterService.isAllowed("127.0.0.1", type);
		// Assert
		assertFalse(result);
		// verify calls
		verify(analyticsRedisService,times(1)).incrementRateLimitCounter("rate_limit:SHORTEN:127.0.0.1");
		verify(analyticsRedisService,never()).initializeRateLimitWindow(anyString(),anyLong());
	}
	
	
	// anayltics
	
	@Test
	public void RateLimiterServiceImpl_isAllowedAnalytics_returnsTrue()
	{
		// Arrange 
			RateLimitType type=RateLimitType.ANALYTICS;
			when(analyticsRedisService.incrementRateLimitCounter("rate_limit:ANALYTICS:127.0.0.1")).thenReturn(16L);
		// Act
			boolean result=rateLimiterService.isAllowed("127.0.0.1", type);
		// Assert
		assertTrue(result);
		// verify calls
		verify(analyticsRedisService).incrementRateLimitCounter("rate_limit:ANALYTICS:127.0.0.1");
		verify(analyticsRedisService,never()).initializeRateLimitWindow(anyString(),anyLong());
	}
	
	@Test
	public void RateLimiterServiceImpl_isAllowedAnalyticsFirstRequest_returnsTrue()
	{
		// Arrange 
			RateLimitType type=RateLimitType.ANALYTICS;
			when(analyticsRedisService.incrementRateLimitCounter("rate_limit:ANALYTICS:127.0.0.1")).thenReturn(1L);
		// Act
			boolean result=rateLimiterService.isAllowed("127.0.0.1", type);
		// Assert
		assertTrue(result);
		// verify calls
		verify(analyticsRedisService,times(1)).initializeRateLimitWindow("rate_limit:ANALYTICS:127.0.0.1",60L);
		verify(analyticsRedisService).incrementRateLimitCounter("rate_limit:ANALYTICS:127.0.0.1");
	}
	
	@Test
	public void RateLimiterServiceImpl_isNotAllowedAnalytics_returnsFalse()
	{
		// Arrange 
			RateLimitType type=RateLimitType.ANALYTICS;
			when(analyticsRedisService.incrementRateLimitCounter("rate_limit:ANALYTICS:127.0.0.1")).thenReturn(21L);
		// Act
			boolean result=rateLimiterService.isAllowed("127.0.0.1", type);
		// Assert
		assertFalse(result);
		// verify calls
		verify(analyticsRedisService,times(1)).incrementRateLimitCounter("rate_limit:ANALYTICS:127.0.0.1");
		verify(analyticsRedisService,never()).initializeRateLimitWindow(anyString(),anyLong());
	}
	

}
