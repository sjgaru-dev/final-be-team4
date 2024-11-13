package com.fourformance.tts_vc_web.dto.tts;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import com.fourformance.tts_vc_web.domain.entity.VoiceStyle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class TTSProjectDto {

    private Long id; // 엔티티 ID
    private String projectName; // 프로젝트 이름
    private String fullScript; // 전체 스크립트
    private Float globalSpeed; // 글로벌 속도
    private Float globalPitch; // 글로벌 피치
    private Float globalVolume; // 글로벌 볼륨
    private APIStatusConst apiStatus; // API 상태
    private VoiceStyle voiceStyle; // 스타일 이름 (optional, lazy load 대신 포함할 수 있는 필드)

    private static ModelMapper modelMapper = new ModelMapper();

    public TTSProject createTTSProject(){
        modelMapper.getConfiguration()
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setFieldMatchingEnabled(true);
        return modelMapper.map(this, TTSProject.class);
    }

    public static TTSProjectDto createTTSProjectDto(TTSProject ttsProject) {
        return modelMapper.map(ttsProject, TTSProjectDto.class);
    }

}