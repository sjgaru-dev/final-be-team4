package com.fourformance.tts_vc_web.dto.vc;

import com.fourformance.tts_vc_web.domain.entity.MemberAudioMeta;

public class VCDetailDto {
    private Long id;
    private Long projectId;
    private Boolean isChecked;
    private MemberAudioMeta memberAudioMeta;
    private String unitScript;
    private Boolean isDeleted;

}
