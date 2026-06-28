package com.priyaanshu.url_shortener.schedular;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.priyaanshu.url_shortener.dao.UrlRepository;
import com.priyaanshu.url_shortener.entity.Url;
import com.priyaanshu.url_shortener.service.AnalyticsRedisService;

import jakarta.transaction.Transactional;

@Component
public class AnalyticsSyncScheduler {
	
	private static final Logger logger =LoggerFactory.getLogger(AnalyticsSyncScheduler.class);
	
	private final UrlRepository urlRepository;
	
	
	
	private final AnalyticsRedisService analyticsRedisService;

	public AnalyticsSyncScheduler(UrlRepository urlRepository,AnalyticsRedisService analyticsRedisService) {
		this.urlRepository = urlRepository;
		this.analyticsRedisService = analyticsRedisService;
	}
	
	@Scheduled(fixedRate = 300000)
	@Transactional
	public void syncAnalytics() {
		// scheduler method to sync redis with DB
		logger.info("Starting analytics synchronization job");
		List<Url> allUrls = this.urlRepository.findAll();
		List<Url> savedUrls=new ArrayList<>();
		
		for(Url url:allUrls)
		{
			String countClicks=analyticsRedisService.getClickCount(url.getShortCode());
			String lastAccessedAt=analyticsRedisService.getLastAccessed(url.getShortCode());
			
			if(countClicks==null){
				logger.debug("No analytics found in Redis for shortCode {}",url.getShortCode());
				continue;
			}
			
			url.setClickCount(Long.parseLong(countClicks));
			if(lastAccessedAt!=null && !lastAccessedAt.equals("null")) {
				url.setLastAccessedAt(LocalDateTime.parse(lastAccessedAt));
				
			}
			savedUrls.add(url);
		}
		urlRepository.saveAll(savedUrls);
		logger.info("Analytics synchronization completed. Updated {} URLs",savedUrls.size());
		
	}
	

}
