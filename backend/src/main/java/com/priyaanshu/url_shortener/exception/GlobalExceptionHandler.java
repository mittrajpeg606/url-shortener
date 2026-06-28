package com.priyaanshu.url_shortener.exception;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.priyaanshu.url_shortener.dto.ErrorResponseDto;



@RestControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger logger =LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	@ExceptionHandler(UrlNotFoundException.class)
	public ResponseEntity<ErrorResponseDto> handleUrlNotFound(UrlNotFoundException e)
	{
		logger.error("URL not found exception occurred: {}",e.getMessage());
		ErrorResponseDto errorResponse=new ErrorResponseDto(LocalDateTime.now(),HttpStatus.NOT_FOUND.value(),"Not Found",e.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);		
		
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException e)
	{
		logger.error("Validation Failed , Request Url is empty: {}",e.getMessage());
		String message =e.getBindingResult().getFieldError().getDefaultMessage();
		
		ErrorResponseDto errorResponse=new ErrorResponseDto(LocalDateTime.now(),HttpStatus.BAD_REQUEST.value(),"Validation Failed",message);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);		
		
	}
	
	@ExceptionHandler(RateLimitExceededException.class)
	public ResponseEntity<String>handleRateLimitExceeded(RateLimitExceededException e) {

		logger.error("Requests limit exceeded per minute {}",e.getMessage());
	    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());
	}
	
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponseDto> httpMethodNotFound(HttpRequestMethodNotSupportedException e)
	{
		logger.error("Http method not allowed",e.getMessage());
		ErrorResponseDto errorResponse=new ErrorResponseDto(LocalDateTime.now(),HttpStatus.FORBIDDEN.value(),"Method not Allowed",e.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);		
		
	}
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponseDto> badRequest(HttpMessageNotReadableException e)
	{
		logger.error("Bad Request",e.getMessage());
		ErrorResponseDto errorResponse=new ErrorResponseDto(LocalDateTime.now(),HttpStatus.BAD_REQUEST.value(),"Bad Request",e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);		
		
	}
	
	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<ErrorResponseDto> nullPointerExceptionHandler(NullPointerException e)
	{
		logger.error("Request Parameter cannot null",e.getMessage());
		ErrorResponseDto errorResponse=new ErrorResponseDto(LocalDateTime.now(),HttpStatus.INTERNAL_SERVER_ERROR.value(),"Request Parameter cannot null",e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);		
		
	}
	
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponseDto> noResourceFoundHandler(NoResourceFoundException e)
	{
		logger.error("Resource Not Found",e.getMessage());
		ErrorResponseDto errorResponse=new ErrorResponseDto(LocalDateTime.now(),HttpStatus.NOT_FOUND.value(),"Resource Not Found , check url pattern or http method",e.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);		
		
	}
	
	
	

}
