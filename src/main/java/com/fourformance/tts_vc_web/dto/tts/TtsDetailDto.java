package com.fourformance.tts_vc_web.dto.tts;

import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.domain.entity.VoiceStyle;
import com.fourformance.tts_vc_web.dto.member.MemberTestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class TTSDetailDto {

    private Long id; // 상세 정보 ID
    private Long ProjectId; // 프로젝트 ID
    private String unitScript; // 단위 스크립트
    private Float unitSpeed; // 단위 속도
    private Float unitPitch; // 단위 피치
    private Float unitVolume; // 단위 볼륨
    private Boolean isDeleted; // 삭제 여부
    private Integer unitSequence; // 단위 시퀀스
    private VoiceStyle voiceStyle; // 스타일 이름 (optional, lazy load 대신 포함할 수 있는 필드)

    private static ModelMapper modelMapper = new ModelMapper();

    public TTSDetail createTTSDetail(){
        modelMapper.getConfiguration()
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setFieldMatchingEnabled(true);
        return modelMapper.map(this, TTSDetail.class);
    }

    public static TTSDetailDto createTTSDetailDto(TTSDetail ttsDetail) {
        return modelMapper.map(ttsDetail, TTSDetailDto.class);
    }


}