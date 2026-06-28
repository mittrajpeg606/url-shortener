package com.priyaanshu.url_shortener.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.priyaanshu.url_shortener.entity.Url;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
	Optional<Url> findByShortCode(String shortCode);
	Optional<Url> findByOriginalUrl(String originalUrl);
}
