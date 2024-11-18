package com.fourformance.tts_vc_web.service.concat;

import com.fourformance.tts_vc_web.service.tts.TTSService_team_api;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class ConcatService_team_api {

    @Value("${upload.dir}")
    private String uploadDir;

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    private static final Logger LOGGER = Logger.getLogger(ConcatService_team_api.class.getName());

    public String convertMultipleAudios(MultipartFile[] sourceAudios) {
        try {
            // 파일 저장 경로 확인 및 생성
            File uploadFolder = new File(uploadDir.trim()); // 경로 공백 제거
            if (!uploadFolder.exists()) {
                boolean created = uploadFolder.mkdirs();
                if (!created) {
                    throw new RuntimeException("파일 저장 디렉토리를 생성할 수 없습니다: " + uploadDir);
                }
            }

            // 소스 파일 저장
            List<String> savedFilePaths = new ArrayList<>();
            for (MultipartFile audio : sourceAudios) {
                if (!audio.isEmpty()) {
                    String savedFileName = UUID.randomUUID().toString() + "_" + audio.getOriginalFilename();
                    File savedFile = new File(uploadFolder, savedFileName);
                    audio.transferTo(savedFile);
                    savedFilePaths.add(savedFile.getAbsolutePath());
                    LOGGER.info("파일 저장 완료: " + savedFile.getAbsolutePath());
                }
            }

            if (savedFilePaths.isEmpty()) {
                throw new RuntimeException("업로드된 파일이 없습니다.");
            }

            // 병합된 파일 경로
            String mergedFilePath = uploadDir.trim() + "/merged_" + UUID.randomUUID() + ".mp3";

            // FFmpeg로 오디오 병합
            mergeAudioFilesWithSilence(savedFilePaths.toArray(new String[0]), mergedFilePath, 2);

            LOGGER.info("병합된 파일 저장 경로: " + mergedFilePath);
            return mergedFilePath;

        } catch (Exception e) {
            LOGGER.severe("오디오 병합 실패: " + e.getMessage());
            throw new RuntimeException("오디오 병합 중 오류 발생", e);
        }
    }

    private void mergeAudioFilesWithSilence(String[] audioPaths, String outputPath, int silenceDurationSec) throws IOException {
        String silenceFilePath = uploadDir.trim() + "/temp_silence.mp3";

        FFmpeg ffmpeg = new FFmpeg(ffmpegPath);

        // 무음 파일 생성
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

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
        executor.createJob(silenceBuilder).run();

        // concat 필터를 위한 입력 설정
        StringBuilder filterComplexBuilder = new StringBuilder();
        List<String> inputs = new ArrayList<>();

        int inputIndex = 0;
        for (String audioPath : audioPaths) {
            inputs.add(audioPath);
            filterComplexBuilder.append("[").append(inputIndex++).append(":a]");
            if (audioPath != audioPaths[audioPaths.length - 1]) {
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

        executor.createJob(mergeBuilder).run();

        // 임시 무음 파일 삭제
        Files.deleteIfExists(new File(silenceFilePath).toPath());
    }

}
