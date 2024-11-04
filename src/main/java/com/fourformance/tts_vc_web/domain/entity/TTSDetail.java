package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.domain.baseEntity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TTSDetail extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "tts_detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private TTSProject ttsProject;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "style_id")
    private Style style;

    private String unitScript;
    private Float unitSpeed;
    private Float unitPitch;
    private Float unitVolume;
    private Boolean isDeleted = false;
    private Integer unitSequence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

//    // isDeleted 업데이트 메서드
//    public void markAsDeleted() {
//        this.isDeleted = true;
//        this.updatedAt = LocalDateTime.now();
//    }

    // 여러 필드를 동시에 업데이트하는 메서드
    public void updateTTSDetails(Style style, String newUnitScript, Float newUnitSpeed, Float newUnitPitch, Float newUnitVolume, Integer newUnitSequence, Boolean newIsDeleted) {
        this.style = style;
        this.unitScript = newUnitScript;
        this.unitSpeed = newUnitSpeed;
        this.unitPitch = newUnitPitch;
        this.unitVolume = newUnitVolume;
        this.unitSequence = newUnitSequence;
        this.isDeleted = newIsDeleted;
        this.updatedAt = LocalDateTime.now();
    }
    public static TTSDetail createTTSDetail(TTSProject ttsProject, String unitScript, Integer unitSequence) {
        TTSDetail ttsDetail = new TTSDetail();
        ttsDetail.ttsProject = ttsProject;
        ttsDetail.unitScript = unitScript;
        ttsDetail.unitSequence = unitSequence;
        ttsDetail.createdAt = LocalDateTime.now(); // 생성 시간 설정
        ttsDetail.updatedAt = LocalDateTime.now(); // 최초 생성 시 업데이트 시간도 함께 설정
        return ttsDetail;
    }


}