package com.priyaanshu.url_shortener.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.priyaanshu.url_shortener.exception.GlobalExceptionHandler;
import com.priyaanshu.url_shortener.exception.UrlNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyaanshu.url_shortener.constants.UrlConstants;
import com.priyaanshu.url_shortener.dto.UrlAnalyticsDto;
import com.priyaanshu.url_shortener.dto.UrlRequestDto;
import com.priyaanshu.url_shortener.dto.UrlResponseDto;
import com.priyaanshu.url_shortener.enums.GeneratorType;
import com.priyaanshu.url_shortener.enums.RateLimitType;
import com.priyaanshu.url_shortener.service.RateLimiterService;
import com.priyaanshu.url_shortener.service.UrlShortenerService;


@WebMvcTest(UrlShortenerController.class)
@Import(GlobalExceptionHandler.class)
public class UrlShortenerControllerTest {
	
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UrlShortenerService urlShortenerService;

	@MockitoBean
	private RateLimiterService rateLimiterService;
	
	private UrlRequestDto requestDto;
	private UrlResponseDto responseDto;
	private UrlAnalyticsDto analyticsDto;
	private LocalDateTime createdAt;
	private LocalDateTime lastAccessedAt;

	@BeforeEach
	public void init() {
		
		createdAt=LocalDateTime.now();
		lastAccessedAt=LocalDateTime.now();

	    requestDto = new UrlRequestDto();
	    requestDto.setOriginalUrl("http://www.google.com");
	    requestDto.setGeneratorType(GeneratorType.HASH);

	    responseDto = new UrlResponseDto("http://www.google.com","abc123",UrlConstants.BASE_SHORT_URL + "abc123");
	    analyticsDto=new UrlAnalyticsDto("http://www.google.com", UrlConstants.BASE_SHORT_URL + "abc123","abc123" ,12L, createdAt, lastAccessedAt);
	}
	
	@Test
	public void UrlShortenerController_shortenUrl_returnUrlResponse() throws Exception {

	    // Arrange
	    when(rateLimiterService.isAllowed(anyString(),eq(RateLimitType.SHORTEN))).thenReturn(true);

	    when(urlShortenerService.shortenUrl(any(UrlRequestDto.class))).thenReturn(responseDto);

	    // Act + Assert
	    mockMvc.perform(post("/api/v1/urls/shorten").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)))
	            .andExpect(status().isCreated())
	            .andExpect(jsonPath("$.originalUrl")
	                    .value("http://www.google.com"))
	            .andExpect(jsonPath("$.shortCode")
	                    .value("abc123"))
	            .andExpect(jsonPath("$.shortUrl")
	                    .value(UrlConstants.BASE_SHORT_URL + "abc123"));

	    // Verify
	    verify(rateLimiterService).isAllowed(anyString(), eq(RateLimitType.SHORTEN));

	    verify(urlShortenerService).shortenUrl(any(UrlRequestDto.class));
	}
	
	@Test
	public void UrlShortenerController_shortenUrl_returnLimitExceed() throws Exception {

	    // Arrange
	    when(rateLimiterService.isAllowed(anyString(),eq(RateLimitType.SHORTEN))).thenReturn(false);

	    // Act + Assert
	    mockMvc.perform(post("/api/v1/urls/shorten").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)))
	    .andExpect(status().isTooManyRequests())
        .andExpect(content().string("Shorten URL limit exceeded"));

	    // Verify
	    verify(rateLimiterService).isAllowed(anyString(), eq(RateLimitType.SHORTEN));
	    verify(urlShortenerService,never()).shortenUrl(any(UrlRequestDto.class));
	   
	}
	
	@Test
	public void UrlShortenerController_shortenUrl_validateRequest() throws Exception {

	    // Arrange
	    //when(rateLimiterService.isAllowed(anyString(),eq(RateLimitType.SHORTEN))).thenReturn(false);
		requestDto.setOriginalUrl("");

	    // Act + Assert
	    mockMvc.perform(post("/api/v1/urls/shorten").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)))
	    .andExpect(status().isBadRequest())
	    .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Validation Failed"))
        .andExpect(jsonPath("$.message").value("URL cannot be empty"));

	    // Verify
	    verifyNoInteractions(rateLimiterService);
	    verifyNoInteractions(urlShortenerService);
	   
	}
	
	@Test
	public void UrlShortenerController_redirect_redirectToUrl() throws Exception
	{
		// arrange
		when(urlShortenerService.getOriginalUrl("abc123")).thenReturn("http://www.google.com");
		
		// act + assert
		mockMvc.perform(get("/api/v1/urls/abc123")).andExpect(status().isFound()).andExpect(header().string("Location", "http://www.google.com"));
		
		// verify
		verifyNoInteractions(rateLimiterService);
		verify(urlShortenerService).getOriginalUrl("abc123");
	}
	
	@Test
	public void UrlShortenerController_redirect_UrlNotFound() throws Exception
	{
		// arrange
		when(urlShortenerService.getOriginalUrl("abc123")).thenThrow(new UrlNotFoundException("URL not found"));
		
		// act + assert
		mockMvc.perform(get("/api/v1/urls/abc123")).andExpect(status().isNotFound())
		.andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("URL not found"));
		
		// verify
		verifyNoInteractions(rateLimiterService);
		verify(urlShortenerService).getOriginalUrl("abc123");
	}
	
	
	@Test
	public void UrlShortenerController_getAnalytics_returnAnalytics() throws Exception {

	    // Arrange
	    when(rateLimiterService.isAllowed(anyString(),eq(RateLimitType.ANALYTICS))).thenReturn(true);

	    when(urlShortenerService.getAnalytics("abc123")).thenReturn(analyticsDto);

	    // Act + Assert
	    mockMvc.perform(get("/api/v1/urls/analytics/abc123"))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.originalUrl")
	                    .value("http://www.google.com"))
	            .andExpect(jsonPath("$.shortCode")
	                    .value("abc123"))
	            .andExpect(jsonPath("$.shortUrl")
	                    .value(UrlConstants.BASE_SHORT_URL + "abc123"))
	            .andExpect(jsonPath("$.clickCount").value(12L));    
	            
	    // Verify
	    
	    verify(rateLimiterService).isAllowed(anyString(), eq(RateLimitType.ANALYTICS));

	    verify(urlShortenerService).getAnalytics("abc123");
	}
	
	
	@Test
	public void UrlShortenerController_getanalytics_returnLimitExceed() throws Exception {

	    // Arrange
	    when(rateLimiterService.isAllowed(anyString(),eq(RateLimitType.ANALYTICS))).thenReturn(false);


	    // Act + Assert
	    mockMvc.perform(get("/api/v1/urls/analytics/abc123"))
	    .andExpect(status().isTooManyRequests())
        .andExpect(content().string("Analytics limit exceeded"));  
	            
	    // Verify
	    
	    verify(rateLimiterService).isAllowed(anyString(), eq(RateLimitType.ANALYTICS));

	    verify(urlShortenerService,never()).getAnalytics(anyString());
	}
	
	@Test
	public void UrlShortenerController_getanalytics_validateRequest() throws Exception {

	    // Arrange
	    when(rateLimiterService.isAllowed(anyString(),eq(RateLimitType.ANALYTICS))).thenReturn(true);
	    when(urlShortenerService.getAnalytics("abc123")).thenThrow(new UrlNotFoundException("URL not found"));

	    // Act + Assert
	    mockMvc.perform(get("/api/v1/urls/analytics/abc123")).andExpect(status().isNotFound())
		.andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("URL not found"));
	            
	    // Verify
	    
	    verify(rateLimiterService).isAllowed(anyString(), eq(RateLimitType.ANALYTICS));

	    verify(urlShortenerService).getAnalytics(anyString());
	}
	
	
}
