package com.fourformance.tts_vc_web.service.concat;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class ConcatService_team_api {

    private static final Logger LOGGER = Logger.getLogger(ConcatService_team_api.class.getName());
    private String uploadDir;

    @PostConstruct
    public void initialize() {
        // 업로드 디렉토리 설정
        uploadDir = System.getProperty("user.home") + "/uploads";
        File uploadFolder = new File(uploadDir);

        if (!uploadFolder.exists()) {
            if (uploadFolder.mkdirs()) {
                LOGGER.info("업로드 디렉토리가 생성되었습니다: " + uploadDir);
            } else {
                throw new RuntimeException("업로드 디렉토리를 생성할 수 없습니다: " + uploadDir);
            }
        }
    }

    public String convertMultipleAudios(MultipartFile[] sourceAudios) {
        List<String> savedFilePaths = new ArrayList<>();
        String silenceFilePath = null;

        try {
            // 파일 저장
            for (MultipartFile audio : sourceAudios) {
                if (!audio.isEmpty()) {
                    String savedFileName = UUID.randomUUID().toString() + "_" + audio.getOriginalFilename();
                    File savedFile = new File(uploadDir, savedFileName);
                    audio.transferTo(savedFile);
                    savedFilePaths.add(savedFile.getAbsolutePath());
                    LOGGER.info("파일 저장 완료: " + savedFile.getAbsolutePath());
                }
            }

            if (savedFilePaths.isEmpty()) {
                throw new RuntimeException("업로드된 파일이 없습니다.");
            }

            // 병합된 파일 저장 경로
            String mergedFilePath = uploadDir + "/merged_" + UUID.randomUUID() + ".mp3";

            // FFmpeg로 오디오 병합
            silenceFilePath = createSilenceFile(2); // 2초 무음 파일 생성
            mergeAudioFilesWithSilence(savedFilePaths, mergedFilePath, silenceFilePath);

            LOGGER.info("병합된 파일 저장 경로: " + mergedFilePath);

            return mergedFilePath;

        } catch (Exception e) {
            LOGGER.severe("오디오 병합 실패: " + e.getMessage());
            throw new RuntimeException("오디오 병합 중 오류 발생", e);

        } finally {
            // 파일 정리
            deleteFiles(savedFilePaths);
            if (silenceFilePath != null) {
                deleteFiles(List.of(silenceFilePath));
            }
        }
    }

    private String createSilenceFile(int silenceDurationSec) throws IOException {
        String silenceFilePath = uploadDir + "/temp_silence.mp3";

        FFmpeg ffmpeg = new FFmpeg("/opt/homebrew/bin/ffmpeg"); // FFmpeg 경로 설정
        FFmpegBuilder silenceBuilder = new FFmpegBuilder()
                .setInput("anullsrc")
                .addExtraArgs("-f", "lavfi")
                .overrideOutputFiles(true)
                .addOutput(silenceFilePath)
                .setAudioCodec("libmp3lame")
                .setAudioChannels(2)
                .setAudioSampleRate(44100)
                .setDuration(silenceDurationSec, TimeUnit.SECONDS)
                .done();

        new FFmpegExecutor(ffmpeg).createJob(silenceBuilder).run();

        LOGGER.info("무음 파일 생성 완료: " + silenceFilePath);
        return silenceFilePath;
    }

    private void mergeAudioFilesWithSilence(List<String> audioPaths, String outputPath, String silenceFilePath) throws IOException {
        FFmpeg ffmpeg = new FFmpeg("/opt/homebrew/bin/ffmpeg");

        // concat 필터를 위한 입력 설정
        StringBuilder filterComplexBuilder = new StringBuilder();
        List<String> inputs = new ArrayList<>();

        int inputIndex = 0;
        for (String audioPath : audioPaths) {
            inputs.add(audioPath);
            filterComplexBuilder.append("[").append(inputIndex++).append(":a]");
            if (!audioPath.equals(audioPaths.get(audioPaths.size() - 1))) {
                inputs.add(silenceFilePath);
                filterComplexBuilder.append("[").append(inputIndex++).append(":a]");
            }
        }
        filterComplexBuilder.append("concat=n=").append(inputIndex).append(":v=0:a=1[out]");

        FFmpegBuilder mergeBuilder = new FFmpegBuilder()
                .overrideOutputFiles(true)
                .addOutput(outputPath)
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
        LOGGER.info("오디오 병합 완료: " + outputPath);
    }

    private void deleteFiles(List<String> filePaths) {
        for (String filePath : filePaths) {
            try {
                Files.deleteIfExists(Paths.get(filePath));
                LOGGER.info("삭제된 파일: " + filePath);
            } catch (IOException e) {
                LOGGER.warning("파일 삭제 실패: " + filePath + " - " + e.getMessage());
            }
        }
    }
}
