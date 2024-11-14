package com.fourformance.tts_vc_web.dto.vc;

import java.util.List;

public class VCProjectWithDetailDto {
    private VCProjectDto vcProject;
    private List<VCDetailDto> vcDetails;

    public VCProjectWithDetailDto(VCProjectDto vcProject, List<VCDetailDto> vcDetails){
        this.vcProject = vcProject;
        this.vcDetails = vcDetails;
    }
}
