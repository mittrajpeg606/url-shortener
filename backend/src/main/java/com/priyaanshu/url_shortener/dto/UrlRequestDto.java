package com.priyaanshu.url_shortener.dto;

import org.hibernate.validator.constraints.URL;

import com.priyaanshu.url_shortener.enums.GeneratorType;

import jakarta.validation.constraints.NotBlank;

public class UrlRequestDto {
	
	@NotBlank(message="URL cannot be empty")
	@URL
	private String originalUrl;
	
	 private GeneratorType generatorType;

	public String getOriginalUrl() {
		return originalUrl;
	}

	public void setOriginalUrl(String originalUrl) {
		this.originalUrl = originalUrl;
	}

	public GeneratorType getGeneratorType() {
		return generatorType;
	}

	public void setGeneratorType(GeneratorType generatorType) {
		this.generatorType = generatorType;
	}
	
	
}
