package com.fourformance.tts_vc_web.controller.concat;

import com.fourformance.tts_vc_web.service.concat.ConcatService_temp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

@RestController
@RequestMapping("/audio2")
public class ConcatController_temp {

    @Autowired
    private ConcatService_temp concatServiceTemp;

    private static final Logger logger = LoggerFactory.getLogger(ConcatController_temp.class);

    @PostMapping("/concat")
    public ResponseEntity<Resource> concatAudioFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("silenceDuration") int silenceDuration) {

        // 침묵 시간이 음수인 경우 오류 반환
        if (silenceDuration < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }

        try {
            // 파일 합치기 및 파일 경로 생성
            String concatenatedFilePath = concatServiceTemp.concatAudioFiles(files, silenceDuration);
            Resource resource = new FileSystemResource(concatenatedFilePath);

            if (!resource.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }

            // 다운로드용 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"concatenatedAudio.wav\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resource.contentLength())
                    .body(resource);

        } catch (IOException | UnsupportedAudioFileException e) {
            logger.error("Error occurred during audio concatenation: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
