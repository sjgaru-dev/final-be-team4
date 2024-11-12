package com.fourformance.tts_vc_web.controller.vc;

import com.fourformance.tts_vc_web.service.vc.VCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/vc")
public class VCController {

    private final VCService vcService;

    @Autowired
    public VCController(VCService vcService) {
        this.vcService = vcService;
    }

    @PostMapping("/convert")
    public ResponseEntity<byte[]> convertVoice(
            @RequestParam("originalVoice") MultipartFile originalVoice,
            @RequestParam("targetVoice") MultipartFile targetVoice) {
        try {
            byte[] audioData = vcService.convertVoice(originalVoice, targetVoice);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "converted_audio.wav");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(audioData);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
