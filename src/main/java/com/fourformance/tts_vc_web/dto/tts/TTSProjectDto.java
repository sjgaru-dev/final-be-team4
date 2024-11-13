package com.fourformance.tts_vc_web.dto.tts;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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
    private LocalDateTime apiStatusModifiedAt; // API 상태 수정 시간
    //private String styleName; // 스타일 이름 (optional, lazy load 대신 포함할 수 있는 필드)

    // TTSProject 엔티티를 TTSProjectDTO로 변환하는 생성자
    public static TTSProjectDto fromEntity(TTSProject ttsProject) {
        //String styleName = ttsProject.getStyle() != null ? ttsProject.getStyle().getName() : null; // 스타일 이름을 포함
        return new TTSProjectDto(
                ttsProject.getId(),
                ttsProject.getProjectName(),
                ttsProject.getFullScript(),
                ttsProject.getGlobalSpeed(),
                ttsProject.getGlobalPitch(),
                ttsProject.getGlobalVolume(),
                ttsProject.getApiStatus(),
                ttsProject.getAPIStatusModifiedAt()
        );
    }

}