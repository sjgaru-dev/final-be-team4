package com.fourformance.tts_vc_web.controller.tts;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
import com.fourformance.tts_vc_web.dto.response.ResponseDto;
import com.fourformance.tts_vc_web.dto.tts.TTSSaveDto;
import com.fourformance.tts_vc_web.service.tts.TTSService_team_api3;
import io.swagger.v3.oas.annotations.Operation; // Swagger Operation 어노테이션
import io.swagger.v3.oas.annotations.tags.Tag; // Swagger Tag 어노테이션
import io.swagger.v3.oas.annotations.responses.ApiResponse; // Swagger ApiResponse 어노테이션
import io.swagger.v3.oas.annotations.responses.ApiResponses; // Swagger ApiResponses 어노테이션
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TTS API 컨트롤러
 * 이 컨트롤러는 텍스트를 오디오 파일로 변환하는 기능을 제공합니다.
 */
@Tag(name = "tts-controller-_team-_api", description = "텍스트를 오디오 파일로 변환하는 API") // Tag 어노테이션
@RestController
@RequestMapping("/tts")
public class TTSController_team_api3 {

    private static final Logger LOGGER = Logger.getLogger(TTSController_team_api3.class.getName());
    private final TTSService_team_api3 ttsService;

    public TTSController_team_api3(TTSService_team_api3 ttsService) {
        this.ttsService = ttsService;
    }

    /**
     * TTS 배치 변환 API
     * 주어진 텍스트 목록을 Google TTS API를 사용하여 음성 파일로 변환합니다.
     *
     * @param ttsSaveDto 변환 요청 데이터
     * @return 변환된 음성 파일의 URL 목록
     */
    @Operation(summary = "TTS 배치 변환", description = "주어진 텍스트 목록을 오디오 파일로 변환합니다.") // Operation 어노테이션
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 TTS 변환 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/convert/batch3")
    public ResponseDto convertBatchTexts(@RequestBody TTSSaveDto ttsSaveDto) {
        LOGGER.info("컨트롤러 메서드 호출됨: " + ttsSaveDto);

        // 유효성 검증
        if (ttsSaveDto == null || ttsSaveDto.getTtsDetails() == null || ttsSaveDto.getTtsDetails().isEmpty()) {
            LOGGER.warning("유효하지 않은 요청 데이터");
            throw new BusinessException(ErrorCode.INVALID_REQUEST_DATA);
        }

        try {
            // 새로운 데이터 처리
            List<Map<String, String>> newFileUrls = ttsService.convertAllTtsDetails(ttsSaveDto);

            if (newFileUrls.isEmpty()) {
                LOGGER.warning("TTS 변환 실패");
                throw new BusinessException(ErrorCode.TTS_CREATE_FAILED);
            }

            LOGGER.info("TTS 변환 성공");
            // 성공적인 응답 반환
            return DataResponseDto.of(newFileUrls);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "TTS 변환 중 예외 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

}
