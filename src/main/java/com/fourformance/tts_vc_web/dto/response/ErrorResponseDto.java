package com.fourformance.tts_vc_web.dto.response;

import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;

public class ErrorResponseDto extends ResponseDto {

    private ErrorResponseDto(ErrorCode errorCode) {
        super(false, errorCode.getCode(), errorCode.getMessage());
    }

    public static ErrorResponseDto of(ErrorCode errorCode) {
        return new ErrorResponseDto(errorCode);
    }
}