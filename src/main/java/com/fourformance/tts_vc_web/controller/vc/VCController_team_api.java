package com.fourformance.tts_vc_web.controller.vc;


import com.fourformance.tts_vc_web.service.vc.VCService_team_api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vc")
public class VCController_team_api {

    private final VCService_team_api vcService;

    @Operation(summary = "타겟 오디오 업로드 및 Voice ID 생성")
    @PostMapping(value = "/target/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadTargetAudio(@RequestParam("targetAudio") MultipartFile targetAudio) {
        try {
            String voiceId = vcService.createVoiceId(targetAudio);
            return ResponseEntity.ok("Voice ID: " + voiceId);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create Voice ID: " + e.getMessage());
        }
    }

    @Operation(
            summary = "여러 소스 오디오 파일 변환 요청",
            description = "MP3 형식의 여러 소스 오디오 파일을 업로드하고, 동일한 Voice ID로 변환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변환 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping(value = "/convert/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> convertMultipleVoices(
            @Parameter(description = "업로드할 소스 오디오 파일들 (MP3 형식)", required = true)
            @RequestPart("sourceAudios") MultipartFile[] sourceAudios,
            @Parameter(description = "Voice ID (타겟 오디오에서 생성된 ID)", required = true)
            @RequestParam("voiceId") String voiceId) {
        try {
            List<String> convertedFiles = vcService.convertMultipleVoices(sourceAudios, voiceId);
            return ResponseEntity.ok(convertedFiles);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Conversion failed: " + e.getMessage());
        }
    }
}
