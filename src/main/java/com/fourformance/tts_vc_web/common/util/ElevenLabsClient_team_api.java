package com.fourformance.tts_vc_web.client;

import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

@Component
public class ElevenLabsClient_team_api {

    private static final String BASE_URL = "https://api.elevenlabs.io/v1";
    private final String apiKey = "your-api-key"; // 여기에 실제 API 키를 입력하세요.
    private final OkHttpClient client = new OkHttpClient();

    /**
     * 타겟 오디오 파일을 업로드하여 새로운 Voice ID 생성
     * @param targetAudioPath 타겟 오디오 파일 경로
     * @return 생성된 Voice ID
     * @throws IOException 파일 처리 중 예외
     */
    public String uploadVoice(String targetAudioPath) throws IOException {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name", "user_custom_voice")
                .addFormDataPart("remove_background_noise", "false");

        File audioFile = new File(targetAudioPath);
        builder.addFormDataPart("files", audioFile.getName(),
                RequestBody.create(audioFile, MediaType.parse("audio/mpeg")));

        Request request = new Request.Builder()
                .url(BASE_URL + "/voices/add")
                .addHeader("xi-api-key", apiKey)
                .post(builder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to create voice ID: " + response.body().string());
            }

            String responseBody = response.body().string();
            return extractVoiceId(responseBody);
        }
    }

    /**
     * 생성된 Voice ID를 사용하여 소스 오디오를 변환
     * @param voiceId 변환에 사용할 Voice ID
     * @param audioFilePath 소스 오디오 파일 경로
     * @return 변환된 오디오 파일 경로
     * @throws IOException 파일 처리 중 예외
     */
    public String convertSpeechToSpeech(String voiceId, String audioFilePath) throws IOException {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model_id", "eleven_english_sts_v2")
                .addFormDataPart("remove_background_noise", "false");

        File audioFile = new File(audioFilePath);
        builder.addFormDataPart("audio", audioFile.getName(),
                RequestBody.create(audioFile, MediaType.parse("audio/mpeg")));

        Request request = new Request.Builder()
                .url(BASE_URL + "/speech-to-speech/" + voiceId)
                .addHeader("xi-api-key", apiKey)
                .post(builder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Conversion failed: " + response.body().string());
            }

            String fileName = Instant.now().toEpochMilli() + "_converted.mp3";
            Files.write(Paths.get("src/main/resources/static/" + fileName), response.body().bytes());
            return fileName;
        }
    }

    /**
     * API 응답에서 Voice ID 추출
     * @param responseBody API 응답 본문
     * @return 추출된 Voice ID
     */
    private String extractVoiceId(String responseBody) {
        // JSON 파싱 없이 간단한 문자열 추출 (간단한 예시)
        String prefix = "voice_id\":\"";
        int startIndex = responseBody.indexOf(prefix) + prefix.length();
        int endIndex = responseBody.indexOf("\"", startIndex);
        return responseBody.substring(startIndex, endIndex);
    }
}
