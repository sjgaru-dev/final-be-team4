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
    S3_DOWNLOAD_FAILED(1014, HttpStatus.BAD_REQUEST, "VC_TRG 파일은 반드시 하나여야 합니다."),

    // 3000번대 코드 : DB 관련
    NOT_EXISTS_PROJECT(3000, HttpStatus.BAD_REQUEST, "해당 프로젝트를 찾을 수 없습니다."),
    AUDIO_NOT_FOUND_EXCEPTION(3400, HttpStatus.BAD_REQUEST, "VC Target 오디오를 찾을 수 없습니다."),

    // 4000번대 코드 : 서버 내부 오류, 코드 오류
    SERVER_ERROR(4000, HttpStatus.BAD_REQUEST,"서버 내부 오류가 발생했습니다."),
    INVALID_REQUEST_DATA(4001, HttpStatus.BAD_REQUEST, "유효하지 않은 요청 데이터입니다."),
    INVALID_PROJECT_ID(40001,HttpStatus.BAD_REQUEST,"유효하지 않은 프로젝트 ID입니다."),

    // 5000번대 코드 : 서버 내부 오류 관련
    INTERNAL_SERVER_ERROR(5001, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    INVALID_VOICE_ID(5002, HttpStatus.BAD_REQUEST, "유효하지 않은 Voice ID입니다."),
    TTS_CREATE_FAILED(5003, HttpStatus.INTERNAL_SERVER_ERROR, "TTS 생성에 실패했습니다."),
    ID_REQUIRED_FOR_EXISTING_DATA(5004, HttpStatus.BAD_REQUEST, "기존 데이터에 대한 ID가 필요합니다."),
    NEW_DATA_PROCESSING_FAILED(5005, HttpStatus.INTERNAL_SERVER_ERROR, "New Data 처리에 실패했습니다."),
    TTS_DETAIL_NOT_FOUND(5006, HttpStatus.NOT_FOUND, "ID에 해당하는 TTSDetail을 찾을 수 없습니다."),
    EXISTING_DATA_PROCESSING_FAILED(5007, HttpStatus.INTERNAL_SERVER_ERROR, "Existing Data 처리에 실패했습니다."),
    TTS_CONVERSION_FAILED_EMPTY_CONTENT(5008, HttpStatus.INTERNAL_SERVER_ERROR, "TTS 변환 실패: 응답의 오디오 콘텐츠가 비어 있습니다."),
    TTS_CONVERSION_FAILED(5009, HttpStatus.INTERNAL_SERVER_ERROR, "TTS 변환 중 오류가 발생했습니다."),
    AUDIO_FILE_SAVE_ERROR(5010, HttpStatus.INTERNAL_SERVER_ERROR, "오디오 파일 저장 중 오류가 발생했습니다."),
    INVALID_TEXT_FOR_KO_KR(5011, HttpStatus.BAD_REQUEST, "언어 코드가 'ko-KR'로 설정되었지만, 텍스트는 한국어가 아닙니다."),
    INVALID_TEXT_FOR_ZH_CN(5012, HttpStatus.BAD_REQUEST, "언어 코드가 'zh-CN'로 설정되었지만, 텍스트는 중국어가 아닙니다."),
    INVALID_TEXT_FOR_JA_JP(5013, HttpStatus.BAD_REQUEST, "언어 코드가 'ja-JP'로 설정되었지만, 텍스트는 일본어가 아닙니다."),
    INVALID_TEXT_FOR_EN(5014, HttpStatus.BAD_REQUEST, "언어 코드가 설정되었지만, 텍스트는 영어가 아닙니다."),
    UNSUPPORTED_LANGUAGE_CODE(5015, HttpStatus.BAD_REQUEST, "지원되지 않는 언어 코드입니다."),
    TTS_DETAIL_PROCESSING_FAILED(5016, HttpStatus.INTERNAL_SERVER_ERROR, "TTSDetail 처리 중 오류가 발생했습니다."),
    DUPLICATE_TTS_DETAIL(5017, HttpStatus.INTERNAL_SERVER_ERROR,"중복된 TTSDetail이 발견되었습니다."),
    INVALID_UNIT_SCRIPT(5018, HttpStatus.BAD_REQUEST, "유효하지 않은 Unit Script입니다."),
    DIRECTORY_CREATION_FAILED(5019, HttpStatus.INTERNAL_SERVER_ERROR, "디렉토리 생성에 실패했습니다."),
    TTS_PROJECT_NOT_FOUND(5020, HttpStatus.INTERNAL_SERVER_ERROR, "TTS Project를 찾을 수 없습니다."),

    // 9999 : 테스트용 커스텀 예외
    TEST_ERROR(9999, HttpStatus.BAD_REQUEST, "테스트용 커스텀 예외입니다."),

    // 10000 : 알 수 없는 예외
    UNKNOWN_ERROR(10000, HttpStatus.BAD_REQUEST, "알 수 없는 에외입니다,"),
    METADATA_FORM_FAULT(1234,HttpStatus.BAD_REQUEST, "메타데이터 형식이 잘못되었습니다."),

    NOT_EXISTS_VOICESTYLE(3001, HttpStatus.BAD_REQUEST, "해당 voice style의 id를 찾을 수 없습니다."),

    NOT_EXISTS_PROJECT_DETAIL(3100, HttpStatus.BAD_REQUEST, "해당 프로젝트 디테일을 찾을 수 없습니다."),

    DUPLICATE_UNIT_SEQUENCE(3200, HttpStatus.BAD_REQUEST, "중복된 unitSequence를 가집니다."),

    INVALID_UNIT_SEQUENCE_ORDER(3300, HttpStatus.BAD_REQUEST, "unitSequenc의 순서가 잘못됐습니다."),

    MEMBER_PROJECT_NOT_MATCH(9001, HttpStatus.BAD_REQUEST, "해당 유저의 프로젝트가 아닙니다!"),
    PROJECT_DETAIL_NOT_MATCH(9002, HttpStatus.BAD_REQUEST, "해당 프로젝트의 유닛이 아닙니다."),
    DTO_NOT_LOGICAL(9003, HttpStatus.BAD_REQUEST, "DTO가 논리적으로 어색합니다.(자세한건 다음에 쓸게요)");

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}