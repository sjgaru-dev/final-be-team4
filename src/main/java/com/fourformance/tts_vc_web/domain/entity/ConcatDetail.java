package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.domain.baseEntity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConcatDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "concat_detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ConcatProject concatProject;

    private Integer audioSeq;
    private boolean isChecked = true;
    private String unitScript;
    private Float endSilence = 0.0F;
    private Boolean isDeleted = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_audio_id")
    private MemberAudioMeta memberAudioMeta;

    // 생성 메서드
    public static ConcatDetail createConcatDetail(ConcatProject concatProject, Integer audioSeq,
                                                  boolean isChecked, String unitScript,
                                                  Float endSilence, MemberAudioMeta memberAudioMeta) {
        ConcatDetail concatDetail = new ConcatDetail();
        concatDetail.concatProject = concatProject;
        concatDetail.audioSeq = audioSeq;
        concatDetail.isChecked = isChecked;
        concatDetail.unitScript = unitScript;
        concatDetail.endSilence = endSilence;
        concatDetail.createdAt = LocalDateTime.now();
        concatDetail.updatedAt = LocalDateTime.now();
        concatDetail.memberAudioMeta = memberAudioMeta;
        return concatDetail;
    }

    // 업데이트 메서드
    public void updateDetails(Integer audioSeq, boolean isChecked, String unitScript, Float endSilence,
                              Boolean newIsDeleted) {
        this.audioSeq = audioSeq;
        this.isChecked = isChecked;
        this.unitScript = unitScript;
        this.endSilence = endSilence;
        this.isDeleted = newIsDeleted;
        this.updatedAt = LocalDateTime.now();
    }

    // 멤버 오디오메타 주입 메서드
    public void injectMemberAudioMeta(MemberAudioMeta memberAudioMeta) {
        this.memberAudioMeta = memberAudioMeta;
    }

    // 삭제 메서드
    public void deleteConcatDetail() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}