package com.fourformance.tts_vc_web.service.concat;

import lombok.RequiredArgsConstructor;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AudioProcessingService {

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    /**
     * 오디오 파일을 로컬로 저장합니다.
     */
    public String saveFileLocally(String audioUrl, String uploadDir, Integer audioSeq) throws IOException {
        String savedFileName = UUID.randomUUID().toString() + "_audioSeq_" + audioSeq + ".mp3";
        File savedFile = new File(uploadDir, savedFileName);
        Files.copy(Paths.get(audioUrl), savedFile.toPath());
        return savedFile.getAbsolutePath();
    }

    /**
     * 침묵 파일을 생성합니다.
     */
    public String createSilenceFile(Long silenceDurationSec, String uploadDir) throws IOException {
        if (silenceDurationSec == null || silenceDurationSec <= 0) {
            return null; // 길이가 0 이하이면 무음 파일 생성하지 않음
        }

        String silenceFileName = "temp_silence_" + UUID.randomUUID() + ".mp3";
        Path silenceFilePath = Paths.get(uploadDir, silenceFileName);

        FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
        FFmpegBuilder silenceBuilder = new FFmpegBuilder()
                .setInput("anullsrc")
                .addExtraArgs("-f", "lavfi")
                .overrideOutputFiles(true)
                .addOutput(silenceFilePath.toString())
                .setAudioCodec("libmp3lame")
                .setAudioChannels(2)
                .setAudioSampleRate(44100)
                .setDuration(silenceDurationSec, TimeUnit.SECONDS)
                .done();

        new FFmpegExecutor(ffmpeg).createJob(silenceBuilder).run();
        return silenceFilePath.toString();
    }


    /**
     * 오디오 파일과 침묵 파일을 병합합니다.
     */
    public String mergeAudioFilesWithSilence(List<String> audioPaths, List<String> silencePaths, String uploadDir) throws IOException {
        String mergedFileName = "merged_" + UUID.randomUUID() + ".mp3";
        Path mergedFilePath = Paths.get(uploadDir, mergedFileName);
        FFmpeg ffmpeg = new FFmpeg(ffmpegPath);

        // FFmpeg 필터 작성
        StringBuilder filterComplexBuilder = new StringBuilder();
        List<String> inputs = new ArrayList<>();
        int inputIndex = 0;

        for (int i = 0; i < audioPaths.size(); i++) {
            inputs.add(audioPaths.get(i));
            filterComplexBuilder.append("[").append(inputIndex++).append(":a]");

            if (i < silencePaths.size() && silencePaths.get(i) != null) {
                inputs.add(silencePaths.get(i));
                filterComplexBuilder.append("[").append(inputIndex++).append(":a]");
            }
        }
        filterComplexBuilder.append("concat=n=").append(inputIndex).append(":v=0:a=1[out]");

        // FFmpeg 빌더 설정
        FFmpegBuilder mergeBuilder = new FFmpegBuilder()
                .overrideOutputFiles(true)
                .addOutput(mergedFilePath.toString())
                .setAudioCodec("libmp3lame")
                .setAudioChannels(2)
                .setAudioBitRate(192000)
                .addExtraArgs("-filter_complex", filterComplexBuilder.toString())
                .addExtraArgs("-map", "[out]")
                .done();

        for (String input : inputs) {
            mergeBuilder.addInput(input);
        }

        new FFmpegExecutor(ffmpeg).createJob(mergeBuilder).run();
        return mergedFilePath.toString();
    }

    /**
     * 로컬 파일을 MultipartFile로 변환합니다.
     */
    public MultipartFile convertToMultipartFile(String filePath) throws IOException {
        File file = new File(filePath);

        return new MultipartFile() {
            @Override
            public String getName() {
                return file.getName();
            }

            @Override
            public String getOriginalFilename() {
                return file.getName();
            }

            @Override
            public String getContentType() {
                try {
                    return Files.probeContentType(file.toPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public boolean isEmpty() {
                return file.length() == 0;
            }

            @Override
            public long getSize() {
                return file.length();
            }

            @Override
            public byte[] getBytes() throws IOException {
                return Files.readAllBytes(file.toPath());
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new FileInputStream(file);
            }

            @Override
            public void transferTo(File dest) throws IOException {
                Files.copy(file.toPath(), dest.toPath());
            }
        };
    }

    /**
     * 임시 파일 삭제
     */
    public void deleteFiles(List<String> filePaths) {
        for (String filePath : filePaths) {
            try {
                Files.deleteIfExists(Paths.get(filePath));
            } catch (IOException e) {
                // 삭제 실패 로그
            }
        }
    }
}
