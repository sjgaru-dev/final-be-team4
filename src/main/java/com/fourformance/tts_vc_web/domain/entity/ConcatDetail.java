package com.fourformance.tts_vc_web.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConcatDetail {
    @Id
    @GeneratedValue
    @Column(name = "concat_detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ConcatProject concatProject;

    private Integer audioSeq;
    private String isChecked;
    private String unitScript;
    private Float endSilence = 0.0F;
    private String silenceApplication;
    private Boolean isDeleted = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 업데이트 메서드
    public void updateDetails(Integer audioSeq, String isChecked, String unitScript, Float endSilence, String silenceApplication, Boolean newIsDeleted) {
        this.audioSeq = audioSeq;
        this.isChecked = isChecked;
        this.unitScript = unitScript;
        this.endSilence = endSilence;
        this.silenceApplication = silenceApplication;
        this.isDeleted = newIsDeleted;
        this.updatedAt = LocalDateTime.now(); // 업데이트 시점 기록
    }

    // 생성 메서드 (isDeleted는 기본값 false)
    public static ConcatDetail createConcatDetail(ConcatProject concatProject, Integer audioSeq,
                                                  String isChecked, String unitScript,
                                                  Float endSilence, String silenceApplication) {
        ConcatDetail concatDetail = new ConcatDetail();
        concatDetail.concatProject = concatProject;
        concatDetail.audioSeq = audioSeq;
        concatDetail.isChecked = isChecked;
        concatDetail.unitScript = unitScript;
        concatDetail.endSilence = endSilence;
        concatDetail.silenceApplication = silenceApplication;
        concatDetail.createdAt = LocalDateTime.now(); // 생성 시점 기록
        concatDetail.updatedAt = LocalDateTime.now(); // 생성 시점과 동일하게 설정
        return concatDetail;
    }

//    public void updateIsDeleted() {
//        this.isDeleted = true;
//        this.updatedAt = LocalDateTime.now(); // isDeleted 상태 변경 시 업데이트 시간 기록
//    }
}