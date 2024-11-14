package com.fourformance.tts_vc_web.common.exception.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // HttpStatus는 애매하면 일단 BAD_REQUEST로 통일합시다.

    // 0번대 코드 : ok
    OK(0, HttpStatus.OK, "Ok"),

    // 1000번대 코드 : 오디오 파일 s3 버킷 업/다운로드 관련
    NOT_EXISTS_AUDIO(1001, HttpStatus.BAD_REQUEST, "존재하지 않는 파일에 대한 접근 시도입니다."),

    // ... 개발 중 추가

    // 500번대 코드 : 서버 내부 오류 관련
    INTERNAL_SERVER_ERROR(5001, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 9999 : 테스트용 커스텀 예외
    TEST_ERROR(9999, HttpStatus.BAD_REQUEST, "테스트용 커스텀 예외입니다."),

    // 10000 : 알 수 없는 예외
    UNKNOWN_ERROR(10000, HttpStatus.BAD_REQUEST, "알 수 없는 에외입니다,");

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;

}