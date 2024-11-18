package com.fourformance.tts_vc_web.controller.tts;

import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
import com.fourformance.tts_vc_web.dto.tts.TTSRequestDto;
import com.fourformance.tts_vc_web.service.tts.TTSService_team_api2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/tts")
public class TTSController_team_api2 {

    private static final Logger LOGGER = Logger.getLogger(TTSController_team_api2.class.getName());

    private final TTSService_team_api2 ttsService;

    public TTSController_team_api2(TTSService_team_api2 ttsService) {
        this.ttsService = ttsService;
    }

    @PostMapping("/convert/batch2")
    public DataResponseDto convertBatchTexts(@RequestBody TTSRequestDto ttsRequestDto) throws Exception {
        // 새로운 데이터 처리
        List<Map<String, String>> newFileUrls = ttsService.convertAllTtsDetails(ttsRequestDto);

        if (newFileUrls.isEmpty()) {
            throw new IllegalArgumentException("No valid TTS Details provided for conversion.");
        }

        // 성공적인 응답 반환
        return DataResponseDto.of(newFileUrls);
    }
}
