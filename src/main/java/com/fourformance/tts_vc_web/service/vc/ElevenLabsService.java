package com.fourformance.tts_vc_web.service.vc;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ElevenLabsService {

    private static final String API_URL_VOICE_ID = "https://api.elevenlabs.io/v1/voice-id";
    private static final String API_URL_CONVERT = "https://api.elevenlabs.io/v1/speech-to-speech";
    private static final String API_KEY = "sk_40dde343a836275e2ce55fc046313220e0e71ca4b24c7843"; // 자신의 API 키를 입력하세요
    private static final String AUDIO_SAVE_PATH = "output"; // 저장 경로를 output 폴더로 설정

    public String generateVoiceId(MultipartFile targetFile) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        // 타겟 파일을 바이트 배열로 변환
        byte[] targetBytes = targetFile.getBytes();

        // JSON 객체 생성
        JSONObject json = new JSONObject();
        json.put("audio", targetBytes);

        // 요청 생성
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL_VOICE_ID))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        // 응답 받기
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject responseBody = new JSONObject(response.body());
            return responseBody.getString("voice_id");
        } else {
            throw new RuntimeException("Voice ID 생성 실패: " + response.statusCode());
        }
    }

    public String convertAndDownloadVoice(String voiceId, MultipartFile sourceFile) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        // 소스 파일을 바이트 배열로 변환
        byte[] sourceBytes = sourceFile.getBytes();

        // JSON 객체 생성
        JSONObject json = new JSONObject();
        json.put("voice_id", voiceId);
        json.put("audio", sourceBytes);

        // 요청 생성
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL_CONVERT + "/" + voiceId))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        // 응답 받기
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject responseBody = new JSONObject(response.body());
            String audioUrl = responseBody.getString("audio_url");

            // 변환된 음원 다운로드
            downloadAudio(audioUrl, "converted_audio.mp3");
            return "Voice Conversion Successful. File downloaded as 'output/converted_audio.mp3'.";
        } else {
            throw new RuntimeException("Voice Conversion 실패: " + response.statusCode());
        }
    }

    private void downloadAudio(String audioUrl, String fileName) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(audioUrl)).build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() == 200) {
            Path filePath = Paths.get(AUDIO_SAVE_PATH, fileName);
            Files.createDirectories(filePath.getParent()); // output 폴더 생성
            Files.write(filePath, response.body());
        } else {
            throw new RuntimeException("Audio 다운로드 실패: " + response.statusCode());
        }
    }
}
