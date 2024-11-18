package com.fourformance.tts_vc_web.common.util;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

@Component
public class ElevenLabsClient_team_api {

    @Value("${elevenlabs.api.url}")
    private String baseUrl;

    @Value("${elevenlabs.api.key}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient();

    public String uploadVoice(String targetAudioPath) throws IOException {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name", "user_custom_voice")
                .addFormDataPart("remove_background_noise", "false");

        File audioFile = new File(targetAudioPath);
        builder.addFormDataPart("files", audioFile.getName(),
                RequestBody.create(audioFile, MediaType.parse("audio/mpeg")));

        Request request = new Request.Builder()
                .url(baseUrl + "/voices/add")
                .addHeader("xi-api-key", apiKey)
                .post(builder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Voice ID creation failed: " + response.body().string());
            }

            String responseBody = response.body().string();
            return extractVoiceId(responseBody);
        }
    }

    public String convertSpeechToSpeech(String voiceId, String audioFilePath) throws IOException {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model_id", "eleven_english_sts_v2")
                .addFormDataPart("remove_background_noise", "false");

        File audioFile = new File(audioFilePath);
        builder.addFormDataPart("audio", audioFile.getName(),
                RequestBody.create(audioFile, MediaType.parse("audio/mpeg")));

        Request request = new Request.Builder()
                .url(baseUrl + "/speech-to-speech/" + voiceId)
                .addHeader("xi-api-key", apiKey)
                .post(builder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Conversion failed: " + response.body().string());
            }

            String fileName = Instant.now().toEpochMilli() + "_converted.mp3";
            Files.write(Paths.get(System.getProperty("user.home") + "/uploads/" + fileName), response.body().bytes());
            return fileName;
        }
    }

    private String extractVoiceId(String responseBody) {
        String prefix = "voice_id\":\"";
        int startIndex = responseBody.indexOf(prefix) + prefix.length();
        int endIndex = responseBody.indexOf("\"", startIndex);
        return responseBody.substring(startIndex, endIndex);
    }
}
