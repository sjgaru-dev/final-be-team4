package com.fourformance.tts_vc_web.dto.vc;

import com.fourformance.tts_vc_web.common.constant.AudioFormat;
import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.domain.entity.MemberAudioMeta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberAudioMetaDto {

    private Long id; // 오디오 메타 데이터 ID
    private Long memberId; // 멤버 ID
    private String audioUrl; // 오디오 URL
    private String script; // 스크립트
    private AudioType audioType; // 오디오 타입
    private Boolean isDeleted; // 삭제 여부
    private AudioFormat audioFormat; // 오디오 포맷


    private static ModelMapper modelMapper = new ModelMapper();

    // MemberAudioMetaDto -> MemberAudioMeta 매핑 메서드
    public MemberAudioMeta createMemberAudioMeta() {
        modelMapper.getConfiguration()
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setFieldMatchingEnabled(true);
        return modelMapper.map(this, MemberAudioMeta.class);
    }

    // MemberAudioMeta -> MemberAudioMetaDto 매핑 메서드
    public static MemberAudioMetaDto createMemberAudioMetaDto(MemberAudioMeta memberAudioMeta) {
        return modelMapper.map(memberAudioMeta, MemberAudioMetaDto.class);
    }
}