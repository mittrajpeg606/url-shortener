package com.priyaanshu.url_shortener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class UrlShortenerApplication {
	
	private static final Logger logger =LoggerFactory.getLogger(UrlShortenerApplication.class);
	public static void main(String[] args) {
		// start of the spring boot application
		SpringApplication.run(UrlShortenerApplication.class, args);
		logger.info("Application Started");
	}

}
