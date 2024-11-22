package com.fourformance.tts_vc_web.dto.concat;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CNCTProjectWithDetailDto {

    private CNCTProjectDto cnctProjectDto;
    private List<CNCTDetailDto> cnctDetailDtos;

    public CNCTProjectWithDetailDto(CNCTProjectDto cnctProjectDto, List<CNCTDetailDto> cnctDetailDtos) {
        this.cnctProjectDto = cnctProjectDto;
        this.cnctDetailDtos = cnctDetailDtos;
    }

}
