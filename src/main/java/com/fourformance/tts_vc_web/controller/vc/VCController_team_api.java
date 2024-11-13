package com.fourformance.tts_vc_web.controller.vc;


import com.fourformance.tts_vc_web.service.vc.VCService_team_api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vc/voice-conversion")
public class VCController_team_api {

    private final VCService_team_api vcService;

    @Operation(
            summary = "소스 오디오와 타겟 오디오를 사용한 음성 변환",
            description = "사용자가 업로드한 소스 오디오와 타겟 오디오 파일을 사용하여 음성 변환을 수행합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "음성 변환 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping(value = "/single", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> convertVoice(
            @Parameter(description = "소스 오디오 파일 (MP3 형식)", required = true)
            @RequestParam("sourceAudio") MultipartFile sourceAudio,
            @Parameter(description = "타겟 오디오 파일 (MP3 형식)", required = true)
            @RequestParam("targetAudio") MultipartFile targetAudio) {
        try {
            // Service 계층에서 변환 로직 호출
            String convertedFilePath = vcService.convertVoice(sourceAudio, targetAudio);
            return ResponseEntity.ok("Converted file available at: " + convertedFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Conversion failed: " + e.getMessage());
        }
    }
}
