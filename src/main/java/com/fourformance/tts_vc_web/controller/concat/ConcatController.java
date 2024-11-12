package com.fourformance.tts_vc_web.controller.concat;

import com.fourformance.tts_vc_web.service.concat.ConcatService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/audio")
public class ConcatController {

    private final ConcatService concatService;

    public ConcatController(ConcatService concatService) {
        this.concatService = concatService;
    }

    @PostMapping("/concat")
    public ResponseEntity<Resource> concatAudio(
            @RequestParam("audioFile1") MultipartFile audioFile1,
            @RequestParam("audioFile2") MultipartFile audioFile2) {

        Resource mergedAudio = concatService.concatAudioFiles(audioFile1, audioFile2);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"merged_audio.mp3\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(mergedAudio);
    }
}
