package com.fourformance.tts_vc_web.dto.tts;

import java.util.List;

public class TTSProjectWithDetailsDto {
    private TTSProjectDto ttsProject;
    private List<TTSDetailDto> ttsDetails;

    public TTSProjectWithDetailsDto(TTSProjectDto ttsProject, List<TTSDetailDto> ttsDetails) {
        this.ttsProject = ttsProject;
        this.ttsDetails = ttsDetails;
    }

    public TTSProjectDto getTtsProject() {
        return ttsProject;
    }

    public void setTtsProject(TTSProjectDto ttsProject) {
        this.ttsProject = ttsProject;
    }

    public List<TTSDetailDto> getTtsDetails() {
        return ttsDetails;
    }

    public void setTtsDetails(List<TTSDetailDto> ttsDetails) {
        this.ttsDetails = ttsDetails;
    }
}
