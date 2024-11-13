package com.fourformance.tts_vc_web.dto.tts;

import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class TTSDetailDto {

    private Long id; // 상세 정보 ID
    private Long ttsProjectId; // 프로젝트 ID
    private String unitScript; // 단위 스크립트
    private Float unitSpeed; // 단위 속도
    private Float unitPitch; // 단위 피치
    private Float unitVolume; // 단위 볼륨
    private Boolean isDeleted; // 삭제 여부
    private Integer unitSequence; // 단위 시퀀스
    private LocalDateTime createdAt; // 생성 시간
    private LocalDateTime updatedAt; // 업데이트 시간
    private LocalDateTime deletedAt; // 삭제 시간

    // TTSDetail 엔티티를 TTSDetailDTO로 변환하는 생성자
    public static TTSDetailDto fromEntity(TTSDetail ttsDetail) {
        return new TTSDetailDto(
                ttsDetail.getId(),
                ttsDetail.getTtsProject() != null ? ttsDetail.getTtsProject().getId() : null, // 프로젝트 ID만 포함
                ttsDetail.getUnitScript(),
                ttsDetail.getUnitSpeed(),
                ttsDetail.getUnitPitch(),
                ttsDetail.getUnitVolume(),
                ttsDetail.getIsDeleted(),
                ttsDetail.getUnitSequence(),
                ttsDetail.getCreatedAt(),
                ttsDetail.getUpdatedAt(),
                ttsDetail.getDeletedAt()
        );
    }
}