package com.fourformance.tts_vc_web.dto.response;

import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
@RequiredArgsConstructor
public abstract class ResponseDto {

    private final Boolean success;
    private final Integer code;
    private final String message;

}
