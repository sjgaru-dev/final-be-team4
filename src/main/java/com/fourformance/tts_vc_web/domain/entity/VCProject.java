package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "vc_project")
public class VCProject extends Project{

    @Enumerated(EnumType.STRING)
    private APIStatusConst apiStatus=APIStatusConst.NOT_STARTED; // enum 생성 필요

    @OneToOne(fetch = FetchType.LAZY)
    private MemberAudioMeta memberTargetAudioMeta;

    private LocalDateTime APIStatusModifiedAt;

    // 생성 메서드
    public static VCProject createVCProject(Member member, String projectName) {
        VCProject vcProject = new VCProject();
        vcProject.member = member;
        vcProject.projectName = projectName;
        vcProject.createdAt();
        vcProject.updatedAt();
        return vcProject;
    }

    // 업데이트 메서드
    public void updateVCProject(String projectName, MemberAudioMeta memberTargetAudioMeta) {
        this.memberTargetAudioMeta = memberTargetAudioMeta;
        super.projectName = projectName;
        super.updatedAt();
    }

    // API 상태 변경 메서드
    public void updateAPIStatus(APIStatusConst apiStatus) {
        this.apiStatus = apiStatus;
        this.APIStatusModifiedAt = LocalDateTime.now();
    }

    // 삭제 메서드
    public void deleteVCProject() {
        super.isDeleted = true;
        super.deletedAt();
    }

    // 복구 메서드
    public void restoreVCProject() {
        super.isDeleted = false;
        super.deletedAtNull();
    }


}
