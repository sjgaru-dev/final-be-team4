package com.fourformance.tts_vc_web.service.concat;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ConcatService {

    public Resource concatAudioFiles(MultipartFile audioFile1, MultipartFile audioFile2) {
        Path mergedFilePath = Paths.get("merged_audio.mp3"); // 병합된 파일의 경로 설정

        try {
            // 멀티파트 파일을 임시 파일로 변환하여 FFmpeg로 병합
            File file1 = convertToFile(audioFile1);
            File file2 = convertToFile(audioFile2);

            FFmpeg.atPath()
                    .addInput(UrlInput.fromPath(file1.toPath()))
                    .addInput(UrlInput.fromPath(file2.toPath()))
                    .addOutput(UrlOutput.toPath(mergedFilePath))
                    .execute();

            return new UrlResource(mergedFilePath.toUri());
        } catch (Exception e) {
            throw new RuntimeException("오디오 파일 병합에 실패했습니다.", e);
        }
    }

    private File convertToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        file.transferTo(convFile);
        return convFile;
    }
}
