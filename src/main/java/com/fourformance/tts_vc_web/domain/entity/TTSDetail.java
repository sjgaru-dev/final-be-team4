package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.domain.baseEntity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tts_detail")
public class TTSDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tts_detail_id")
    private Long id;

    @OneToMany(mappedBy = "ttsDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<APIStatus> apiStatuses = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private TTSProject ttsProject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voice_style_id")
    private VoiceStyle voiceStyle;

    private String unitScript;
    private Float unitSpeed = 1f;
    private Float unitPitch = 0f;
    private Float unitVolume = 0f;
    private Boolean isDeleted = false;
    private Integer unitSequence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // 생성 메서드
    public static TTSDetail createTTSDetail(TTSProject ttsProject, String unitScript, Integer unitSequence) {
        TTSDetail ttsDetail = new TTSDetail();
        ttsDetail.ttsProject = ttsProject;
        ttsDetail.unitScript = unitScript;
        ttsDetail.unitSequence = unitSequence;
        ttsDetail.createdAt = LocalDateTime.now();
        ttsDetail.updatedAt = LocalDateTime.now();
        return ttsDetail;
    }

    // 연관관계 편의 메서드
    public void addAPIStatus(APIStatus apiStatus) {
        this.apiStatuses.add(apiStatus);
        apiStatus.setTtsDetail(this);
    }

    public void removeAPIStatus(APIStatus apiStatus) {
        this.apiStatuses.remove(apiStatus);
        apiStatus.setTtsDetail(null);
    }

    // 업데이트 메서드
    public void updateTTSDetail(VoiceStyle voiceStyle, String newUnitScript, Float newUnitSpeed, Float newUnitPitch,
                                Float newUnitVolume, Integer newUnitSequence, Boolean newIsDeleted) {
        this.voiceStyle = voiceStyle;
        this.unitScript = newUnitScript;
        this.unitSpeed = newUnitSpeed;
        this.unitPitch = newUnitPitch;
        this.unitVolume = newUnitVolume;
        this.unitSequence = newUnitSequence;
        this.isDeleted = newIsDeleted;
        this.updatedAt = LocalDateTime.now();
    }

    // 삭제 메서드
    public void deleteTTSDetail() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}