package com.priyaanshu.url_shortener.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.priyaanshu.url_shortener.dto.UrlAnalyticsDto;
import com.priyaanshu.url_shortener.dto.UrlRequestDto;
import com.priyaanshu.url_shortener.dto.UrlResponseDto;
import com.priyaanshu.url_shortener.enums.RateLimitType;
import com.priyaanshu.url_shortener.exception.RateLimitExceededException;
import com.priyaanshu.url_shortener.exception.UrlNotFoundException;
import com.priyaanshu.url_shortener.service.RateLimiterService;
import com.priyaanshu.url_shortener.service.UrlShortenerService;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/urls")
public class UrlShortenerController {
	
	private final static Logger logger=LoggerFactory.getLogger(UrlShortenerController.class);
	
	private final UrlShortenerService urlShortenerService;
	
	private final RateLimiterService rateLimiterService;
	
	public UrlShortenerController(UrlShortenerService urlShortenerService,RateLimiterService rateLimiterService) {
		super();
		this.urlShortenerService = urlShortenerService;
		this.rateLimiterService=rateLimiterService;
	}

	@PostMapping("/shorten")
	public ResponseEntity<UrlResponseDto> shorten(@Valid @RequestBody UrlRequestDto requestDto,HttpServletRequest requests)
	{
		logger.info("Request to shorten");

		String ipAddress =requests.getRemoteAddr();
		logger.info("Client IP: {}",ipAddress);
		if(!rateLimiterService.isAllowed(ipAddress,RateLimitType.SHORTEN)) {

		    throw new RateLimitExceededException("Shorten URL limit exceeded");
		}
		UrlResponseDto urlResponseDto=this.urlShortenerService.shortenUrl(requestDto);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(urlResponseDto);
		
	}
	
	@GetMapping("/{shortCode}")
	public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
		logger.info("Request to get original url");

	    String originalUrl =urlShortenerService.getOriginalUrl(shortCode);
	   
	    return ResponseEntity.status(HttpStatus.FOUND).header("Location", originalUrl).build();
	}
	
	@GetMapping("/analytics/{shortCode}")
	public ResponseEntity<UrlAnalyticsDto> analytics(@PathVariable String shortCode,HttpServletRequest requests)
	{
		logger.info("Request to get analytics");
		String ipAddress=requests.getRemoteAddr();
		logger.info("Client IP: {}",ipAddress);
		if(!rateLimiterService.isAllowed(ipAddress,RateLimitType.ANALYTICS)) {

		    throw new RateLimitExceededException("Analytics limit exceeded");
		}
		UrlAnalyticsDto urlAnalyticsDto= this.urlShortenerService.getAnalytics(shortCode);
		
		return ResponseEntity.status(HttpStatus.OK).body(urlAnalyticsDto);
	}
}
