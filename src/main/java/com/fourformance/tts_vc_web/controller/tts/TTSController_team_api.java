package com.fourformance.tts_vc_web.controller.tts;


import com.fourformance.tts_vc_web.service.tts.TTSService_team_api;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/tts")
public class TTSController_team_api {

    private static final String FILE_ENCODING = StandardCharsets.UTF_8.name();

    @Autowired
    private TTSService_team_api ttsService;

    // 업로드 페이지 렌더링 (웹 페이지)
    @GetMapping("/upload")
    public String showUploadPage() {
        return "upload"; // upload.html 템플릿 반환
    }

    // 파일 업로드 및 변환 처리
    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            // 업로드된 파일을 UTF-8 인코딩으로 읽어 텍스트로 변환
            String text = new String(file.getBytes(), FILE_ENCODING);

            // TTS 서비스 호출을 통해 텍스트를 음성 파일(MP3)로 변환
            ByteString audioContents = ttsService.convertTextToSpeech(text);

            // HTTP 응답 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "output.mp3");

            // 성공적인 응답 반환
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(audioContents.toByteArray());

        } catch (IOException e) {
            // 파일 처리 중 발생한 예외를 처리
            return ResponseEntity.badRequest()
                    .body("File processing error: " + e.getMessage());
        } catch (Exception e) {
            // 텍스트를 음성으로 변환하는 과정에서 발생한 예외를 처리
            return ResponseEntity.status(500)
                    .body("Text-to-Speech conversion error: " + e.getMessage());
        }
    }
}