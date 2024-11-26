package com.fourformance.tts_vc_web.dto.vc;

import com.fourformance.tts_vc_web.dto.common.GeneratedAudioDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VCDetailLoadDto {

    private Long id; // 상세 정보 ID
    private Long projectId; // 프로젝트 ID
    private Boolean isChecked; // 체크 여부
    private String unitScript; // 단위 스크립트
    private String srcAudio; // source 오디오 url
    private List<GeneratedAudioDto> genAudios; // VC 변환된 오디오 url
}