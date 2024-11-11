package com.fourformance.tts_vc_web.controller.tts;

import com.fourformance.tts_vc_web.service.tts.TTSService_team_api;
import com.google.protobuf.ByteString;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tts")
public class TTSController_team_api {

    private final TTSService_team_api ttsService;

    @Autowired
    public TTSController_team_api(TTSService_team_api ttsService) {
        this.ttsService = ttsService;
    }

    /**
     * 개별 텍스트 변환 API
     * Google TTS API를 사용하여 개별 텍스트를 WAV 파일로 변환합니다.
     */
    @Operation(summary = "Convert Single Text to WAV", description = "Google TTS API를 사용하여 개별 텍스트를 WAV 형식으로 변환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "WAV 파일 변환 성공", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생", content = @Content)
    })
    @PostMapping("/convert/single")
    public ResponseEntity<Map<String, String>> convertSingleText(
            @RequestParam("text") String text,
            @RequestParam("speed") double speed,
            @RequestParam("volume") double volume,
            @RequestParam("pitch") double pitch) {
        try {
            String filePath = ttsService.convertSingleText(text, speed, volume, pitch);
            return ResponseEntity.ok(Map.of("status", "success", "fileUrl", "/api/tts/download?path=" + filePath));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    /**
     * 전체 텍스트 변환 API
     * 여러 텍스트 세그먼트를 한꺼번에 WAV 파일로 변환합니다.
     */
    @Operation(summary = "Convert Batch of Texts to WAV", description = "여러 텍스트 세그먼트를 한꺼번에 WAV 형식으로 변환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "WAV 파일 변환 성공", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생", content = @Content)
    })
    @PostMapping("/convert/batch")
    public ResponseEntity<?> convertBatchTexts(@RequestBody List<Map<String, Object>> texts) {
        try {
            List<Map<String, String>> fileUrls = ttsService.convertAllTexts(texts);
            return ResponseEntity.ok(Map.of("status", "success", "files", fileUrls));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    /**
     * WAV 파일 다운로드 API
     * 변환된 WAV 파일을 다운로드합니다.
     */
    @Operation(summary = "Download Converted WAV File", description = "변환된 WAV 파일을 다운로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "WAV 파일 다운로드 성공", content = @Content),
            @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음", content = @Content)
    })
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("path") String filePath) {
        try {
            Resource resource = ttsService.loadFileAsResource(filePath);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", resource.getFilename());
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(null);
        }
    }
}
