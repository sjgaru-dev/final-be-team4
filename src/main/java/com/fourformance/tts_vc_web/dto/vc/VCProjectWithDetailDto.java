package com.fourformance.tts_vc_web.dto.vc;

import com.fourformance.tts_vc_web.dto.tts.TTSDetailDto;
import com.fourformance.tts_vc_web.dto.tts.TTSProjectDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VCProjectWithDetailDto {
    private VCProjectDto vcProject;
    private List<VCDetailDto> vcDetails;

    public VCProjectWithDetailDto(VCProjectDto vcProject, List<VCDetailDto> vcDetails) {
        this.vcProject = vcProject;
        this.vcDetails = vcDetails;
    }
}
