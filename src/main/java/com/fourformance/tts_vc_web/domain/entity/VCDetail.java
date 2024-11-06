package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.domain.baseEntity.BaseEntity;
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
public class VCDetail extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "vc_detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private VCProject vcProject;

    private Boolean isChecked;
    private String unitScript;
    private Boolean isDeleted=false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // isChecked와 unitScript를 동시에 업데이트하는 메서드
    public void updateDetails(Boolean newIsChecked, String newUnitScript, Boolean newIsDeleted) {
        this.isChecked = newIsChecked;
        this.unitScript = newUnitScript;
        this.isDeleted = newIsDeleted;
        this.setUpdatedAt();
    }

    // isDeleted 업데이트 메서드
    public void markAsDeleted() {
        this.isDeleted = true;
        this.setUpdatedAt();
    }

    // updatedAt 업데이트 메서드 (내부에서 호출)
    private void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    public static VCDetail createVCDetail(VCProject vcProject) {
        VCDetail vcDetail = new VCDetail();
        vcDetail.vcProject = vcProject;
        vcDetail.createdAt = LocalDateTime.now();
        vcDetail.updatedAt = LocalDateTime.now();
        return vcDetail;
    }
}