package com.fourformance.tts_vc_web.dto.vc;

import com.fourformance.tts_vc_web.domain.entity.MemberAudioVC;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VCSaveDetailDto {
    private Long id; // 상세 정보 ID
    private Long projectId; // 프로젝트 ID
    private MemberAudioVC memberSourceAudioVC; // src audio
    private String unitScript;
    private Boolean isChecked;
    private Boolean isDeleted;
}
