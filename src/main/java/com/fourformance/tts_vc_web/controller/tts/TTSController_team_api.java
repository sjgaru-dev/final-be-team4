package com.fourformance.tts_vc_web.controller.tts;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
import com.fourformance.tts_vc_web.dto.response.ResponseDto;
import com.fourformance.tts_vc_web.dto.tts.TTSResponseDto;
import com.fourformance.tts_vc_web.dto.tts.TTSSaveDto;
import com.fourformance.tts_vc_web.service.tts.TTSService_team_api;
import io.swagger.v3.oas.annotations.Operation; // Swagger Operation 어노테이션
import io.swagger.v3.oas.annotations.tags.Tag; // Swagger Tag 어노테이션
import io.swagger.v3.oas.annotations.responses.ApiResponse; // Swagger ApiResponse 어노테이션
import io.swagger.v3.oas.annotations.responses.ApiResponses; // Swagger ApiResponses 어노테이션
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TTS API 컨트롤러
 * 이 컨트롤러는 텍스트를 오디오 파일로 변환하는 기능을 제공합니다.
 */
@Tag(name = "tts-controller-_team-_api", description = "텍스트를 오디오 파일로 변환하는 API") // Swagger에서 사용할 태그 정의
@RestController
@RequestMapping("/tts") // API 요청 매핑
public class TTSController_team_api {

    private static final Logger LOGGER = Logger.getLogger(TTSController_team_api.class.getName()); // 로깅 설정
    private final TTSService_team_api ttsService; // TTS 변환 서비스 의존성 주입

    /**
     * 생성자: TTSService_team_api 의존성을 주입받아 초기화
     *
     * @param ttsService TTS 변환 서비스
     */
    public TTSController_team_api(TTSService_team_api ttsService) {
        this.ttsService = ttsService;
    }

    /**
     * TTS 배치 변환 API
     * 주어진 텍스트 목록을 Google TTS API를 사용하여 음성 파일로 변환합니다.
     *
     * @param ttsSaveDto 변환 요청 데이터 (텍스트와 관련 설정 정보 포함)
     * @return 변환된 음성 파일의 URL 목록을 포함한 응답 객체
     */
    @Operation(summary = "TTS 배치 변환", description = "주어진 텍스트 목록을 오디오 파일로 변환합니다.") // Swagger에서 사용할 API 요약 정보와 설명
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 TTS 변환 완료"), // 성공 응답
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"), // 잘못된 요청 응답
            @ApiResponse(responseCode = "500", description = "서버 내부 오류") // 서버 오류 응답
    })
    @PostMapping("/convert/batch") // HTTP POST 메서드와 엔드포인트 매핑
    public ResponseDto convertBatchTexts(@RequestBody TTSSaveDto ttsSaveDto) { // 요청 데이터로 TTSSaveDto 사용
        LOGGER.info("컨트롤러 메서드 호출됨: " + ttsSaveDto); // 요청 데이터 로깅

        // 유효성 검증: 요청 데이터가 null이거나 텍스트 세부사항 리스트가 비어있는 경우 예외 처리
        if (ttsSaveDto == null || ttsSaveDto.getTtsDetails() == null || ttsSaveDto.getTtsDetails().isEmpty()) {
            LOGGER.warning("유효하지 않은 요청 데이터"); // 잘못된 요청 데이터 로깅
            throw new BusinessException(ErrorCode.INVALID_REQUEST_DATA); // 커스텀 예외 발생
        }

        try {
            // 서비스 계층에서 TTS 변환 로직 실행
            TTSResponseDto ttsResponseDto = ttsService.convertAllTtsDetails(ttsSaveDto);

            // 변환 결과가 비어있으면 실패로 간주하고 예외 처리
            if (ttsResponseDto.getTtsDetails().isEmpty()) {
                LOGGER.warning("TTS 변환 실패"); // 변환 실패 로그
                throw new BusinessException(ErrorCode.TTS_CREATE_FAILED); // 커스텀 예외 발생
            }

            LOGGER.info("TTS 변환 성공"); // 변환 성공 로그
            // 성공적인 응답 데이터 반환
            return DataResponseDto.of(ttsResponseDto);

        } catch (Exception e) {
            // 변환 과정에서 발생한 예외 처리
            LOGGER.log(Level.SEVERE, "TTS 변환 중 예외 발생", e); // 예외 상세 정보 로깅
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR); // 서버 오류 예외 발생
        }
    }
}
