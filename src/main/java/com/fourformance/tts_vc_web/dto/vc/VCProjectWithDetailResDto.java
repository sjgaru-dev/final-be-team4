package com.fourformance.tts_vc_web.dto.vc;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VCProjectWithDetailResDto {
    private VCProjectResDto vcProjectRes;
    private List<VCDetailLoadDto> vcDetailsRes;

    public VCProjectWithDetailResDto(VCProjectResDto vcProject, List<VCDetailLoadDto> vcDetails) {
        this.vcProjectRes = vcProject;
        this.vcDetailsRes = vcDetails;
    }
}
