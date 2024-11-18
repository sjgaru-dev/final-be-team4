package com.fourformance.tts_vc_web.dto.vc;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import com.fourformance.tts_vc_web.domain.entity.MemberAudioMeta;
import com.fourformance.tts_vc_web.domain.entity.MemberAudioVC;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VCSaveDto {

    // VCProject 관련 필드
    private Long projectId;
    private String projectName;
//    private APIStatusConst apiStatus; // 이건 로드 기능 같은데..
    private MemberAudioMeta memberTargetAudioMeta; // trg audio
    // private String trgVoiceId; // 깃에서 풀 받아오면 주석 풀기

    // VCDetail 관련 필드
    List<VCSaveDetailDto> vcDetails;

}
