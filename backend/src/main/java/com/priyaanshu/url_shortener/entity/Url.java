package com.priyaanshu.url_shortener.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name="URLS")
public class Url {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long urlId;
	
	@Column(nullable=false,length=2048)
	private String originalUrl;
	
	@Column(nullable=false,unique=true)
	private String shortCode;
	
	@Column(nullable=false)
	private Long clickCount=0L;
	
	@Column(nullable=false)
	private LocalDateTime createdAt;
	
	private LocalDateTime lastAccessedAt;
	
	

	public Url(String originalUrl, String shortCode, Long clickCount, LocalDateTime createdAt,
			LocalDateTime lastAccessedAt) {
		super();
		this.originalUrl = originalUrl;
		this.shortCode = shortCode;
		this.clickCount = clickCount;
		this.createdAt = createdAt;
		this.lastAccessedAt = lastAccessedAt;
	}

	public Url() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getUrlId() {
		return urlId;
	}

	public String getOriginalUrl() {
		return originalUrl;
	}

	public String getShortCode() {
		return shortCode;
	}

	public Long getClickCount() {
		return clickCount;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getLastAccessedAt() {
		return lastAccessedAt;
	}

	public void setUrlId(Long urlId) {
		this.urlId = urlId;
	}

	public void setOriginalUrl(String originalUrl) {
		this.originalUrl = originalUrl;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public void setClickCount(Long clickCount) {
		this.clickCount = clickCount;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
		this.lastAccessedAt = lastAccessedAt;
	}

	@Override
	public String toString() {
		return "Url [urlId=" + urlId + ", originalUrl=" + originalUrl + ", shortCode=" + shortCode + ", clickCount="
				+ clickCount + ", createdAt=" + createdAt + ", lastAccessedAt=" + lastAccessedAt + "]";
	}
	
	
	
	
	
	

}
