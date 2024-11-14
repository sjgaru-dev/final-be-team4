package com.fourformance.tts_vc_web.dto.vc;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import com.fourformance.tts_vc_web.domain.entity.MemberAudioVC;

public class VCProjectDto {
    private Long id;
    private MemberAudioVC memberAudioVc; //에서 오디오 타입이 VC_SRC, VC_TRG 두개만 필요
    private APIStatusConst apiStatus;

}
