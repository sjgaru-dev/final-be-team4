package com.fourformance.tts_vc_web.controller.vc;

import com.fourformance.tts_vc_web.service.vc.ElevenLabsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ElevenLabsController {

    @Autowired
    private ElevenLabsService elevenLabsService;

    @Value("${server.port:8080}")
    private String serverPort;

    @PostMapping(value = "/convertAndDownloadVoice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> convertAndDownloadVoice(
            @RequestParam("sourceFile") MultipartFile sourceFile,
            @RequestParam("targetFile") MultipartFile targetFile) {
        Map<String, String> response = new HashMap<>();
        try {
            // 타겟 음성 파일로 voiceId 생성
            String voiceId = elevenLabsService.generateVoiceId(targetFile);

            // voiceId로 소스 파일 변환
            elevenLabsService.convertAndDownloadVoice(voiceId, sourceFile);

            // 변환된 파일의 다운로드 URL 반환
            String downloadUrl = "http://localhost:" + serverPort + "/download/converted_audio.mp3";
            response.put("downloadUrl", downloadUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Voice conversion failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("output", filename);
            if (!Files.exists(filePath)) {
                return ResponseEntity.status(404).body(null);
            }

            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
