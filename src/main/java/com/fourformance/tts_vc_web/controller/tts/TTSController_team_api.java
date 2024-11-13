package com.fourformance.tts_vc_web.controller.tts;

import com.fourformance.tts_vc_web.service.tts.TTSService_team_api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tts")
public class TTSController_team_api {

    private final TTSService_team_api ttsService;

    @Autowired
    public TTSController_team_api(TTSService_team_api ttsService) {
        this.ttsService = ttsService;
    }

    /**
     * 개별 텍스트 변환 API
     * Google TTS API를 사용하여 개별 텍스트를 WAV 파일로 변환합니다.
     *
     * 매개변수:
     * - text: 변환할 텍스트 (예: "안녕하세요")
     * - languageCode: 언어 코드 (예: "ko-KR", "en-US")
     * - gender: 성별 ("male", "female", "neutral")
     * - speed: 말하는 속도 (범위: 0.25 ~ 4.0, 기본값: 1.0)
     * - volume: 볼륨 조정 (범위: -96.0 ~ 16.0 데시벨, 기본값: 0.0)
     * - pitch: 음의 높낮이 (범위: -20.0 ~ 20.0, 기본값: 0.0)
     */
    @Operation(summary = "Convert Single Text to WAV", description = "Google TTS API를 사용하여 개별 텍스트를 WAV 형식으로 변환합니다.\n\n" +
            "매개변수:\n" +
            "- text: 변환할 텍스트 (예: '안녕하세요')\n" +
            "- languageCode: 언어 코드 (예: 'ko-KR', 'en-US')\n" +
            "- gender: 성별 ('male', 'female', 'neutral')\n" +
            "- speed: 말하는 속도 (범위: 0.25 ~ 4.0, 기본값: 1.0)\n" +
            "- volume: 볼륨 조정 (범위: -96.0 ~ 16.0 데시벨, 기본값: 0.0)\n" +
            "- pitch: 음의 높낮이 (범위: -20.0 ~ 20.0, 기본값: 0.0)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "WAV 파일 변환 성공"),
            @ApiResponse(responseCode = "400", description = "언어 불일치 오류"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @PostMapping("/convert/single")
    public ResponseEntity<Map<String, String>> convertSingleText(
            @RequestParam("text") String text,
            @RequestParam("languageCode") String languageCode,
            @RequestParam("gender") String gender,
            @RequestParam("speed") double speed,
            @RequestParam("volume") double volume,
            @RequestParam("pitch") double pitch) {
        try {
            // TTS 변환 후 파일명 반환 (파일 경로가 아닌 파일명만 반환)
            String fileName = ttsService.convertSingleText(text, languageCode, gender, speed, volume, pitch);

            // 전체 URL 생성 (output/ 경로는 한 번만 추가)
            String fullFileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/output/")
                    .path(fileName)
                    .toUriString();

            return ResponseEntity.ok(Map.of("fileUrl", fullFileUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }




    @PostMapping("/convert/single2")
    public ResponseEntity<Map<String, String>> convertSingleText2(
            @RequestParam("id") Long id,
            @RequestParam("languageCode") String languageCode,
            @RequestParam("gender") String gender) {
        try {
            String filePath = ttsService.convertSingleText(id, languageCode, gender);

            // 전체 URL 생성
            String fullFileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/converted/download")
                    .queryParam("path", filePath)
                    .toUriString();

            return ResponseEntity.ok(Map.of("fileUrl", fullFileUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 전체 텍스트 변환 API
     * 여러 텍스트 세그먼트를 한꺼번에 WAV 파일로 변환합니다.
     *
     * 매개변수:
     * - text: 변환할 텍스트 세그먼트 리스트 (JSON 형식)
     *   - text: 변환할 텍스트 (예: "Hello")
     *   - languageCode: 언어 코드 (예: "en-US")
     *   - gender: 성별 ("male", "female", "neutral")
     *   - speed: 말하는 속도 (범위: 0.25 ~ 4.0, 기본값: 1.0)
     *   - volume: 볼륨 조정 (범위: -96.0 ~ 16.0 데시벨, 기본값: 0.0)
     *   - pitch: 음의 높낮이 (범위: -20.0 ~ 20.0, 기본값: 0.0)
     */
    @Operation(summary = "Convert Batch of Texts to WAV", description = "여러 텍스트 세그먼트를 한꺼번에 WAV 형식으로 변환합니다.\n\n" +
            "매개변수:\n" +
            "- text: 변환할 텍스트 세그먼트 리스트 (JSON 형식)\n" +
            "  - text: 변환할 텍스트 (예: 'Hello')\n" +
            "  - languageCode: 언어 코드 (예: 'en-US')\n" +
            "  - gender: 성별 ('male', 'female', 'neutral')\n" +
            "  - speed: 말하는 속도 (범위: 0.25 ~ 4.0, 기본값: 1.0)\n" +
            "  - volume: 볼륨 조정 (범위: -96.0 ~ 16.0 데시벨, 기본값: 0.0)\n" +
            "  - pitch: 음의 높낮이 (범위: -20.0 ~ 20.0, 기본값: 0.0)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "WAV 파일 변환 성공"),
            @ApiResponse(responseCode = "400", description = "언어 불일치 오류"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @PostMapping("/convert/batch")
    public ResponseEntity<?> convertBatchTexts(@RequestBody List<Map<String, Object>> texts) {
        try {
            List<Map<String, String>> fileUrls = ttsService.convertAllTexts(texts);

            // 전체 URL 리스트 생성
            List<Map<String, String>> fullFileUrls = fileUrls.stream().map(fileUrlMap -> {
                String filePath = fileUrlMap.get("fileUrl");
                String fullFileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/converted/download")
                        .queryParam("path", filePath)
                        .toUriString();
                return Map.of("fileUrl", fullFileUrl);
            }).toList();

            return ResponseEntity.ok(Map.of("status", "success", "files", fullFileUrls));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 변환된 WAV 파일 다운로드 API
     */
    @Operation(summary = "Download Converted WAV File", description = "변환된 WAV 파일을 다운로드합니다.\n\n" +
            "매개변수:\n" +
            "- path: 다운로드할 WAV 파일의 경로 (예: 'output/tts_output_123456.wav')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "WAV 파일 다운로드 성공"),
            @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음")
    })
    @GetMapping("/converted/download")
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
