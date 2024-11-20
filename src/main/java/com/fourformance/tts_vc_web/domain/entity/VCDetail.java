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
import jakarta.persistence.OneToOne;
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
@Table(name = "vc_detail")
public class VCDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vc_detail_id")
    private Long id;

    @OneToMany(mappedBy = "vcDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<APIStatus> apiStatuses = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private VCProject vcProject;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_audio_id")
    private MemberAudioMeta memberAudioMeta;

    private Boolean isChecked = false;
    private String unitScript;
    private Boolean isDeleted = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // 생성 메서드
    public static VCDetail createVCDetail(VCProject vcProject, MemberAudioMeta memberAudioMeta) {
        VCDetail vcDetail = new VCDetail();
        vcDetail.vcProject = vcProject;
        vcDetail.memberAudioMeta = memberAudioMeta;
        vcDetail.createdAt = LocalDateTime.now();
        vcDetail.updatedAt = LocalDateTime.now();
        return vcDetail;
    }

    // 연관관계 편의 메서드
    public void addAPIStatus(APIStatus apiStatus) {
        this.apiStatuses.add(apiStatus);
        apiStatus.setVcDetail(this);
    }

    public void removeAPIStatus(APIStatus apiStatus) {
        this.apiStatuses.remove(apiStatus);
        apiStatus.setVcDetail(null);
    }

    // 업데이트 메서드
    public void updateDetails(Boolean newIsChecked, String newUnitScript) {
        this.isChecked = newIsChecked;
        this.unitScript = newUnitScript;
        this.updatedAt = LocalDateTime.now();
    }

    // 삭제 메서드
    public void markAsDeleted() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}