package com.fourformance.tts_vc_web.controller.vc;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
import com.fourformance.tts_vc_web.dto.response.ResponseDto;
import com.fourformance.tts_vc_web.service.vc.VCService_team_api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vc")
@Tag(name = "Voice Conversion API", description = "Voice Conversion 관련 기능을 제공합니다.")
public class VCController_team_api {

    private static final Logger LOGGER = Logger.getLogger(VCController_team_api.class.getName());
    private final VCService_team_api vcService;

    /**
     * 타겟 오디오를 업로드하여 Voice ID를 생성하는 API
     *
     * @param targetAudio 사용자가 업로드한 타겟 오디오 파일
     * @param memberId    업로드한 사용자의 ID
     * @return 생성된 Voice ID
     */
    @Operation(
            summary = "타겟 오디오 업로드 및 Voice ID 생성",
            description = "사용자가 업로드한 타겟 오디오 파일을 처리하여 Voice ID를 생성하고, 이를 데이터베이스에 저장합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Voice ID 생성 성공"),
    })
    @PostMapping(value = "/target/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto uploadTargetAudio(
            @RequestParam("targetAudio") MultipartFile targetAudio,
            @RequestParam("memberId") Long memberId) {
        try {
            LOGGER.info("타겟 오디오 업로드 요청 수신: memberId=" + memberId);
            // 서비스 호출을 통해 Voice ID 생성
            String voiceId = vcService.createVoiceId(targetAudio, memberId);
            LOGGER.info("Voice ID 생성 완료: voiceId=" + voiceId);
            // 성공적인 응답 반환
            return DataResponseDto.of(voiceId);
        } catch (IOException e) {
            LOGGER.severe("Voice ID 생성 실패: " + e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 사용자가 업로드한 소스 오디오에 타겟 Voice ID를 입히는 변환 API
     *
     * @param sourceAudios 소스 오디오 파일 배열
     * @param voiceId      타겟 Voice ID
     * @return 변환된 오디오 파일 경로 리스트
     */
    @Operation(
            summary = "소스 오디오 변환",
            description = "여러 소스 오디오 파일을 사용자가 선택한 타겟 Voice ID로 변환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "변환 성공"),
    })
    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto convertMultipleVoices(
            @RequestPart("sourceAudios") MultipartFile[] sourceAudios,
            @RequestParam("voiceId") String voiceId) {
        try {
            LOGGER.info("소스 오디오 변환 요청 수신: voiceId=" + voiceId + ", 소스 파일 개수=" + sourceAudios.length);
            // 서비스 호출을 통해 변환된 파일 리스트 반환
            List<String> convertedFiles = vcService.convertMultipleVoices(sourceAudios, voiceId);
            // 변환된 파일이 없을 경우 예외 처리
            if (convertedFiles.isEmpty()) {
                LOGGER.warning("변환된 파일이 없습니다.");
                throw new BusinessException(ErrorCode.NOT_EXISTS_AUDIO);
            }
            LOGGER.info("소스 오디오 변환 완료: convertedFiles=" + convertedFiles);
            // 성공적인 응답 반환
            return DataResponseDto.of(convertedFiles);
        } catch (IOException e) {
            LOGGER.severe("소스 오디오 변환 실패: " + e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


}
