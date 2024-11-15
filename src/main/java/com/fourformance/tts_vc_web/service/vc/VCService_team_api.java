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
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class VCService_team_api {

    private static final Logger LOGGER = Logger.getLogger(VCService_team_api.class.getName());
    private final ElevenLabsClient_team_api elevenLabsClient;

    @Value("${user.home}/uploads")
    private String uploadDir;

    public String createVoiceId(MultipartFile targetAudio) throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
        String targetFilePath = uploadDir + File.separator + targetAudio.getOriginalFilename();
        File targetFile = new File(targetFilePath);

        LOGGER.info("Saving target audio file to: " + targetFilePath);
        targetAudio.transferTo(targetFile);

        LOGGER.info("Uploading target audio to generate Voice ID");
        return elevenLabsClient.uploadVoice(targetFilePath);
    }

    public List<String> convertMultipleVoices(MultipartFile[] sourceAudios, String voiceId) throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
        List<String> convertedFiles = new ArrayList<>();

        for (MultipartFile sourceAudio : sourceAudios) {
            String sourceFilePath = uploadDir + File.separator + sourceAudio.getOriginalFilename();
            File sourceFile = new File(sourceFilePath);

            LOGGER.info("Saving source audio file to: " + sourceFilePath);
            sourceAudio.transferTo(sourceFile);

            LOGGER.info("Converting source audio file using Voice ID: " + voiceId);
            String convertedFile = elevenLabsClient.convertSpeechToSpeech(voiceId, sourceFilePath);
            convertedFiles.add("/uploads/" + convertedFile);
        }

        return convertedFiles;
    }
}
