package com.fourformance.tts_vc_web.dto.tts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class TtsStatusSaveResponseDto {
    private String message;
    private Long projectId;
}
