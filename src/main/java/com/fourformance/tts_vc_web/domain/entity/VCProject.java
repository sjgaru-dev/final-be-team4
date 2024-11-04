package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VCProject extends Project{
    private APIStatusConst apiStatus=APIStatusConst.NOT_STARTED; // enum 생성 필요

    // apiStatus 업데이트 메서드
    public void updateApiStatus(APIStatusConst newApiStatus) {
        this.apiStatus = newApiStatus;
        super.updateUpdatedAt();
    }
    public static VCProject createVCProject() {
        VCProject vcProject = new VCProject();
        return vcProject;
    }

}
