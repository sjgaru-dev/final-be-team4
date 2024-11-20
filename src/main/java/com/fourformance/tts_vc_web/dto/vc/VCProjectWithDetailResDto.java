package com.fourformance.tts_vc_web.dto.vc;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VCProjectWithDetailResDto {
    private VCProjectResDto vcProjectRes;
    private List<VCDetailResDto> vcDetailsRes;

    public VCProjectWithDetailResDto(VCProjectResDto vcProject, List<VCDetailResDto> vcDetails) {
        this.vcProjectRes = vcProject;
        this.vcDetailsRes = vcDetails;
    }
}
