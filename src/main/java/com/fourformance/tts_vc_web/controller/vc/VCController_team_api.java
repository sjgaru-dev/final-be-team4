package com.fourformance.tts_vc_web.controller.vc;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
import com.fourformance.tts_vc_web.dto.response.ResponseDto;
import com.fourformance.tts_vc_web.service.vc.VCService_team_api;
import io.swagger.v3.oas.annotations.Operation;
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
public class VCController_team_api {

    private static final Logger LOGGER = Logger.getLogger(VCController_team_api.class.getName());
    private final VCService_team_api vcService;

    @Operation(summary = "타겟 오디오 업로드 및 Voice ID 생성",
            description = "사용자가 업로드한 타겟 오디오 파일을 사용하여 Voice ID를 생성합니다.")
    @PostMapping(value = "/target/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto uploadTargetAudio(@RequestParam("targetAudio") MultipartFile targetAudio) {
        try {
            String voiceId = vcService.createVoiceId(targetAudio);
            LOGGER.info("Voice ID 생성 완료: voiceId=" + voiceId);
            return DataResponseDto.of(voiceId);
        } catch (IOException e) {
            LOGGER.severe("Voice ID 생성 실패: " + e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "여러 소스 오디오 파일 변환 요청",
            description = "사용자가 업로드한 여러 소스 오디오 파일을 타겟 Voice ID로 변환합니다.")
    @PostMapping(value = "/convert/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto convertMultipleVoices(
            @RequestPart("sourceAudios") MultipartFile[] sourceAudios,
            @RequestParam("voiceId") String voiceId) {
        try {
            LOGGER.info("변환 요청 시작: Voice ID = " + voiceId + ", 소스 오디오 파일 개수 = " + sourceAudios.length);
            List<String> convertedFiles = vcService.convertMultipleVoices(sourceAudios, voiceId);

            // 변환된 파일이 없을 경우 예외 처리
            if (convertedFiles.isEmpty()) {
                LOGGER.warning("변환된 파일이 없습니다.");
                throw new BusinessException(ErrorCode.NOT_EXISTS_AUDIO);
            }

            LOGGER.info("VC 변환 완료: convertedFiles=" + convertedFiles);
            return DataResponseDto.of(convertedFiles);
        } catch (IOException e) {
            LOGGER.severe("VC 변환 실패: " + e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
