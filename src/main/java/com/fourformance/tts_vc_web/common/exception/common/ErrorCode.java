package com.fourformance.tts_vc_web.common.exception.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // HttpStatus는 애매하면 일단 BAD_REQUEST로 통일

    // 0번대 코드 : ok
    OK(0, HttpStatus.OK, "Ok"),

    // 1000번대 코드 : 오디오 파일 s3 버킷 업/다운로드 관련
    NOT_EXISTS_AUDIO(1001, HttpStatus.BAD_REQUEST, "존재하지 않는 파일에 대한 접근 시도입니다."),
    EMPTY_FILE(1002, HttpStatus.BAD_REQUEST, "파일이 비어 있습니다."), // *
    PROJECT_NOT_FOUND(1003, HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."), // *
    MEMBER_NOT_FOUND(1004, HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."), // *
    DETAIL_NOT_FOUND(1005, HttpStatus.NOT_FOUND, "유닛 정보를 찾을 수 없습니다."),
    UNSUPPORTED_PROJECT_TYPE(1006, HttpStatus.BAD_REQUEST, "지원되지 않는 프로젝트 유형입니다."), // *
    S3_UPLOAD_FAILED(1007, HttpStatus.INTERNAL_SERVER_ERROR, "S3 업로드에 실패했습니다."), // *
    FILE_PROCESSING_ERROR(1008, HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리 중 오류가 발생했습니다."), // *
    S3_PRESIGNED_URL_FAILED(1009, HttpStatus.INTERNAL_SERVER_ERROR, "S3 presigned URL 생성에 실패했습니다."), // *
    MISSING_REQUIRED_FIELD(1010,HttpStatus.NOT_FOUND,"VC_TRG 파일이 누락되었습니다."),
    INVALID_PROJECT_DATA(1011,HttpStatus.BAD_REQUEST,"AudioFileDto에 MemberAudioMetaId와 LocalAudioFile이 모두 누락되었습니다."),
    INVALID_TRG_FILE_COMBINATION(1012, HttpStatus.BAD_REQUEST, "trgVoiceId가 설정된 경우, audioFiles에 VC_TRG 파일이 포함될 수 없습니다."),
    MISSING_TRG_FILE(1013, HttpStatus.BAD_REQUEST, "VC_TRG 파일은 반드시 하나여야 합니다."),

    // 3000번대 코드 : DB 관련
    NOT_EXISTS_PROJECT(3000, HttpStatus.BAD_REQUEST, "해당 프로젝트를 찾을 수 없습니다."),
    AUDIO_NOT_FOUND_EXCEPTION(3400, HttpStatus.BAD_REQUEST, "VC Target 오디오를 찾을 수 없습니다."),

    // 4000번대 코드 : 서버 내부 오류, 코드 오류
    SERVER_ERROR(4000, HttpStatus.BAD_REQUEST,"서버 내부 오류가 발생했습니다."),
    INVALID_PROJECT_ID(40001,HttpStatus.BAD_REQUEST,"유효하지 않은 프로젝트 ID입니다."),

    // 5000번대 코드 : 서버 내부 오류 관련
    INTERNAL_SERVER_ERROR(5001, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    INVALID_VOICE_ID(5002, HttpStatus.BAD_REQUEST, "유효하지 않은 Voice ID입니다."),

    // 9999 : 테스트용 커스텀 예외
    TEST_ERROR(9999, HttpStatus.BAD_REQUEST, "테스트용 커스텀 예외입니다."),

    // 10000 : 알 수 없는 예외
    UNKNOWN_ERROR(10000, HttpStatus.BAD_REQUEST, "알 수 없는 에외입니다,"),
    METADATA_FORM_FAULT(1234,HttpStatus.BAD_REQUEST, "메타데이터 형식이 잘못되었습니다."),

    NOT_EXISTS_VOICESTYLE(3001,HttpStatus.BAD_REQUEST, "해당 voice style의 id를 찾을 수 없습니다." ),

    NOT_EXISTS_PROJECT_DETAIL(3100, HttpStatus.BAD_REQUEST, "해당 프로젝트 디테일을 찾을 수 없습니다."),

    DUPLICATE_UNIT_SEQUENCE(3200, HttpStatus.BAD_REQUEST, "중복된 unitSequence를 가집니다."),

    INVALID_UNIT_SEQUENCE_ORDER(3300, HttpStatus.BAD_REQUEST, "unitSequenc의 순서가 잘못됐습니다.");

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}