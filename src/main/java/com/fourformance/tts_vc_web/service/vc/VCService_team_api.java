package com.fourformance.tts_vc_web.service.vc;

import com.fourformance.tts_vc_web.common.util.ElevenLabsClient_team_api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VCService_team_api {

    private final ElevenLabsClient_team_api elevenLabsClient;

    @Value("${user.home}/uploads")
    private String uploadDir;

    // 타겟 파일 업로드 및 Voice ID 생성
    public String createVoiceId(MultipartFile targetAudio) throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
        String targetFilePath = uploadDir + File.separator + targetAudio.getOriginalFilename();
        File targetFile = new File(targetFilePath);

        // 파일 저장
        targetAudio.transferTo(targetFile);

        // Voice ID 생성
        return elevenLabsClient.uploadVoice(targetFilePath);
    }

    // 여러 소스 파일 변환
    public List<String> convertMultipleVoices(MultipartFile[] sourceAudios, String voiceId) throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
        List<String> convertedFiles = new ArrayList<>();

        for (MultipartFile sourceAudio : sourceAudios) {
            String sourceFilePath = uploadDir + File.separator + sourceAudio.getOriginalFilename();
            File sourceFile = new File(sourceFilePath);

            // 소스 파일 저장
            sourceAudio.transferTo(sourceFile);

            // 변환 작업 수행
            String convertedFile = elevenLabsClient.convertSpeechToSpeech(voiceId, sourceFilePath);
            convertedFiles.add("/uploads/" + convertedFile);
        }

        return convertedFiles;
    }
}
