package com.fourformance.tts_vc_web.common.exception.common;

import com.fourformance.tts_vc_web.dto.response.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDto> handleBusinessException(BusinessException e) {
        ErrorResponseDto errorResponse = ErrorResponseDto.of(e.getErrorCode());
        return new ResponseEntity<>(errorResponse, e.getErrorCode().getHttpStatus());
    }

    // 기타 예외에 대한 처리 예시
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception e) {
        ErrorResponseDto errorResponse = ErrorResponseDto.of(ErrorCode.UNKNOWN_ERROR);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}