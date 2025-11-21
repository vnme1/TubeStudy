package com.tubestudy.tracker.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 클래스
 * 모든 컨트롤러에서 발생하는 예외를 일관되게 처리합니다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 404 Not Found 예외 처리
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoHandlerFoundException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", 404);
        errorResponse.put("message", "요청한 리소스를 찾을 수 없습니다.");
        errorResponse.put("path", ex.getRequestURL());
        errorResponse.put("timestamp", System.currentTimeMillis());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * 400 Bad Request 예외 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", 400);
        errorResponse.put("message", "잘못된 요청입니다: " + ex.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 500 Internal Server Error 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", 500);
        errorResponse.put("message", "서버에 오류가 발생했습니다.");
        errorResponse.put("error", ex.getClass().getSimpleName());
        errorResponse.put("timestamp", System.currentTimeMillis());

        // 개발 환경에서만 상세 메시지 표시 (필요시 spring.profiles.active 환경변수 확인)
        if (System.getProperty("debug") != null) {
            errorResponse.put("details", ex.getMessage());
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
