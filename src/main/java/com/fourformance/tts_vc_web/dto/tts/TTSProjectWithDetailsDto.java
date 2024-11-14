package com.fourformance.tts_vc_web.dto.tts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TTSProjectWithDetailsDto {
    private TTSProjectDto ttsProject;
    private List<TTSDetailDto> ttsDetails;

    public TTSProjectWithDetailsDto(TTSProjectDto ttsProject, List<TTSDetailDto> ttsDetails) {
        this.ttsProject = ttsProject;
        this.ttsDetails = ttsDetails;
    }

}
