package com.fourformance.tts_vc_web.dto.response;

import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class ResponseDto {

    private final Boolean success;
    private final Integer code;
    private final String message;

    public static ResponseDto of(Boolean success, ErrorCode errorCode) {
        return new ResponseDto(success, errorCode.getCode(), errorCode.getMessage());
    }
}