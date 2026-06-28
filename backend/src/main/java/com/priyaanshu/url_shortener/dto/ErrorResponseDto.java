package com.priyaanshu.url_shortener.dto;

import java.time.LocalDateTime;

public class ErrorResponseDto {
	
	private LocalDateTime localDateTime;
	private int status;
	private String error;
	private String message;
	
	public ErrorResponseDto(LocalDateTime localDateTime, int status, String error, String message) {
		super();
		this.localDateTime = localDateTime;
		this.status = status;
		this.error = error;
		this.message = message;
	}

	public LocalDateTime getLocalDateTime() {
		return localDateTime;
	}

	public int getStatus() {
		return status;
	}

	public String getError() {
		return error;
	}

	public String getMessage() {
		return message;
	}
	
	

}
