package com.fourformance.tts_vc_web.service;


import com.fourformance.tts_vc_web.controller.vc.ElevenLabsController;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class VCService_team_api {

    private final ElevenLabsController elevenLabsClient;

    @Value("${user.home}/uploads")
    private String uploadDir;

    public String convertVoice(MultipartFile sourceAudio, MultipartFile targetAudio) throws IOException {
        // 업로드 디렉토리 생성 (존재하지 않는 경우)
        Files.createDirectories(Paths.get(uploadDir));

        // 소스 및 타겟 오디오 파일 저장 경로 설정
        String sourceFilePath = uploadDir + File.separator + sourceAudio.getOriginalFilename();
        String targetFilePath = uploadDir + File.separator + targetAudio.getOriginalFilename();

        File sourceFile = new File(sourceFilePath);
        File targetFile = new File(targetFilePath);

        // 파일 저장
        sourceAudio.transferTo(sourceFile);
        targetAudio.transferTo(targetFile);

        // 타겟 오디오에서 Voice ID 생성
        String voiceId = elevenLabsClient.uploadVoice(targetFilePath);

        // 생성된 Voice ID를 사용하여 소스 오디오 변환
        String convertedFile = elevenLabsClient.convertSpeechToSpeech(voiceId, sourceFilePath);

        // 변환된 파일 경로 반환
        return "/uploads/" + convertedFile;
    }
}
